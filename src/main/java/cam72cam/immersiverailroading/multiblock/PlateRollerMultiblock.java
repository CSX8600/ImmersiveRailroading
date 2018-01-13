package cam72cam.immersiverailroading.multiblock;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.library.GuiTypes;
import cam72cam.immersiverailroading.net.MultiblockSelectCraftPacket;
import cam72cam.immersiverailroading.tile.TileMultiblock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class PlateRollerMultiblock extends Multiblock {
	public static final String NAME = "PLATE_MACHINE";
	private static final BlockPos render = new BlockPos(2,0,0);
	private static final BlockPos crafter = new BlockPos(2,0,14);
	private static final BlockPos input = new BlockPos(2,0,0);
	private static final BlockPos output = new BlockPos(2,0,29);
	private static final BlockPos power = new BlockPos(1,4,14);
	
	private static MultiblockComponent[][][] componentGenerator() {
		MultiblockComponent[][][] result = new MultiblockComponent[30][][];
		
		MultiblockComponent[] bed = new MultiblockComponent[] {
				L_ENG(), S_SCAF(), S_SCAF(), S_SCAF(), L_ENG()
		};
		MultiblockComponent[] mid = new MultiblockComponent[] {
				L_ENG(), AIR, AIR, AIR, L_ENG()
		};
		MultiblockComponent[] top = new MultiblockComponent[] {
				H_ENG(), H_ENG(), H_ENG(), H_ENG(), H_ENG()
		};
		for (int i = 0; i < 30; i ++) {
			if (i >= 11 && i <= 18) {
				if (i >= 13 && i <=16) {
					if (i == 14) {
						result[i] = new MultiblockComponent[][] { bed, mid, top, { AIR, L_ENG(), L_ENG(), L_ENG(), AIR}, { AIR, H_ENG(), AIR, AIR, AIR } };
					} else {
						result[i] = new MultiblockComponent[][] { bed, mid, top, { AIR, L_ENG(), L_ENG(), L_ENG(), AIR} };
					}
				} else {
					result[i] = new MultiblockComponent[][] { bed, mid, top };
				}
			} else {
				result[i] = new MultiblockComponent[][] { bed };
			}
		}
		
		return result;
	}

	public PlateRollerMultiblock() {
		super(NAME, componentGenerator());
	}
	
	@Override
	public BlockPos placementPos() {
		return new BlockPos(2, 0, 0);
	}

	@Override
	protected MultiblockInstance newInstance(World world, BlockPos origin, Rotation rot) {
		return new PlateRollerInstance(world, origin, rot);
	}
	public class PlateRollerInstance extends MultiblockInstance {
		
		public PlateRollerInstance(World world, BlockPos origin, Rotation rot) {
			super(world, origin, rot);
		}

		@Override
		public boolean onBlockActivated(EntityPlayer player, EnumHand hand, BlockPos offset) {
			if (!player.isSneaking()) {
				ItemStack held = player.getHeldItem(hand);
				if (held == null) {
					TileMultiblock outputTe = getTile(output);
					if (outputTe == null) {
						return false;
					}
					
					if (!(outputTe.getContainer().getStackInSlot(0) == null)) {
						if (!world.isRemote) {
							player.setHeldItem(hand, outputTe.getContainer().getStackInSlot(0));
							outputTe.getContainer().setStackInSlot(0, null);
						}
						return true;
					}
				} else if (held.isItemEqual(steelBlock())) {
					TileMultiblock inputTe = getTile(input);
					if (inputTe == null) {
						return false;
					}
					if (inputTe.getContainer().getStackInSlot(0) == null) {
						if (!world.isRemote) {
							ItemStack inputStack = held.copy();
							inputStack.stackSize = (1);
							inputTe.getContainer().setStackInSlot(0, inputStack);
							held.stackSize -= (1);
							player.setHeldItem(hand, held);
						}
					}
					return true;
				}
				
				if (world.isRemote) {
					BlockPos pos = getPos(offset);
					player.openGui(ImmersiveRailroading.instance, GuiTypes.PLATE_ROLLER.ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean isRender(BlockPos offset) {
			return render.equals(offset);
		}

		@Override
		public int getInvSize(BlockPos offset) {
			return offset.equals(input) || offset.equals(output) ? 1 : 0;
		}

		@Override
		public void tick(BlockPos offset) {
			if (!offset.equals(crafter)) {
				return;
			}
			TileMultiblock craftingTe = getTile(crafter);
			if (craftingTe == null) {
				return;
			}
			
			TileMultiblock powerTe = getTile(power);
			if (powerTe == null) {
				return;
			}
			
			TileMultiblock inputTe = getTile(input);
			if (inputTe == null) {
				return;
			}
			
			TileMultiblock outputTe = getTile(output);
			if (outputTe == null) {
				return;
			}
			
			if (!hasPower()) {
				return;
			}
			
			if (world.isRemote) {
				if (craftingTe.getRenderTicks() % 10 == 0 && craftingTe.getCraftProgress() != 0) {
					world.playSound(craftingTe.getPos().getX(), craftingTe.getPos().getY(), craftingTe.getPos().getZ(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1.0f, 0.2f, false);
				}
				return;
			}
			
			// Decrement craft progress down to 0
			if (craftingTe.getCraftProgress() != 0) {
				IEnergyStorage energy = powerTe.getCapability(CapabilityEnergy.ENERGY, null);
				energy.extractEnergy(32, false);
				craftingTe.setCraftProgress(Math.max(0, craftingTe.getCraftProgress() - 1));
			}
			
			float progress = craftingTe.getCraftProgress();
			
			ItemStack input = inputTe.getContainer().getStackInSlot(0);
			ItemStack output = outputTe.getContainer().getStackInSlot(0);
			
			
			if (progress == 0) {
				// Try to start crafting
				if (input != null && input.isItemEqual(steelBlock()) && output == null && !(craftingTe.getCraftItem() == null)) {
					input.stackSize = (input.stackSize - 1);
					inputTe.getContainer().setStackInSlot(0, input);;
					progress = 100;
					craftingTe.setCraftProgress(100);
				}
			}
			
			if (progress == 1) {
				// Stop crafting
				outputTe.getContainer().setStackInSlot(0, craftingTe.getCraftItem().copy());
			}
		}

		@Override
		public boolean canInsertItem(BlockPos offset, int slot, ItemStack stack) {
			return offset.equals(input) && stack.isItemEqual(steelBlock());
		}

		@Override
		public boolean isOutputSlot(BlockPos offset, int slot) {
			return offset.equals(output);
		}

		@Override
		public int getSlotLimit(BlockPos offset, int slot) {
			return offset.equals(input) || offset.equals(output) ? 1 : 0;
		}

		@Override
		public boolean canRecievePower(BlockPos offset) {
			return offset.equals(power);
		}

		public boolean hasPower() {
			TileMultiblock powerTe = getTile(power);
			if (powerTe == null) {
				return false;
			}
			IEnergyStorage energy = powerTe.getCapability(CapabilityEnergy.ENERGY, null);
			return energy.getEnergyStored() > 32;
			
		}
		
		public ItemStack steelBlock() {
			return new ItemStack(IEContent.blockStorage,1, BlockTypes_MetalsAll.STEEL.getMeta());
		}
		
		public void setCraftItem(ItemStack stack) {
			ImmersiveRailroading.net.sendToServer(new MultiblockSelectCraftPacket(getPos(crafter), stack));
		}

		public ItemStack getCraftItem() {
			TileMultiblock craftingTe = getTile(crafter);
			if (craftingTe == null) {
				return null;
			}
			return craftingTe.getCraftItem();
		}
	}
}
