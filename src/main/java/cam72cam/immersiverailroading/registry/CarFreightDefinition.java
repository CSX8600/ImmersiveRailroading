package cam72cam.immersiverailroading.registry;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.entity.CarFreight;
import net.minecraft.world.World;

public class CarFreightDefinition extends EntityRollingStockDefinition {

	private int numSlots;
	private int width;
	private List<String> validCargo;
	
	public CarFreightDefinition(String defID, JsonObject data) throws Exception {
		super(defID, data);
		
		// Handle null data
		if (validCargo == null) {
			validCargo = new ArrayList<String>();
		}
	}
	
	@Override
	public void parseJson(JsonObject data) throws Exception {
		super.parseJson(data);
		JsonObject freight = data.get("freight").getAsJsonObject();
		this.numSlots = (int)Math.ceil(freight.get("slots").getAsInt() * internal_scale);
		this.width = (int)Math.ceil(freight.get("width").getAsInt() * internal_scale);
		this.validCargo = new ArrayList<String>();
		for (JsonElement el : freight.get("cargo").getAsJsonArray()) {
			validCargo.add(el.getAsString());
		}
	}
	
	public EntityRollingStock instance(World world) {
		return new CarFreight(world, defID);
	}
	
	public int getInventorySize(Gauge gauge) {
		return (int) Math.ceil(numSlots * gauge.scale());
	}

	public int getInventoryWidth(Gauge gauge) {
		return (int) Math.ceil(width * gauge.scale());
	}
}
