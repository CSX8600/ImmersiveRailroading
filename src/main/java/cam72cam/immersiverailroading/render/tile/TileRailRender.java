package cam72cam.immersiverailroading.render.tile;

import org.lwjgl.opengl.GL11;

import cam72cam.immersiverailroading.render.rail.RailRenderUtil;
import cam72cam.immersiverailroading.tile.TileRail;
import cam72cam.immersiverailroading.util.GLBoolTracker;
import cam72cam.immersiverailroading.util.RailInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TileRailRender extends TileEntitySpecialRenderer<TileRail> {
	
	@Override
	public boolean isGlobalRenderer(TileRail te) {
		return true;
	}
	

	@Override
	public void renderTileEntityAt(TileRail te, double x, double y, double z, float partialTicks, int destroyStage) {
		RailInfo info = te.getRailRenderInfo();
		if (info == null) {
			// Still loading...
			return;
		}
		Minecraft.getMinecraft().mcProfiler.startSection("tile_rail");
		GL11.glPushMatrix();
		{
			// Move to specified position
			GL11.glTranslated(x, y, z);
			GLBoolTracker blend = new GLBoolTracker(GL11.GL_BLEND, false);			
			RailRenderUtil.render(info, false);
			blend.restore();
		}
		GL11.glPopMatrix();
		Minecraft.getMinecraft().mcProfiler.endSection();
	}
}
