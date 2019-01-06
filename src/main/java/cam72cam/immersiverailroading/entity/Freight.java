package cam72cam.immersiverailroading.entity;

import javax.annotation.Nullable;

import cam72cam.immersiverailroading.Config.ConfigBalance;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.inventory.FilteredStackHandler;
import cam72cam.immersiverailroading.library.GuiTypes;
import cam72cam.immersiverailroading.library.StockDeathType;
import cam72cam.immersiverailroading.registry.FreightDefinition;
import cam72cam.immersiverailroading.util.VecUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public abstract class Freight extends EntityCoupleableRollingStock {
	protected FilteredStackHandler cargoItems = new FilteredStackHandler(0) {
        @Override
        protected void onContentsChanged(int slot) {
            // We need to tell the tile entity that something has changed so
            // that the chest contents is persisted
        	Freight.this.onInventoryChanged();
        }
    };
    
	protected static DataParameter<Integer> CARGO_ITEMS = EntityDataManager.createKey(Freight.class, DataSerializers.VARINT);
	protected static DataParameter<Integer> PERCENT_FULL = EntityDataManager.createKey(Freight.class, DataSerializers.VARINT);

	public Freight(World world, String defID) {
		super(world, defID);
		
		this.getDataManager().register(CARGO_ITEMS, 0);
		this.getDataManager().register(PERCENT_FULL, 0);
	}
	
	protected void onInventoryChanged() {
		if (!worldObj.isRemote) {
			handleMass();
		}
	}

	public abstract int getInventorySize();

	public boolean showCurrentLoadOnly() {
		return this.getDefinition().shouldShowCurrentLoadOnly();
	}
	
	@Override
	public FreightDefinition getDefinition() {
		return this.getDefinition(FreightDefinition.class);
	}

	/*
	 * 
	 * EntityRollingStock Overrides
	 */
	
	@Override
	public void onAssemble() {
		super.onAssemble();
		this.cargoItems.setSize(this.getInventorySize());
	}
	
	@Override
	public void onDissassemble() {
		super.onDissassemble();
		
		if (!worldObj.isRemote) {		
			for (int slot = 0; slot < cargoItems.getSlots(); slot++) {
				ItemStack itemstack = cargoItems.getStackInSlot(slot);
				if (itemstack != null && itemstack.stackSize != 0) {
					Vec3d pos = this.getPositionVector().add(VecUtil.fromWrongYaw(4, this.rotationYaw+90));
					worldObj.spawnEntityInWorld(new EntityItem(this.worldObj, pos.xCoord, pos.yCoord, pos.zCoord, itemstack.copy()));
					itemstack.stackSize = 0;
				}
			}
		}
		
		this.cargoItems.setSize(0);
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand) {
		if ((super.processInitialInteract(player, stack, hand))) {
			return true;
		}
		
		if (!this.isBuilt()) {
			return false;
		}

		// See ItemLead.attachToFence
		double dist = 10.0D;
		double i = player.posX;
		double j = player.posY;
		double k = player.posZ;

		for (EntityLiving entityliving : worldObj.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB((double) i - dist, (double) j - 7.0D,
				(double) k - dist, (double) i + dist, (double) j + dist, (double) k + dist))) {
			if (entityliving.getLeashed() && entityliving.getLeashedToEntity() == player) {
				if (canFitPassenger(entityliving) && this.getDefinition().acceptsLivestock()) {
					entityliving.clearLeashed(true, !player.isCreative());
					this.addStaticPassenger(entityliving, player.getPositionVector());
					return true;
				}
			}
		}

		if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == Items.LEAD) {
			EntityLiving passenger = this.removeStaticPasssenger(player.getPositionVector(), false);
			if (passenger != null) {
				if (passenger.canBeLeashedTo(player)) {
					passenger.setLeashedToEntity(player, true);
					player.getHeldItemMainhand().stackSize -= 1;
					return true;
				}
				return true;
			}
		}
		
		// I don't believe the positions are used
		if (guiType() != null) {
			player.openGui(ImmersiveRailroading.instance, guiType().ordinal(), worldObj, this.getEntityId(), 0, 0);
			return true;
		}
		return false;
	}

	protected GuiTypes guiType() {
		return GuiTypes.FREIGHT;
	}

	/**
	 * Handle mass depending on item count
	 */
	protected void handleMass() {
		int itemInsideCount = 0;
		int stacksWithStuff = 0;
		for (int slot = 0; slot < cargoItems.getSlots(); slot++) {
			itemInsideCount += cargoItems.getStackInSlot(slot) == null ? 0 : cargoItems.getStackInSlot(slot).stackSize;
			stacksWithStuff += cargoItems.getStackInSlot(slot) == null ? 0 : 1;
		}
		this.getDataManager().set(CARGO_ITEMS, itemInsideCount);
		this.getDataManager().set(PERCENT_FULL, stacksWithStuff * 100 / this.getInventorySize());
	}
	
	public int getPercentCargoFull() {
		return this.getDataManager().get(PERCENT_FULL);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setTag("items", cargoItems.serializeNBT());
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		ItemStackHandler temp = new ItemStackHandler();
		temp.deserializeNBT((NBTTagCompound) nbttagcompound.getTag("items"));
		cargoItems.setSize(this.getInventorySize());
		for (int slot = 0; slot < temp.getSlots(); slot ++) {
			if (slot < cargoItems.getSlots()) {
				cargoItems.setStackInSlot(slot, temp.getStackInSlot(slot));
			} else {
				worldObj.spawnEntityInWorld(new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, temp.getStackInSlot(slot)));
			}
		}
		handleMass();
		initContainerFilter();
	}
	
	@Override
	public void readSpawnData(ByteBuf additionalData) {
		super.readSpawnData(additionalData);
		initContainerFilter();
	}
	
	protected void initContainerFilter() {
		
	}
	
	@Override
	public void onDeath(StockDeathType type) {
		super.onDeath(type);
		
		if (worldObj.isRemote) {
			return;
		}
		
		for (int slot = 0; slot < cargoItems.getSlots(); slot++) {
			ItemStack itemstack = cargoItems.getStackInSlot(slot);
			if (itemstack != null && itemstack.stackSize != 0) {
				worldObj.spawnEntityInWorld(new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, itemstack.copy()));
				itemstack.stackSize = 0;
			}
		}
	}
	
	@Override
	public double getWeight() {
		double fLoad = ConfigBalance.blockWeight * this.getDataManager().get(CARGO_ITEMS);
		fLoad = fLoad + super.getWeight();
		return fLoad;
	}

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) cargoItems;
        }
        return super.getCapability(capability, facing);
    }
}