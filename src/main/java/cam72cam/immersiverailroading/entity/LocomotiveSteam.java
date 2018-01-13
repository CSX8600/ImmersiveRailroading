package cam72cam.immersiverailroading.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cam72cam.immersiverailroading.Config;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.GuiTypes;
import cam72cam.immersiverailroading.library.RenderComponentType;
import cam72cam.immersiverailroading.model.RenderComponent;
import cam72cam.immersiverailroading.registry.LocomotiveSteamDefinition;
import cam72cam.immersiverailroading.util.BurnUtil;
import cam72cam.immersiverailroading.util.FluidQuantity;
import cam72cam.immersiverailroading.util.VecUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.Vec3d;
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
	private boolean gonnaExplode;
	private double driverDiameter;
	
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
	
	@Override
	public void readSpawnData(ByteBuf additionalData) {
		super.readSpawnData(additionalData);

		List<RenderComponent> driving = this.getDefinition().getComponents(RenderComponentType.WHEEL_DRIVER_X, gauge);
		if (driving != null) {
			for (RenderComponent driver : driving) {
				driverDiameter = Math.max(driverDiameter, driver.height());
			}
		}
		driving = this.getDefinition().getComponents(RenderComponentType.WHEEL_DRIVER_REAR_X, gauge);
		if (driving != null) {
			for (RenderComponent driver : driving) {
				driverDiameter = Math.max(driverDiameter, driver.height());
			}
		}
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
	
	private double getPhase(int spikes, float offsetDegrees) {
		if (driverDiameter == 0) {
			return 0;
		}
		double circumference = (driverDiameter * Math.PI);
		double phase = (this.distanceTraveled % circumference)/circumference;
		phase = Math.abs(Math.cos(phase*Math.PI*spikes + Math.toRadians(offsetDegrees)));
		return phase;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();

		if (worldObj.isRemote) {
			// Particles
			
			if (!Config.particlesEnabled) {
				return;
			}
			
			Vec3d fakeMotion = VecUtil.fromYaw(this.getCurrentSpeed().minecraft(), this.rotationYaw);
			
			List<RenderComponent> smokes = this.getDefinition().getComponents(RenderComponentType.PARTICLE_CHIMNEY_X, gauge);
			if (smokes != null) {
				double phase = getPhase(4, 0);
				//System.out.println(phase);
				for (RenderComponent smoke : smokes) {
					Vec3d particlePos = this.getPositionVector().add(VecUtil.rotateYaw(smoke.center(), this.rotationYaw + 180)).addVector(0, 0.35 * gauge.scale(), 0);
					particlePos = particlePos.subtract(fakeMotion);
					if (this.ticksExisted % 1 == 0 ) {
						float darken = 0;
						float thickness = Math.abs(this.getThrottle())/2;
						for (int i : this.getBurnTime().values()) {
							darken += i >= 1 ? 1 : 0;
						}
						if (darken == 0) {
							break;
						}
						darken /= this.getInventorySize() - 2.0;
						darken *= 0.5;
						
						int lifespan = (int) (200 * (1 + Math.abs(this.getThrottle())) / 2);
						//lifespan *= size;
						
						float verticalSpeed = (0.5f + Math.abs(this.getThrottle())) * ((float)smoke.width() / 0.6f);
						
						double size = smoke.width();
						if (phase != 0 && Math.abs(this.getThrottle()) > 0.01 && Math.abs(this.getCurrentSpeed().metric()) < 30) {
							double phaseSpike = Math.pow(phase, 8);
							size *= 1 + phaseSpike*1.5;
							verticalSpeed *= 1 + phaseSpike/2;
						}
						
						particlePos = particlePos.subtract(fakeMotion);
						
						EntitySmokeParticle sp = new EntitySmokeParticle(worldObj, lifespan , darken, thickness, size);
						sp.setPosition(particlePos.xCoord, particlePos.yCoord, particlePos.zCoord);
						sp.setVelocity(fakeMotion.xCoord, fakeMotion.yCoord + verticalSpeed, fakeMotion.zCoord);
						worldObj.spawnEntityInWorld(sp);
					}
				}
			}
			
			List<RenderComponent> pistons = this.getDefinition().getComponents(RenderComponentType.PISTON_ROD_SIDE, gauge);
			double csm = Math.abs(this.getCurrentSpeed().metric());
			if (pistons != null && csm > 0.1 && csm  < 20 && this.getBoilerPressure() > 0) {
				for (RenderComponent piston : pistons) {
					float phaseOffset = 0;
					switch (piston.side) {
					case "LEFT":
						phaseOffset = 45+90;
						break;
					case "RIGHT":
						phaseOffset = -45+90;
						break;
					case "LEFT_FRONT":
						phaseOffset = 45+90;
						break;
					case "RIGHT_FRONT":
						phaseOffset = -45+90;
						break;
					case "LEFT_REAR":
						phaseOffset = 90;
						break;
					case "RIGHT_REAR":
						phaseOffset = 0;
						break;
					default:
						continue;
					}
					
					double phase = this.getPhase(2, phaseOffset);
					double phaseSpike = Math.pow(phase, 4);
					
					if (phaseSpike < 0.6) {
						continue;
					}
					
					
					Vec3d particlePos = this.getPositionVector().add(VecUtil.rotateYaw(piston.min(), this.rotationYaw + 180)).addVector(0, 0.35 * gauge.scale(), 0);
					EntitySmokeParticle sp = new EntitySmokeParticle(worldObj, 80, 0, 0.6f, 0.2);
					sp.setPosition(particlePos.xCoord, particlePos.yCoord, particlePos.zCoord);
					double accell = (piston.side.contains("RIGHT") ? 1 : -1) * 0.3;
					Vec3d sideMotion = fakeMotion.add(VecUtil.fromYaw(accell, this.rotationYaw+90));
					sp.setVelocity(sideMotion.xCoord, sideMotion.yCoord+0.01, sideMotion.zCoord);
					worldObj.spawnEntityInWorld(sp);
				}
			}
			
			List<RenderComponent> steams = this.getDefinition().getComponents(RenderComponentType.PRESSURE_VALVE_X, gauge);
			if (steams != null && this.getBoilerPressure() == this.getDefinition().getMaxPSI(gauge)) {
				for (RenderComponent steam : steams) {
					Vec3d particlePos = this.getPositionVector().add(VecUtil.rotateYaw(steam.center(), this.rotationYaw + 180)).addVector(0, 0.35 * gauge.scale(), 0);
					particlePos = particlePos.subtract(fakeMotion);
					EntitySmokeParticle sp = new EntitySmokeParticle(worldObj, 40, 0, 0.2f, steam.width());
					sp.setPosition(particlePos.xCoord, particlePos.yCoord, particlePos.zCoord);
					sp.setVelocity(fakeMotion.xCoord, fakeMotion.yCoord + 0.2, fakeMotion.zCoord);
					worldObj.spawnEntityInWorld(sp);
				}
			}
			
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
							ItemStack cargoStack = this.cargoItems.getStackInSlot(slot);
							ItemStack tenderStack = tender.cargoItems.getStackInSlot(tenderSlot);
							if (cargoStack == null || tenderStack == null) {
								continue;
							}
							if (cargoStack.isItemEqual(tenderStack)) {
								if (cargoStack.getMaxStackSize() > tenderStack.stackSize) {
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
				if (stack == null || stack.stackSize <= 0 || !TileEntityFurnace.isItemFuel(stack)) {
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
			this.gonnaExplode = true;
			if (Config.explosionsEnabled) {
				for (int i = 0; i < 5; i++) {
					worldObj.createExplosion(this, this.posX, this.posY, this.posZ, boilerPressure/8, true);
				}
			}
			worldObj.removeEntity(this);
		}
	}
	
	@Override
	public void setDead() {
		if (this.gonnaExplode) {
			this.isDead = true;
			return;
		}
		// Don't do drops if from explosion
		super.setDead();
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
