package cam72cam.immersiverailroading.interaction;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cam72cam.immersiverailroading.model.BBModel;
import cam72cam.immersiverailroading.model.BBModel.BoundingBox;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RayCaster {
	private BBModel model;
	
	public RayCaster (BBModel model) {
		this.model = model;
	}
	
	/**
	 * 
	 * @param rayStart - the position where the ray should begin (relative to the rolling stock)
	 * @param rayDirection - the direction the ray should shoot in (must be a normalized vector)
	 * @param maxDistance - the maximum distance the ray should shoot (set to 0 = for infinite distance)
	 * @return the name of the OBJ part that got hit (null if nothing got hit)
	 */
	public String getHit (Vec3d rayStart, Vec3d rayDirection, double maxDistance, Vec3d stockPosition, float rotationYaw, World w) {
		List<BoundingBox> bb = model.boundingBoxes;
		Map<Double, BoundingBox> matches = new HashMap<Double, BoundingBox>();
		
		for (BoundingBox comp : bb) {
			Vec3d c = comp.aabb.getCenter();
			double distance = c.distanceTo(rayStart);
			
			RayTraceResult ray = comp.aabb.calculateIntercept(rayStart, rayStart.add(rayDirection.scale(maxDistance)));
			
			if (ray != null) {
				matches.put(distance, comp);
			}
		}
		
		if (!matches.keySet().isEmpty()) {
			BoundingBox matchComp = matches.get(Collections.max(matches.keySet()));
			
			return matchComp.name;
		}
		
		return null;
	}
}
