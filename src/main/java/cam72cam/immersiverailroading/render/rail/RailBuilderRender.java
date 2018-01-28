package cam72cam.immersiverailroading.render.rail;

import java.util.ArrayList;
import org.lwjgl.opengl.GL11;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.TrackDirection;
import cam72cam.immersiverailroading.model.obj.OBJModel;
import cam72cam.immersiverailroading.render.DisplayListCache;
import cam72cam.immersiverailroading.render.OBJRender;
import cam72cam.immersiverailroading.track.BuilderBase.VecYawPitch;
import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.immersiverailroading.util.VecUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class RailBuilderRender {
	
	private static OBJRender baseRailModel;
	private static OBJRender baseRailModelModel;
	
	static {
		try {
			baseRailModel = new OBJRender(new OBJModel(new ResourceLocation(ImmersiveRailroading.MODID, "models/block/track_1m.obj"), 0.05f));
		} catch (Exception e) {
			ImmersiveRailroading.catching(e);
		}
		try {
			baseRailModelModel = new OBJRender(new OBJModel(new ResourceLocation(ImmersiveRailroading.MODID, "models/block/track_1m_model.obj"), 0.05f));
		} catch (Exception e) {
			ImmersiveRailroading.catching(e);
		}
	}

	private static DisplayListCache displayLists = new DisplayListCache();
	public static void renderRailBuilder(RailInfo info) {
		
		OBJRender model = info.gauge != Gauge.MODEL ? baseRailModel : baseRailModelModel;

		Vec3d renderOff = new Vec3d(-0.5, 0, -0.5);

		switch (info.facing) {
		case EAST:
			renderOff = renderOff.addVector(0, 0, 1);
			break;
		case NORTH:
			renderOff = renderOff.addVector(1, 0, 1);
			break;
		case SOUTH:
			// No Change
			break;
		case WEST:
			renderOff = renderOff.addVector(1, 0, 0);
			break;
		default:
			break;
		}	
		
		renderOff = VecUtil.rotateYaw(renderOff, (info.direction == TrackDirection.LEFT ? -1 : 1) * info.quarter/4f * 90 - 90);
		GL11.glTranslated(renderOff.xCoord, renderOff.yCoord, renderOff.zCoord);
		//GlStateManager.translate(info.getOffset().x, 0, info.getOffset().z);
		GL11.glTranslated(-info.position.getX(), -info.position.getY(), -info.position.getZ());
		GL11.glTranslated(info.placementPosition.xCoord, info.placementPosition.yCoord, info.placementPosition.zCoord);
		
		renderOff = VecUtil.fromYaw((info.gauge.value() - Gauge.STANDARD.value()) * 0.34828 *2, info.facing.getOpposite().getHorizontalAngle()-90);
		GL11.glTranslated(renderOff.xCoord, renderOff.yCoord, renderOff.zCoord);

		Integer displayList = displayLists.get(RailRenderUtil.renderID(info));
		if (displayList == null) {
			displayList = GL11.glGenLists(1);
			GL11.glNewList(displayList, GL11.GL_COMPILE);		
			
			for (VecYawPitch piece : info.getBuilder().getRenderData()) {
				GL11.glPushMatrix();;
				GL11.glRotatef(180-info.facing.getOpposite().getHorizontalAngle(), 0, 1, 0);
				GL11.glTranslated(piece.xCoord, piece.yCoord, piece.zCoord);
				GL11.glRotatef(piece.getYaw(), 0, 1, 0);
				GL11.glRotatef(piece.getPitch(), 1, 0, 0);
				GL11.glRotatef(-90, 0, 1, 0);
				
				if (piece.getGroups().size() != 0) {
					if (piece.getLength() != 1) {
						GL11.glScaled(piece.getLength() / info.gauge.scale(), 1, 1);
					}
					
					// TODO static
					ArrayList<String> groups = new ArrayList<String>();
					for (String baseGroup : piece.getGroups()) {
						for (String groupName : model.model.groups())  {
							if (groupName.contains(baseGroup)) {
								groups.add(groupName);
							}
						}
					}
					
					model.drawDirectGroups(groups, info.gauge.scale());
				} else {
					model.drawDirect(info.gauge.scale());
				}
				GL11.glPopMatrix();;
			}

			GL11.glEndList();
			displayLists.put(RailRenderUtil.renderID(info), displayList);
		}
		
		model.bindTexture();
		GL11.glCallList(displayList);
		model.restoreTexture();
	}
}
