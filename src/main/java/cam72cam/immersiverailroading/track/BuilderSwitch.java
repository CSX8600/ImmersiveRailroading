package cam72cam.immersiverailroading.track;

import java.util.List;

import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.util.RailInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BuilderSwitch extends BuilderBase {

	private BuilderTurn turnBuilder;
	private BuilderStraight straightBuilder;

	public BuilderSwitch(RailInfo info, BlockPos pos) {
		super(info, pos);
		
		info.quarter = 0;
		
		RailInfo turnInfo = info.clone();
		turnInfo.type = TrackItems.TURN;

		straightBuilder = new BuilderStraight(info, pos, true);
		turnBuilder = new BuilderTurn(turnInfo, pos);
		turnBuilder.overrideFlexible = true;
		
		for(TrackBase turn : turnBuilder.tracks) {
			if (turn instanceof TrackRail) {
				turn.overrideParent(new BlockPos(straightBuilder.mainX, 0, straightBuilder.mainZ));
			}
		}
		for (TrackBase straight : straightBuilder.tracks) {
			if (straight instanceof TrackGag) {
				straight.setFlexible();
			}
		}
	}
	
	public int costTies() {
		return straightBuilder.costTies() + turnBuilder.costTies();
	}
	
	public int costRails() {
		return straightBuilder.costRails() + turnBuilder.costRails();
	}

	public int costBed() {
		return straightBuilder.costBed() + turnBuilder.costBed();
	}
	
	public int costFill() {
		return straightBuilder.costFill() + turnBuilder.costFill();
	}
	
	public void setDrops(List<ItemStack> drops) {
		straightBuilder.setDrops(drops);
	}
	

	@Override
	public boolean canBuild() {
		return straightBuilder.canBuild() && turnBuilder.canBuild();
	}
	
	@Override
	public void build() {
		straightBuilder.build();
		turnBuilder.build();
	}
	
	@Override
	public List<TrackBase> getTracksForRender() {
		List<TrackBase> data = straightBuilder.getTracksForRender();
		// TODO flag for in hand render
		//if (info.relativePosition) {
			data.addAll(turnBuilder.getTracksForRender());
		//}
		return data;
	}
	
	@Override
	public List<VecYawPitch> getRenderData() {
		List<VecYawPitch> data = straightBuilder.getRenderData();
		// TODO flag for in hand render
		//if (info.relativePosition) {
			data.addAll(turnBuilder.getRenderData());
		//}
		return data;
	}

	private boolean between(double start, double end, double offset) {
		return Math.abs(start - end) < offset;
	}

	public boolean isOnStraight(Vec3d position) {
		if (between(straightBuilder.x + info.placementPosition.xCoord, position.xCoord, gauge.scale())) {
			return true;
		}
		
		if (between(straightBuilder.z + info.placementPosition.zCoord, position.zCoord, gauge.scale())) {
			return true;
		}
		
		return false;
	}
}