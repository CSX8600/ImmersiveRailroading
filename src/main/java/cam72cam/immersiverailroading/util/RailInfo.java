package cam72cam.immersiverailroading.util;

import java.util.ArrayList;
import java.util.List;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.items.ItemTrackBlueprint;
import cam72cam.immersiverailroading.items.nbt.ItemGauge;
import cam72cam.immersiverailroading.library.SwitchState;
import cam72cam.immersiverailroading.library.ChatText;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.TrackDirection;
import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.library.TrackPositionType;
import cam72cam.immersiverailroading.track.BuilderBase;
import cam72cam.immersiverailroading.track.BuilderCrossing;
import cam72cam.immersiverailroading.track.BuilderSlope;
import cam72cam.immersiverailroading.track.BuilderStraight;
import cam72cam.immersiverailroading.track.BuilderSwitch;
import cam72cam.immersiverailroading.track.BuilderTurn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class RailInfo {
	public BlockPos position;
	public World world;
	public EnumFacing facing;
	public TrackItems type;
	public TrackDirection direction;
	public int length;
	public int quarter;
	public int quarters;
	public Gauge gauge;
	public Vec3d placementPosition;
	public ItemStack railBed;
	public ItemStack railBedFill;

	// Used for tile rendering only
	public boolean snowRenderFlagDirty = false;
	public SwitchState switchState = SwitchState.NONE;
	
	
	public RailInfo(BlockPos position, World world, EnumFacing facing, TrackItems type, TrackDirection direction, int length, int quarter, int quarters, Gauge gauge, Vec3d placementPosition, ItemStack railBed, ItemStack railBedFill) {
		this.position = position;
		this.world = world;
		this.facing = facing;
		this.type = type;
		this.direction = direction;
		this.length = length;
		this.quarter = quarter;
		this.quarters = quarters;
		this.gauge = gauge;
		this.placementPosition = placementPosition;
		this.railBed = railBed;
		this.railBedFill = railBedFill;
	}
	
	public RailInfo(ItemStack stack, World worldIn, float yawHead, BlockPos pos, float hitX, float hitY, float hitZ) {
		position = pos;
		type = ItemTrackBlueprint.getType(stack);
		length = ItemTrackBlueprint.getLength(stack);
		quarters = ItemTrackBlueprint.getQuarters(stack);
		gauge = ItemGauge.get(stack);
		railBed = ItemTrackBlueprint.getBed(stack);
		railBedFill = ItemTrackBlueprint.getBedFill(stack);
		world = worldIn;
		TrackPositionType posType = ItemTrackBlueprint.getPosType(stack);
		
		yawHead = yawHead % 360 + 360;
		direction = (yawHead % 90 < 45) ? TrackDirection.RIGHT : TrackDirection.LEFT;
		//quarter = MathHelper.floor((yawHead % 90f) /(90)*4);
		float yawPartial = (yawHead+3600) % 90f;
		if (direction == TrackDirection.LEFT) {
			yawPartial = 90-yawPartial;
		}
		if (yawPartial < 15) {
			quarter = 0;
		} else if (yawPartial < 30) {
			quarter = 1;
		} else {
			quarter = 2;
		}
		
		facing = EnumFacing.fromAngle(yawHead);

		
		switch(posType) {
		case FIXED:
			hitX = 0.5f;
			hitZ = 0.5f;
			break;
		case PIXELS:
			hitX = ((int)(hitX * 16)) / 16f;
			hitZ = ((int)(hitZ * 16)) / 16f;
			break;
		case SMOOTH:
			// NOP
			break;
		}
		
		placementPosition = new Vec3d(pos).addVector(hitX, 0, hitZ);
		
		if (BlockUtil.canBeReplaced(world, pos.down(), true)) {
			pos = pos.down();
		}
	}
	
	public RailInfo clone() {
		RailInfo c = new RailInfo(position, world, facing, type, direction, length, quarter, quarters, gauge, placementPosition, railBed, railBedFill);
		return c;
	}
	
	public BuilderBase getBuilder(BlockPos pos) {
		switch (type) {
		case STRAIGHT:
			return new BuilderStraight(this, pos);
		case CROSSING:
			return new BuilderCrossing(this, pos);
		case SLOPE:
			return new BuilderSlope(this, pos);
		case TURN:
			return new BuilderTurn(this, pos);
		case SWITCH:
			return new BuilderSwitch(this, pos);
		default:
			return null;
		}
	}
	
	private BuilderBase builder;
	public BuilderBase getBuilder() {
		if (builder == null) {
			builder = getBuilder(new BlockPos(0,0,0));
		}
		return builder;
	}
	
	public boolean build(EntityPlayer player, BlockPos pos) {
		BuilderBase builder = getBuilder(pos);
		

		if (builder.canBuild()) {
			if (!world.isRemote) {
				if (player.isCreative()) {
					builder.build();
					return true;
				}
				
				// Survival check
				
				int ties = 0;
				int rails = 0;
				int bed = 0;
				int fill = 0;
				
				for (ItemStack playerStack : player.inventory.mainInventory) {
					if (playerStack == null) {
						continue;
					}
					if (playerStack.getItem() == ImmersiveRailroading.ITEM_RAIL && ItemGauge.get(playerStack) == builder.gauge) {
						rails += playerStack.stackSize;
					}
					if (OreDictionaryContainsMatch(false, OreDictionary.getOres("plankTreatedWood"), playerStack)) {
						ties += playerStack.stackSize;
					}
					if (railBed != null && railBed.getItem() == playerStack.getItem() && railBed.getMetadata() == playerStack.getMetadata()) {
						bed += playerStack.stackSize;
					}
					if (railBedFill != null && railBedFill.getItem() == playerStack.getItem() && railBedFill.getMetadata() == playerStack.getMetadata()) {
						fill += playerStack.stackSize;
					}
				}
				
				if (ties < builder.costTies()) {
					player.addChatMessage(ChatText.BUILD_MISSING_TIES.getMessage(builder.costTies() - ties));
					return false;
				}
				
				if (rails < builder.costRails()) {
					player.addChatMessage(ChatText.BUILD_MISSING_RAILS.getMessage(builder.costRails() - rails));
					return false;
				}
				
				if (railBed != null && bed < builder.costBed()) {
					player.addChatMessage(ChatText.BUILD_MISSING_RAIL_BED.getMessage(builder.costBed() - bed));
					return false;
				}
				
				if (railBedFill != null && fill < builder.costFill()) {
					player.addChatMessage(ChatText.BUILD_MISSING_RAIL_BED_FILL.getMessage(builder.costFill() - fill));
					return false;
				}

				ties = builder.costTies();
				rails = builder.costRails();
				bed = builder.costBed();
				fill = builder.costFill();
				List<ItemStack> drops = new ArrayList<ItemStack>();
				
				for (ItemStack playerStack : player.inventory.mainInventory) {
					if (playerStack == null) {
						continue;
					}
					if (playerStack.getItem() == ImmersiveRailroading.ITEM_RAIL && ItemGauge.get(playerStack) == builder.gauge) {
						if (rails > playerStack.stackSize) {
							rails -= playerStack.stackSize;
							ItemStack copy = playerStack.copy();
							copy.stackSize = (playerStack.stackSize);
							drops.add(copy); 
							playerStack.stackSize = (0);
						} else if (rails != 0) {
							ItemStack copy = playerStack.copy();
							copy.stackSize = (rails);
							drops.add(copy); 
							playerStack.stackSize = (playerStack.stackSize - rails);
							rails = 0;
						}
					}
					if (OreDictionaryContainsMatch(false, OreDictionary.getOres("plankTreatedWood"), playerStack)) {
						if (ties > playerStack.stackSize) {
							ties -= playerStack.stackSize;
							ItemStack copy = playerStack.copy();
							copy.stackSize = (playerStack.stackSize);
							drops.add(copy);  
							playerStack.stackSize = (0);
						} else if (ties != 0) {
							ItemStack copy = playerStack.copy();
							copy.stackSize = (ties);
							drops.add(copy); 
							playerStack.stackSize = (playerStack.stackSize - ties);
							ties = 0;
						}
					}
					if (railBed != null && railBed.getItem() == playerStack.getItem() && railBed.getMetadata() == playerStack.getMetadata()) {
						if (bed > playerStack.stackSize) {
							bed -= playerStack.stackSize;
							ItemStack copy = playerStack.copy();
							copy.stackSize = (playerStack.stackSize);
							drops.add(copy);  
							playerStack.stackSize = (0);
						} else if (bed != 0) {
							ItemStack copy = playerStack.copy();
							copy.stackSize = (bed);
							drops.add(copy); 
							playerStack.stackSize = (playerStack.stackSize - bed);
							bed = 0;
						}
					}
					if (railBedFill != null && railBedFill.getItem() == playerStack.getItem() && railBedFill.getMetadata() == playerStack.getMetadata()) {
						if (fill > playerStack.stackSize) {
							fill -= playerStack.stackSize;
							ItemStack copy = playerStack.copy();
							copy.stackSize = (playerStack.stackSize);
							//drops.add(copy);  
							playerStack.stackSize = (0);
						} else if (fill != 0) {
							ItemStack copy = playerStack.copy();
							copy.stackSize = (fill);
							//drops.add(copy); 
							playerStack.stackSize = (playerStack.stackSize - fill);
							fill = 0;
						}
					}
				}
				builder.setDrops(drops);
				builder.build();
				return true;
			}
		}
		return false;
	}

	
	private boolean OreDictionaryContainsMatch(boolean strict, List<ItemStack> ores, ItemStack playerStack) {
        for (ItemStack target : ores)
        {
            if (OreDictionary.itemMatches(target, playerStack, strict))
            {
                return true;
            }
        }
        return false;
	}
}
