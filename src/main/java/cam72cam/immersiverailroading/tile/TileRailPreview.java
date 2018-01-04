package cam72cam.immersiverailroading.tile;

import cam72cam.immersiverailroading.util.RailInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileRailPreview extends SyncdTileEntity {
	public static TileRailPreview get(IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		return te instanceof TileRailPreview ? (TileRailPreview) te : null;
	}

	private ItemStack item;
	float yawHead;
	float hitX;
	float hitY;
	float hitZ;
	
	@Override
	@SideOnly(Side.CLIENT)
	public net.minecraft.util.math.AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return Double.MAX_VALUE;
	}

	public ItemStack getItem() {
		return this.item;
	}

	public void setItem(ItemStack stack) {
		this.item = stack.copy();
		this.markDirty();
	}

	public void setHit(float hitX, float hitY, float hitZ) {
		this.hitX = hitX;
		this.hitY = hitY;
		this.hitZ = hitZ;
		this.markDirty();
	}
	
	public void init(ItemStack item, float yawHead, float hitX, float hitY, float hitZ) {
		this.item = item.copy();
		this.yawHead = yawHead;
		this.hitX = hitX;
		this.hitY = hitY;
		this.hitZ = hitZ;
		this.markDirty();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		item = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("item"));
		yawHead = nbt.getFloat("yawHead");
		hitX = nbt.getFloat("hitX");
		hitY = nbt.getFloat("hitY");
		hitZ = nbt.getFloat("hitZ");
	}
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setTag("item", item.serializeNBT());
		nbt.setFloat("yawHead", yawHead);
		nbt.setFloat("hitX", hitX);
		nbt.setFloat("hitY", hitY);
		nbt.setFloat("hitZ", hitZ);
		
		return super.writeToNBT(nbt);
	}
	
	public RailInfo getRailRenderInfo() {
		if (hasTileData || !worldObj.isRemote) {
			return new RailInfo(item, worldObj, yawHead, pos, hitX, hitY, hitZ);
		}
		return null;
	}
}
