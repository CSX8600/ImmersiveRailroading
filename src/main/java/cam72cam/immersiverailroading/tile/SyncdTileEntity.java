package cam72cam.immersiverailroading.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;

public class SyncdTileEntity extends TileEntity {
	public boolean hasTileData;
	
	public static SyncdTileEntity get(IBlockAccess world, BlockPos pos, EnumCreateEntityType type) {
		TileEntity te;
		if (world instanceof World) {
			te = ((World)world).getChunkFromBlockCoords(pos).getTileEntity(pos, type);
		} else {
			te = world.getTileEntity(pos);
		}
		
		if (te instanceof SyncdTileEntity) {
			return (SyncdTileEntity) te;
		}
		return null;
	}
	
	public boolean isLoaded() {
		return this.hasWorldObj() && (!worldObj.isRemote || hasTileData);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (!worldObj.isRemote) {
			worldObj.notifyBlockUpdate(getPos(), worldObj.getBlockState(getPos()), worldObj.getBlockState(getPos()), 1 + 2 + 8);
			worldObj.notifyNeighborsOfStateChange(pos, this.getBlockType());
		}
	}
	
	public void writeUpdateNBT(NBTTagCompound nbt) {
	}
	public void readUpdateNBT(NBTTagCompound nbt) {
	}
	
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		this.writeUpdateNBT(nbt);
		
		return new SPacketUpdateTileEntity(this.getPos(), 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
		this.readUpdateNBT(pkt.getNbtCompound());
		super.onDataPacket(net, pkt);
		if (updateRerender()) {
			worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
		}
		hasTileData = true;
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.getUpdateTag();
		if (this.isLoaded()) {
			this.writeToNBT(tag);
			this.writeUpdateNBT(tag);
		}
		return tag;
	}
	
	public boolean updateRerender() {
		return false;
	}
	
	@Override 
	public void handleUpdateTag(NBTTagCompound tag) {
		this.readFromNBT(tag);
		this.readUpdateNBT(tag);
		super.handleUpdateTag(tag);
		if (updateRerender()) {
			worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
		}
		hasTileData = true;
	}
}
