package cam72cam.immersiverailroading.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cam72cam.immersiverailroading.Config;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.GuiTypes;
import cam72cam.immersiverailroading.registry.LocomotiveSteamDefinition;
import cam72cam.immersiverailroading.util.BurnUtil;
import cam72cam.immersiverailroading.util.FluidQuantity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;

public class LocomotiveSteam extends Locomotive {
	// PSI
	private static DataParameter<Float> BOILER_PRESSURE = EntityDataManager.createKey(LocomotiveSteam.class, DataSerializers.FLOAT);
	// Celsius
	private static DataParameter<Float> BOILER_TEMPERATURE = EntityDataManager.createKey(LocomotiveSteam.class, DataSerializers.FLOAT);
	// Map<Slot, TicksToBurn>
	private static DataParameter<String> BURN_TIME = EntityDataManager.createKey(LocomotiveSteam.class, DataSerializers.STRING);
	private static DataParameter<String> BURN_MAX = EntityDataManager.createKey(LocomotiveSteam.class, DataSerializers.STRING);
	
	public LocomotiveSteam(World world) {
		this(world, null);
	}

	public LocomotiveSteam(World world, String defID) {
		super(world, defID);
		
		this.getDataManager().register(BOILER_PRESSURE, 0f);
		this.getDataManager().register(BOILER_TEMPERATURE, 0f);
		this.getDataManager().register(BURN_TIME, new NBTTagCompound().toString());
		this.getDataManager().register(BURN_MAX, new NBTTagCompound().toString());
	}

	public LocomotiveSteamDefinition getDefinition() {
		return super.getDefinition(LocomotiveSteamDefinition.class);
	}

