package cam72cam.immersiverailroading.gui.overlay;

import org.lwjgl.opengl.GL11;

import cam72cam.immersiverailroading.entity.LocomotiveDiesel;
import cam72cam.immersiverailroading.library.GuiText;
import net.minecraft.entity.Entity;
import net.minecraftforge.fluids.Fluid;

public class DieselLocomotiveOverlay extends LocomotiveOverlay {
	public void draw() {
		Entity riding = mc.player.getRidingEntity();
		if (riding == null) {
			return;
		}
		if (!(riding instanceof LocomotiveDiesel)) {
			return;
		}
		LocomotiveDiesel loco = (LocomotiveDiesel) riding;
		drawGauge(0xAA79650c, ((float)loco.getLiquidAmount())/Fluid.BUCKET_VOLUME, loco.getTankCapacity().Buckets(), "B");
		
		drawScalar(GuiText.LABEL_BRAKE.toString(), loco.getAirBrake()*10, 0, 10);
		drawScalar(GuiText.LABEL_THROTTLE.toString(), loco.getFakeThrottle()*10, -10, 10);
		
		if(loco.getReverse() == -1) {
			drawReverse();
		}
	}
}
