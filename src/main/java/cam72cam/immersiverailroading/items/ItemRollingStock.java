package cam72cam.immersiverailroading.items;

import java.util.List;

import cam72cam.immersiverailroading.ConfigGraphics;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.items.nbt.ItemDefinition;
import cam72cam.immersiverailroading.items.nbt.ItemGauge;
import cam72cam.immersiverailroading.library.GuiText;
import cam72cam.immersiverailroading.library.ChatText;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.registry.CarPassengerDefinition;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.registry.LocomotiveDefinition;
import cam72cam.immersiverailroading.tile.TileRailBase;
import cam72cam.immersiverailroading.util.BlockUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.Interface(iface = "mezz.jei.api.ingredients.ISlowRenderItem", modid = "jei")
public class ItemRollingStock extends BaseItemRollingStock {
	public static final String NAME = "item_rolling_stock";
	
	public ItemRollingStock() {
		super();
		setUnlocalizedName(ImmersiveRailroading.MODID + ":" + NAME);
        setRegistryName(new ResourceLocation(ImmersiveRailroading.MODID, NAME));
        this.setCreativeTab(ItemTabs.STOCK_TAB);
        this.setMaxStackSize(1);
	}
	
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> items)
    {
    	for (String defID : DefinitionManager.getDefinitionNames()) {
    		EntityRollingStockDefinition def = DefinitionManager.getDefinition(defID);
    		if (def instanceof CarPassengerDefinition) {
    			if (tab != ItemTabs.PASSENGER_TAB) {
    				continue;
    			}
    		} else if (def instanceof LocomotiveDefinition) {
    			if (tab != ItemTabs.LOCOMOTIVE_TAB) {
    				continue;
    			}
    		} else {
    			if (tab != ItemTabs.STOCK_TAB) {
    				continue;
    			}
    		}
    		ItemStack stack = new ItemStack(this);
    		ItemDefinition.setID(stack, defID);
			overrideStackDisplayName(stack);
            items.add(stack);
    	}
    }
	
	@Override
	public CreativeTabs[] getCreativeTabs() {
        return new CreativeTabs[]{ ItemTabs.LOCOMOTIVE_TAB, ItemTabs.PASSENGER_TAB, ItemTabs.STOCK_TAB };
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
		overrideStackDisplayName(stack);
		
		Gauge gauge = ItemGauge.get(stack);
		
        super.addInformation(stack, playerIn, tooltip, advanced);
        EntityRollingStockDefinition def = ItemDefinition.get(stack);
        if (def != null) {
        	tooltip.addAll(def.getTooltip(gauge));
        }
        tooltip.add(GuiText.GAUGE_TOOLTIP.toString(gauge));
    }
	
	@Override
	public EnumActionResult onItemUse(ItemStack stackIn, EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (BlockUtil.isIRRail(worldIn, pos)) {
			TileRailBase te = TileRailBase.get(worldIn, pos);
			if (te.getAugment() != null) {
				switch(te.getAugment()) {
				case DETECTOR:
				case FLUID_LOADER:
				case FLUID_UNLOADER:
				case ITEM_LOADER:
				case ITEM_UNLOADER:
					if (!worldIn.isRemote) {
						boolean set = te.setAugmentFilter(ItemDefinition.getID(player.getHeldItem(hand)));
						if (set) {
							player.addChatMessage(ChatText.SET_AUGMENT_FILTER.getMessage(ItemDefinition.get(player.getHeldItem(hand)).name));
						} else {
							player.addChatMessage(ChatText.RESET_AUGMENT_FILTER.getMessage());
						}
					}
					return EnumActionResult.SUCCESS;
				default:
					break;
				}
			}
		}
		return tryPlaceStock(player, worldIn, pos, hand, null);
	}
	
	@Override
	public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity) {
		return armorType == EntityEquipmentSlot.HEAD && ConfigGraphics.trainsOnTheBrain;
	}
}
