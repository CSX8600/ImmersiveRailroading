package cam72cam.immersiverailroading.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import cam72cam.immersiverailroading.library.TrackDirection;
import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.immersiverailroading.util.VecUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BuilderStraight extends BuilderBase {
	protected float angle;
	public int mainX;
	public int mainZ;
	protected HashSet<Pair<Integer, Integer>> positions;
	
	public BuilderStraight(RailInfo info, BlockPos pos) {
		this(info, pos, false);
	}

	public BuilderStraight(RailInfo info, BlockPos pos, boolean endOfTrack) {
		super(info, pos);
		
		if (info.direction == TrackDirection.RIGHT) {
			info.quarter = -info.quarter; 
		}
		
		positions = new HashSet<Pair<Integer, Integer>>();
		HashMap<Pair<Integer, Integer>, Float> heights = new HashMap<Pair<Integer, Integer>, Float>();
		HashSet<Pair<Integer, Integer>> flexPositions = new HashSet<Pair<Integer, Integer>>();
		
		angle = info.quarter/4f * 90;
		
		double actualLength = info.length;
		double horiz = gauge.value();
		if (info.gradeCrossing) {
			horiz += 2f  * gauge.scale();
		}
		double clamp = 0.17 * gauge.scale();
		
		for (float dist = 0; dist < actualLength; dist += 0.25) {
			Vec3d gagPos = VecUtil.fromYaw(dist, angle);
			for (double q = -horiz; q <= horiz; q+=0.1) {
				Vec3d nextUp = VecUtil.fromYaw(q, 90 + angle);
				int posX = (int)(gagPos.xCoord+nextUp.xCoord);
				int posZ = (int)(gagPos.zCoord+nextUp.zCoord);
				double height = (1 - Math.abs((int)q)/horiz)/3 - 0.05;
				height *= gauge.scale();
				height = Math.min(height, clamp);
				positions.add(Pair.of(posX, posZ));
				heights.put(Pair.of(posX, posZ), (float)height);
				if (Math.abs(q) > gauge.scale()) {
					flexPositions.add(Pair.of(posX, posZ));
				}
				if (dist < 3 || dist > actualLength - 3) {
					flexPositions.add(Pair.of(posX, posZ));
				}
			}
			if (endOfTrack) {
				if (Math.ceil(dist) == Math.ceil(actualLength-1)) {
					mainX = (int) gagPos.xCoord;
					mainZ = (int) gagPos.zCoord;
				}
			} else {
				if (Math.ceil(dist) == Math.ceil(actualLength/2)) {
					mainX = (int) gagPos.xCoord;
					mainZ = (int) gagPos.zCoord;
				}
			}
		}
		
		this.setParentPos(new BlockPos(mainX, 0, mainZ));
		TrackRail main = new TrackRail(this, mainX, 0, mainZ, EnumFacing.NORTH, info.type, info.length, info.quarter, info.placementPosition);
		tracks.add(main);
		if (info.gradeCrossing) {
			main.setBedHeight((float)clamp);
		}
		
		for (Pair<Integer, Integer> pair : positions) {
			if (pair.getLeft() == mainX && pair.getRight() == mainZ) {
				// Skip parent block
				continue;
			}
			TrackBase tg = new TrackGag(this, pair.getLeft(), 0, pair.getRight());
			if (flexPositions.contains(pair)) {
				tg.setFlexible();
			}
			if (info.gradeCrossing) {
				tg.setBedHeight(heights.get(pair));
			}
			tracks.add(tg);
		}
	}
	
	@Override
	public List<TrackBase> getTracksForRender() {
		return this.tracks;
	}

	@Override
	public List<VecYawPitch> getRenderData() {
		List<VecYawPitch> data = new ArrayList<VecYawPitch>();
		
		Vec3d pos = VecUtil.rotateYaw(new Vec3d(0, 0, info.length/2.0-0.5), angle-90);
		data.add(new VecYawPitch(pos.xCoord, pos.yCoord, pos.zCoord, -angle, 0, info.length, "RAIL_RIGHT", "RAIL_LEFT"));
		
		double trackOffset = (1-info.gauge.scale())/4;
		
		for (double i = -trackOffset; i < info.length - trackOffset; i+=gauge.scale()) {
			pos = VecUtil.rotateYaw(new Vec3d(0, 0, i-0.25), angle-90);
			data.add(new VecYawPitch(pos.xCoord, pos.yCoord, pos.zCoord, -angle, "RAIL_BASE"));
		}
		return data;
	}
}