package cam72cam.immersiverailroading.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class NBTUtil {
	//TODO move other nbt helpers here

	public final static NBTTagCompound vec3dToNBT(Vec3d value) {
		NBTTagCompound nbt = new NBTTagCompound();
		if (value != null) {
			nbt.setDouble("x", value.xCoord);
			nbt.setDouble("y", value.yCoord);
			nbt.setDouble("z", value.zCoord);
		}
		return nbt;
	}
	public final static Vec3d nbtToVec3d(NBTTagCompound nbt) {
		return new Vec3d(nbt.getDouble("x"),nbt.getDouble("y"),nbt.getDouble("z"));
	}
}
