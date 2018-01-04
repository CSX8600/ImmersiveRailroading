package cam72cam.immersiverailroading.track;

import java.util.ArrayList;
import java.util.List;

import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.immersiverailroading.util.VecUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BuilderCrossing extends BuilderBase {
	/*
	 * This is terrible
	 */

	public BuilderCrossing(RailInfo info, BlockPos pos) {
		super(info, pos);
		
		info.length = 3;
		
		this.setParentPos(new BlockPos(0, 0, 0));
		
		tracks.add(new TrackRail(this, 0, 0, 0, EnumFacing.NORTH, TrackItems.CROSSING, 3, info.quarter, info.placementPosition));
		int blocks = 1;
		for (int i = -blocks; i <=blocks; i ++) {
			for (int j = -blocks; j <=blocks; j ++) {
				if (i == 0 && j == 0) {
					continue;
				}
				tracks.add(new TrackGag(this, i, 0, j));
			}
		}
	}

	@Override
	public List<VecYawPitch> getRenderData() {
		
		List<VecYawPitch> data = new ArrayList<VecYawPitch>();
		float angle = 0;
		info.length = 3;
		
		Vec3d pos = VecUtil.rotateYaw(new Vec3d(-0.5, 0, 0), angle-90);
		data.add(new VecYawPitch(pos.xCoord, pos.yCoord, pos.zCoord-1, -angle, 0, info.length, "RAIL_RIGHT", "RAIL_LEFT"));
		
		for (double i = 0; i < info.length-gauge.scale()/2; i+=gauge.scale()) {
			pos = VecUtil.rotateYaw(new Vec3d(-0.5, 0, i), angle-90);
			data.add(new VecYawPitch(pos.xCoord, pos.yCoord, pos.zCoord-1, -angle, "RAIL_BASE"));
		}
		angle -= 90;
		
		pos = VecUtil.rotateYaw(new Vec3d(-0.5, 0, 0), angle-90);
		data.add(new VecYawPitch(pos.xCoord-(2-gauge.scale()), pos.yCoord, pos.zCoord+gauge.scale(), -angle, 0, info.length, "RAIL_RIGHT", "RAIL_LEFT"));
		for (double i = 0; i < info.length-gauge.scale()/2; i+=gauge.scale()) {
			pos = VecUtil.rotateYaw(new Vec3d(-0.5, 0, i), angle-90);
			data.add(new VecYawPitch(pos.xCoord-(2-gauge.scale()), pos.yCoord, pos.zCoord+gauge.scale(), -angle, "RAIL_BASE"));
		}
		
		return data;
	}

}
