package cam72cam.immersiverailroading.render.rail;

import org.lwjgl.opengl.GL11;

import cam72cam.immersiverailroading.track.BuilderBase.PosRot;
import cam72cam.immersiverailroading.track.TrackBase;
import cam72cam.immersiverailroading.util.GLBoolTracker;
import cam72cam.immersiverailroading.util.RailInfo;

public class RailBaseOverlayRender {
	private static void doDraw(RailInfo info) {
		GL11.glTranslated(-info.position.getX(), -info.position.getY(), -info.position.getZ());
		
		GL11.glColor4f(1, 0, 0, 1);
		
		for (TrackBase base : info.getBuilder(info.position).getTracksForRender()) {
			if (!base.canPlaceTrack() ) {
				GL11.glPushMatrix();
				PosRot pos = base.getPos();
				GL11.glTranslated(pos.getX(), pos.getY(), pos.getZ()+1);
				GL11.glScaled(1, base.getHeight() + 0.2f, 1);

				GL11.glBegin(GL11.GL_QUADS);
				// front
			    GL11.glVertex3f(0.0f, 0.0f, 0.0f);
			    GL11.glVertex3f(1.0f, 0.0f, 0.0f);
			    GL11.glVertex3f(1.0f, 1.0f, 0.0f);
			    GL11.glVertex3f(0.0f, 1.0f, 0.0f);
			    // back
			    GL11.glVertex3f(0.0f, 0.0f, -1.0f);
			    GL11.glVertex3f(1.0f, 0.0f, -1.0f);
			    GL11.glVertex3f(1.0f, 1.0f, -1.0f);
			    GL11.glVertex3f(0.0f, 1.0f, -1.0f);
			    // right
			    GL11.glVertex3f(1.0f, 0.0f, 0.0f);
			    GL11.glVertex3f(1.0f, 0.0f, -1.0f);
			    GL11.glVertex3f(1.0f, 1.0f, -1.0f);
			    GL11.glVertex3f(1.0f, 1.0f, 0.0f);
			    // left
			    GL11.glVertex3f(0.0f, 0.0f, 0.0f);
			    GL11.glVertex3f(0.0f, 0.0f, -1.0f);
			    GL11.glVertex3f(0.0f, 1.0f, -1.0f);
			    GL11.glVertex3f(0.0f, 1.0f, 0.0f);
			    // top
			    GL11.glVertex3f(0.0f, 1.0f, 0.0f);
			    GL11.glVertex3f(1.0f, 1.0f, 0.0f);
			    GL11.glVertex3f(1.0f, 1.0f, -1.0f);
			    GL11.glVertex3f(0.0f, 1.0f, -1.0f);
			    // bottom
			    GL11.glVertex3f(0.0f, 0.0f, 0.0f);
			    GL11.glVertex3f(1.0f, 0.0f, 0.0f);
			    GL11.glVertex3f(1.0f, 0.0f, -1.0f);
			    GL11.glVertex3f(0.0f, 0.0f, -1.0f);
				GL11.glEnd();
				
			    GL11.glPopMatrix();
			}
		}
	}

	public static void draw(RailInfo info) {
		GLBoolTracker tex = new GLBoolTracker(GL11.GL_TEXTURE_2D, false);
		GLBoolTracker color = new GLBoolTracker(GL11.GL_COLOR_MATERIAL, true);
		GL11.glPushMatrix();
		doDraw(info);
		GL11.glPopMatrix();
		tex.restore();
		color.restore();
	}
}
