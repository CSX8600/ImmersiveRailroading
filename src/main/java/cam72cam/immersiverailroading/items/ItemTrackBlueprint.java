package cam72cam.immersiverailroading.items;

import java.util.List;

import cam72cam.immersiverailroading.IRBlocks;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.blocks.BlockRailBase;
import cam72cam.immersiverailroading.items.nbt.ItemGauge;
import cam72cam.immersiverailroading.library.GuiText;
import cam72cam.immersiverailroading.library.GuiTypes;
import cam72cam.immersiverailroading.library.TrackDirection;
import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.library.TrackPositionType;
import cam72cam.immersiverailroading.tile.TileRailPreview;
import cam72cam.immersiverailroading.util.RailInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
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
public class ItemTrackBlueprint extends Item {
	public static final String NAME = "item_rail";

	public ItemTrackBlueprint() {
		setUnlocalizedName(ImmersiveRailroading.MODID + ":" + NAME);
		setRegistryName(new ResourceLocation(ImmersiveRailroading.MODID, NAME));
        this.setCreativeTab(ItemTabs.MAIN_TAB);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (worldIn.isRemote && handIn == EnumHand.MAIN_HAND) {
            playerIn.openGui(ImmersiveRailroading.instance, GuiTypes.RAIL.ordinal(), worldIn, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
        }
        return super.onItemRightClick(itemStackIn, worldIn, playerIn, handIn);
	}
	
	@Override
	public EnumActionResult onItemUse(ItemStack stackIn, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		pos = pos.up();
		
		ItemStack stack = player.getHeldItem(hand);
		if (ItemTrackBlueprint.isPreview(stack)) {
			world.setBlockState(pos, IRBlocks.BLOCK_RAIL_PREVIEW.getDefaultState());
			TileRailPreview te = TileRailPreview.get(world, pos);
			if (te != null) {
				te.init(stack, player.getRotationYawHead(), hitX, hitY, hitZ);
			}
			return EnumActionResult.SUCCESS;
		}
		if (player.getEntityWorld().getBlockState(pos.down()).getBlock() instanceof BlockRailBase) {
			pos = pos.down();
		}
		
		RailInfo info = new RailInfo(stack, player.worldObj, player.getRotationYawHead(), pos, hitX, hitY, hitZ); 
		info.build(player, pos);
		return EnumActionResult.SUCCESS;
    }

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
		super.addInformation(stack, playerIn, tooltip, advanced);
        tooltip.add(GuiText.TRACK_TYPE.toString(getType(stack)));
        tooltip.add(GuiText.TRACK_GAUGE.toString(ItemGauge.get(stack)));
        tooltip.add(GuiText.TRACK_LENGTH.toString(getLength(stack)));
        tooltip.add(GuiText.TRACK_POSITION.toString(getPosType(stack)));
		tooltip.add(GuiText.TRACK_DIRECTION.toString(getDirection(stack)));
        tooltip.add(GuiText.TRACK_RAIL_BED.toString(getBed(stack) != null ? getBed(stack).getDisplayName() : ""));
        tooltip.add(GuiText.TRACK_RAIL_BED_FILL.toString(getBedFill(stack) != null ? getBedFill(stack).getDisplayName() : ""));
        tooltip.add((isPreview(stack) ? GuiText.TRACK_PLACE_BLUEPRINT_TRUE : GuiText.TRACK_PLACE_BLUEPRINT_FALSE).toString());
        tooltip.add(GuiText.TRACK_QUARTERS.toString(getQuarters(stack) * 90.0/4 ));
	}

	public static void setType(ItemStack stack, TrackItems type) {
		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger("type", type.ordinal());
	}
	
	public static TrackItems getType(ItemStack stack) {
		if (stack.getTagCompound() != null){
			return TrackItems.values()[stack.getTagCompound().getInteger("type")];
		}
		return TrackItems.STRAIGHT;
	}
	
	public static void setLength(ItemStack stack, int length) {
		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger("length", length);
	}
	
	public static int getLength(ItemStack stack) {
		if (stack.getTagCompound() != null){
			return stack.getTagCompound().getInteger("length");
		}
		return 10;
	}
	
	public static void setQuarters(ItemStack stack, int quarters) {
		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger("quarters", quarters);
	}
	
	public static int getQuarters(ItemStack stack) {
		if (stack.getTagCompound() != null){
			return stack.getTagCompound().getInteger("quarters");
		}
		return 4;
	}
	
	public static void setPosType(ItemStack stack, TrackPositionType posType) {
		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger("pos_type", posType.ordinal());
	}
	
	public static TrackPositionType getPosType(ItemStack stack) {
		if (stack.getTagCompound() != null){
			return TrackPositionType.values()[stack.getTagCompound().getInteger("pos_type")];
		}
		return TrackPositionType.FIXED;
	}
	
	public static void setDirection(ItemStack stack, TrackDirection posType) {
		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger("direction", posType.ordinal());
	}
	
	public static TrackDirection getDirection(ItemStack stack) {
		if (stack.getTagCompound() != null){
			return TrackDirection.values()[stack.getTagCompound().getInteger("direction")];
		}
		return TrackDirection.NONE;
	}

	public static ItemStack getBed(ItemStack stack) {
		if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("bedItem")) {
			return ItemStack.loadItemStackFromNBT(stack.getTagCompound().getCompoundTag("bedItem"));
		} else {
			return null;
		}
	}
	
	public static void setBed(ItemStack stack, ItemStack base) {
		if (base == null) {
			stack.getTagCompound().setTag("bedItem", new NBTTagCompound());
		} else {
			stack.getTagCompound().setTag("bedItem", base.serializeNBT());
		}
	}

	public static ItemStack getBedFill(ItemStack stack) {
		if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("bedFill")) { 
			return ItemStack.loadItemStackFromNBT(stack.getTagCompound().getCompoundTag("bedFill"));
		} else {
			return null;
		}
	}
	public static void setBedFill(ItemStack stack, ItemStack base) {
		if (base == null) {
			stack.getTagCompound().setTag("bedFill", new NBTTagCompound());
		} else {
			stack.getTagCompound().setTag("bedFill", base.serializeNBT());
		}
	}

	public static boolean isPreview(ItemStack stack) {
		if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("isPreview")) { 
			return stack.getTagCompound().getBoolean("isPreview");
		} else {
			return false;
		}
	}
	public static void setPreview(ItemStack stack, boolean value) {
		stack.getTagCompound().setBoolean("isPreview", value);
	}

	public static boolean isGradeCrossing(ItemStack stack) {
		if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("isGradeCrossing")) { 
			return stack.getTagCompound().getBoolean("isGradeCrossing");
		} else {
			return false;
		}
	}
	public static void setGradeCrossing(ItemStack stack, boolean value) {
		stack.getTagCompound().setBoolean("isGradeCrossing", value);
	}
}