	@Override
	public GuiTypes guiType() {
		return GuiTypes.STEAM_LOCOMOTIVE;
	}
	
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setFloat("boiler_temperature", getBoilerTemperature());
		nbttagcompound.setFloat("boiler_psi", getBoilerPressure());
		nbttagcompound.setString("burn_time", dataManager.get(BURN_TIME));
		nbttagcompound.setString("burn_max", dataManager.get(BURN_MAX));
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		setBoilerTemperature(nbttagcompound.getFloat("boiler_temperature"));
		setBoilerPressure(nbttagcompound.getFloat("boiler_psi"));
		dataManager.set(BURN_TIME, nbttagcompound.getString("burn_time"));
		dataManager.set(BURN_MAX, nbttagcompound.getString("burn_max"));
	}
	
	public float getBoilerTemperature() {
		return this.dataManager.get(BOILER_TEMPERATURE);
	}
	private void setBoilerTemperature(float temp) {
		this.dataManager.set(BOILER_TEMPERATURE, temp);
	}
	
	public float getBoilerPressure() {
		return this.dataManager.get(BOILER_PRESSURE);
	}
	private void setBoilerPressure(float temp) {
		this.dataManager.set(BOILER_PRESSURE, temp);
	}
	
	private NBTTagCompound mapToNBT(Map<Integer, Integer> map) {
		NBTTagCompound data = new NBTTagCompound();
		for (Integer slot : map.keySet()) {
			data.setInteger("" + slot, map.get(slot));
		}
		return data;
	}
	private Map<Integer, Integer> NBTtoMap(NBTTagCompound nbt) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (String key : nbt.getKeySet()) {
			map.put(Integer.parseInt(key), nbt.getInteger(key));
		}
		return map;
	}
	
	public Map<Integer, Integer> getBurnTime() {
		try {
			return NBTtoMap(JsonToNBT.getTagFromJson(this.dataManager.get(BURN_TIME)));
		} catch (NBTException e) {
			e.printStackTrace();
			return null;
		}
	}
	private void setBurnTime(Map<Integer, Integer> burnTime) {
		this.dataManager.set(BURN_TIME, mapToNBT(burnTime).toString());
	}
	public Map<Integer, Integer> getBurnMax() {
		try {
			return NBTtoMap(JsonToNBT.getTagFromJson(this.dataManager.get(BURN_MAX)));
		} catch (NBTException e) {
			e.printStackTrace();
			return null;
		}
	}
	private void setBurnMax(Map<Integer, Integer> burnMax) {
		this.dataManager.set(BURN_MAX, mapToNBT(burnMax).toString());
	}
	
	
	@Override
	protected int getAvailableHP() {
		if (Config.ModelFuelRequired == false && this.gauge == Gauge.MODEL) {
			return this.getDefinition().getHorsePower(gauge);
		}
		return (int) (this.getDefinition().getHorsePower(gauge) * Math.pow(this.getBoilerPressure() / this.getDefinition().getMaxPSI(gauge), 3));
	}
	
	
	@Override
	public void onDissassemble() {
		super.onDissassemble();
		this.setBoilerTemperature(0);
		this.setBoilerPressure(0);
		
		Map<Integer, Integer> burnTime = getBurnTime();
		for (Integer slot : burnTime.keySet()) {
			burnTime.put(slot, 0);
		}
		setBurnTime(burnTime);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();

		if (worldObj.isRemote) {
			return;
		}
		
		if (!this.isBuilt()) {
			return;
		}
		
		if (this.getCoupled(CouplerType.BACK) instanceof Tender) {
			Tender tender = (Tender) getCoupled(CouplerType.BACK);

			// Only drain 10mb at a time from the tender
			int desiredDrain = 10;
			if (getTankCapacity().MilliBuckets() - getServerLiquidAmount() >= 10) {
				FluidUtil.tryFluidTransfer(this.theTank, tender.theTank, desiredDrain, true);
			}
			
			if (this.ticksExisted % 20 == 0) {
				// Top off stacks
				for (int slot = 0; slot < this.cargoItems.getSlots()-2; slot ++) {
					if (BurnUtil.getBurnTime(this.cargoItems.getStackInSlot(slot)) != 0) {
						for (int tenderSlot = 0; tenderSlot < tender.cargoItems.getSlots(); tenderSlot ++) {
							if (this.cargoItems.getStackInSlot(slot).isItemEqual(tender.cargoItems.getStackInSlot(tenderSlot))) {
								if (this.cargoItems.getStackInSlot(slot).getMaxStackSize() > this.cargoItems.getStackInSlot(slot).stackSize) {
									ItemStack extracted = tender.cargoItems.extractItem(tenderSlot, 1, false);
									this.cargoItems.insertItem(slot, extracted, false);
								}
							}
						}
					}
				}
			}
		}
		
		// Water to steam
		if (getBoilerTemperature() >= 100) {
			if (getTankCapacity().MilliBuckets() > 0) {
				if (rand.nextInt(100) == 0) {
					int outputHorsepower = (int) Math.abs(getThrottle() * getAvailableHP());
					theTank.drain(outputHorsepower * 10 / this.getDefinition().getHorsePower(gauge), true);
				}
			}
		}
		
		Map<Integer, Integer> burnTime = getBurnTime();
		Map<Integer, Integer> burnMax = getBurnMax();
		Boolean changedBurnTime = false;
		Boolean changedBurnMax = false;
		
		float waterLevelMB = this.getLiquidAmount();
		float boilerTemperature = this.getBoilerTemperature();
		float boilerPressure = this.getBoilerPressure();
		
		// TODO actual gas and fluid temp calculations
		
		for (int slot = 0; slot < this.cargoItems.getSlots()-2; slot ++) {
			if (waterLevelMB == 0) {
				// Don't burn if completely out of water
				// Prevents beginner mistakes
				continue;
			}
			int time = burnTime.containsKey(slot) ? burnTime.get(slot) : 0;
			if (time <= 0) {
				ItemStack stack = this.cargoItems.getStackInSlot(slot);
				if (stack.stackSize <= 0 || !TileEntityFurnace.isItemFuel(stack)) {
					continue;
				}
				time = (int) (BurnUtil.getBurnTime(stack) * 1/gauge.scale());
				burnTime.put(slot, time);
				burnMax.put(slot, time);
				stack.stackSize = (stack.stackSize-1);
				this.cargoItems.setStackInSlot(slot, stack);
				changedBurnMax = true;
			} else {
				burnTime.put(slot, time - 1);
			}
			changedBurnTime = true;
			if (boilerTemperature < 100 || waterLevelMB < this.getTankCapacity().MilliBuckets() * 0.75) {
				boilerTemperature += 100/waterLevelMB * Math.sqrt(gauge.scale());
			}
			if (boilerTemperature >= 100) {
				boilerPressure += 100/waterLevelMB * Math.sqrt(gauge.scale());
				if (rand.nextInt(10) == 0) {
					waterLevelMB -= 1;
				}
			}
		}
		
		if (!changedBurnTime) {
			if (boilerPressure <= 0 || boilerTemperature > 100) {
				//cooling firebox
				boilerTemperature = (float) Math.max(0, boilerTemperature-0.05);
			} else {
				// cooling gas
				boilerPressure = (float) Math.max(0, boilerPressure - 0.05);
			}
		}
		
		// This can go away once adding water drops the boiler pressure/temperature
		if (boilerTemperature > 100 && gauge == Gauge.MODEL) {
			boilerTemperature = 100;
		}
		
		float throttle = Math.abs(getThrottle());
		if (throttle != 0 && boilerPressure > 0) {
			boilerPressure = Math.max(0, boilerPressure - throttle * (this.cargoItems.getSlots()-2) * 100/waterLevelMB);
		}
		
		if (boilerPressure > this.getDefinition().getMaxPSI(gauge)) {
			// TODO hissing and steam of pressure relief valve
			boilerPressure = this.getDefinition().getMaxPSI(gauge);
		}
		
		if (changedBurnTime) {
			setBurnTime(burnTime);
			theTank.drain(this.getLiquidAmount() - (int)waterLevelMB, true);
		}
		if (changedBurnMax) {
			setBurnMax(burnMax);
		}
		setBoilerTemperature(boilerTemperature);
		setBoilerPressure(boilerPressure);
		
		if (boilerPressure > this.getDefinition().getMaxPSI(gauge) * 1.1 || (boilerPressure > this.getDefinition().getMaxPSI(gauge) * 0.5 && boilerTemperature > 150)) {
			// 10% over max pressure OR
			// Half max pressure and high boiler temperature
			//EXPLODE
			if (Config.explosionsEnabled) {
				worldObj.createExplosion(this, this.posX, this.posY, this.posZ, boilerPressure, true);
			}
			worldObj.removeEntity(this);
		}
	}

	@Override
	public int getInventorySize() {
		return this.getDefinition().getInventorySize(gauge) + 2;
	}
	
	public int getInventoryWidth() {
		return this.getDefinition().getInventoryWidth(gauge);
	}
	
	@Override
	protected int[] getContainerInputSlots() {
		return new int[] { getInventorySize()-2 };
	}
	@Override
	protected int[] getContainertOutputSlots() {
		return new int[] { getInventorySize()-1 };
	}

	@Override
	public FluidQuantity getTankCapacity() {
		return this.getDefinition().getTankCapacity(gauge);
	}

	@Override
	public List<Fluid> getFluidFilter() {
		List<Fluid> filter = new ArrayList<Fluid>();
		filter.add(FluidRegistry.WATER);
		return filter;
	}
}