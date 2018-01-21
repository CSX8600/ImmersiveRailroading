package cam72cam.immersiverailroading.items;

import java.util.List;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.items.nbt.ItemMultiblockType;
import cam72cam.immersiverailroading.library.GuiText;
import cam72cam.immersiverailroading.multiblock.MultiblockRegistry;
import cam72cam.immersiverailroading.util.BlockUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemManual extends Item {
	public static final String NAME = "item_manual";
	
	public ItemManual() {
		super();
		
		setUnlocalizedName(ImmersiveRailroading.MODID + ":" + NAME);
		setRegistryName(new ResourceLocation(ImmersiveRailroading.MODID, NAME));
        this.setCreativeTab(ItemTabs.MAIN_TAB);
        //TODO LOCALIZATION
	}
	
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean flagIn)
    {
        tooltip.add(GuiText.SELECTOR_TYPE.toString(ItemMultiblockType.get(stack)));
    }
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World world, EntityPlayer player, EnumHand hand) {
		if (player.isSneaking()) {
			if (!world.isRemote) {
				ItemStack item = player.getHeldItem(hand);
				String current = ItemMultiblockType.get(item);
				List<String> keys = MultiblockRegistry.keys();
				current = keys.get((keys.indexOf(current) + 1) % (keys.size()));
				ItemMultiblockType.set(item, current);
				player.addChatMessage(new TextComponentString("Placing: " + current));
			}
		} else {
			if (world.isRemote) {
				player.addChatMessage(new TextComponentString("Coming Soon..."));
			}
		}
		return super.onItemRightClick(itemStackIn, world, player, hand);
	}
	
	@Override
	public EnumActionResult onItemUse(ItemStack stackIn, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			ItemStack item = player.getHeldItem(hand);
			String current = ItemMultiblockType.get(item);
			BlockPos realPos = pos;
			if (facing == EnumFacing.DOWN) {
				realPos = realPos.down();
			}
			if (facing == EnumFacing.UP) {
				realPos = realPos.up();
			}
			MultiblockRegistry.get(current).place(world, player, realPos, BlockUtil.rotFromFacing(EnumFacing.fromAngle(player.rotationYawHead+180)));
		}
		return EnumActionResult.PASS;
	}
}
