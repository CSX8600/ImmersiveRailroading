package cam72cam.immersiverailroading.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import cam72cam.immersiverailroading.library.RenderComponentType;
import cam72cam.immersiverailroading.model.obj.OBJModel;
import net.java.games.input.Component.Identifier.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class BBModel {
	public List<BoundingBox> boundingBoxes = new ArrayList<BoundingBox>();
	
	public BBModel (OBJModel model) {
		for (String comp : model.groups()) {
			if (!comp.equalsIgnoreCase("defaultName")) {
				for (RenderComponentType type : RenderComponentType.values()) {
					if (Pattern.matches(type.regex.replace("#SIDE#", "").replaceAll("#ID#", "").replaceAll("#POS#", ""), comp) && type.receiveRayCast) {
						List<String> cmp = Lists.newArrayList(comp);
						this.boundingBoxes.add(new BoundingBox(comp, new AxisAlignedBB(model.minOfGroup(cmp), model.maxOfGroup(cmp))));
						break;
					}
					
				}
			}
		}
	}
	
	public static class BoundingBox {
		public String name;
		public AxisAlignedBB aabb;
		
		public BoundingBox (String name, AxisAlignedBB aabb) {
			this.name = name;
			this.aabb = aabb;
		}
	}
}
