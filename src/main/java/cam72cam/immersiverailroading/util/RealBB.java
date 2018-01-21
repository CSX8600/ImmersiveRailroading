package cam72cam.immersiverailroading.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/*
 * For now this just wraps the AABB constructor
 * 
 *  In the future we can override the intersects functions for better bounding boxes
 */
public class RealBB extends AxisAlignedBB {
	private double front;
	private double rear;
	private double width;
	private double height;
	private float yaw;
	private double centerX;
	private double centerY;
	private double centerZ;
	private double[][] heightMap;
	
	public RealBB(double front, double rear, double width, double height, float yaw) {
		this(front, rear, width, height, yaw, null);		
	}
	
	public RealBB(double front, double rear, double width, double height, float yaw, double[][] heightMap) {
		this(front, rear, width, height, yaw, 0, 0, 0, heightMap);
	}
	
	private RealBB(double front, double rear, double width, double height, float yaw, double centerX, double centerY, double centerZ, double[][] heightMap) {
		this(constructorParams(front, rear, width, height, yaw, centerX, centerY, centerZ));
		this.front = front;
		this.rear = rear;
		this.width = width;
		this.height = height;
		this.yaw = yaw;
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
		this.heightMap = heightMap;
	}
	
	private RealBB(double[] constructorParams) {
		super(constructorParams[0], constructorParams[1], constructorParams[2], constructorParams[3], constructorParams[4], constructorParams[5]);
	}

	private static AxisAlignedBB newBB(Vec3d min, Vec3d max) {
		//Why the fuck is this ClientOnly?
		return new AxisAlignedBB(min.xCoord, min.yCoord, min.zCoord, max.xCoord, max.yCoord, max.zCoord);
	}

	private static double[] constructorParams(double front, double rear, double width, double height, float yaw, double centerX, double centerY, double centerZ) {
		Vec3d frontPos = VecUtil.fromYaw(front, yaw);
		Vec3d rearPos = VecUtil.fromYaw(rear, yaw);

		// width
		Vec3d offsetRight = VecUtil.fromYaw(width / 2, yaw + 90);
		Vec3d offsetLeft = VecUtil.fromYaw(width / 2, yaw - 90);
		AxisAlignedBB rightBox = newBB(frontPos.add(offsetRight), rearPos.add(offsetRight));
		AxisAlignedBB leftBox = newBB(frontPos.add(offsetLeft), rearPos.add(offsetLeft));

		AxisAlignedBB newthis = rightBox.union(leftBox).offset(centerX, centerY, centerZ);
		return new double[] { newthis.maxX, newthis.maxY + height, newthis.maxZ, newthis.minX, newthis.minY, newthis.minZ };
	}
	
	public RealBB clone() {
		return new RealBB(front, rear, width, height, yaw, centerX, centerY, centerZ, heightMap);
	}
	public AxisAlignedBB setMaxY(double y2) {
		return this.clone();
	}
	public AxisAlignedBB contract(double x, double y, double z) {
		RealBB expanded = this.clone();
		
		if (x > 0) {
			expanded.front -= x;
		} else {
			expanded.rear -= x;
		}
		
		if (y > 0) {
			expanded.height -= y;
		} else {
			expanded.centerY -= y;
		}
		
		expanded.width -= z;
		
		return expanded.clone();
	}
	public AxisAlignedBB expand(double x, double y, double z) {
		RealBB expanded = this.clone();
		
		if (x > 0) {
			expanded.front += x;
		} else {
			expanded.rear += x;
		}
		
		if (y > 0) {
			expanded.height += y;
		} else {
			expanded.centerY += y;
		}
		
		expanded.width += z;
		
		return expanded.clone();
	}
	public AxisAlignedBB grow(double x, double y, double z) {
		RealBB growed = this.clone();
		growed.front += x;
		growed.rear += x;
		growed.height += y;
		growed.centerY += y;
		growed.width += z + z;
		return growed;
	}
	public AxisAlignedBB intersect(AxisAlignedBB p_191500_1_) {
		return this.clone();
	}
	public AxisAlignedBB union(AxisAlignedBB other) {
		return this.clone();
	}
	
	@Override
	public AxisAlignedBB offset(BlockPos pos) {
		return this.offset(pos.getX(), pos.getY(), pos.getZ());
	}
	//@Override
	public AxisAlignedBB offset(Vec3d vec) {
		return this.offset(vec.xCoord, vec.yCoord, vec.zCoord);
	}
	@Override
	public AxisAlignedBB offset(double x, double y, double z) {
		RealBB offsetted = this.clone();
		offsetted.centerX += x;
		offsetted.centerY += y;
		offsetted.centerZ += z;
		return offsetted.clone();
	}
	
	public double calculateXOffset(AxisAlignedBB other, double offsetX) {
		return 0;
	}
	public double calculateYOffset(AxisAlignedBB other, double offsetY) {
		double hack = 0.04;
		other = other.expand(hack, 0, hack);
		Double intersect = intersectsAt(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ, true).getRight();
		if (other.minY < intersect) {
			return Math.min(0.1, intersect - other.minY);
		} else {
			return 0;
		}
	}
	public double calculateZOffset(AxisAlignedBB other, double offsetZ) {
		return 0;
	}
	
	@Override
	public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return intersectsAt(minX, minY, minZ, maxX, maxY, maxZ, true).getLeft();
	}
	public Pair<Boolean, Double> intersectsAt(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, boolean useHeightmap) {
		if (!super.intersects(minX, minY, minZ, maxX, maxY, maxZ)) {
			return Pair.of(false, minY);
		}
		
		double actualYMin = this.centerY;
		double actualYMax = this.centerY + this.height;
		if (! (actualYMin < maxY && actualYMax > minY)) {
			return Pair.of(false, minY);
		}
		
		Rectangle2D otherRect = new Rectangle2D.Double(minX, minZ, 0, 0);
		if (minX == maxX && minZ == maxZ) {
			otherRect.add(maxX+0.2, maxZ + 0.2);
		} else {
			otherRect.add(maxX, maxZ);
		}
		
		Rectangle2D myRect = new Rectangle2D.Double(this.rear, -this.width/2, 0, 0);
		myRect.add(this.front, this.width/2);
		
		Area otherArea = new Area(otherRect);
		Area myArea = new Area(myRect);
		
		AffineTransform myTransform = new AffineTransform();
		myTransform.translate(this.centerX, this.centerZ);
		myArea.transform(myTransform);
		
		AffineTransform otherTransform = new AffineTransform();
		otherTransform.rotate(Math.toRadians(180-yaw+90), this.centerX, this.centerZ);
		otherArea.transform(otherTransform);

		if (!otherArea.intersects(myArea.getBounds2D())) {
			return Pair.of(false, minY);
		}
		if (this.heightMap != null && useHeightmap) {
			int xRes = this.heightMap.length-1;
			int zRes = this.heightMap[0].length-1;
			
			double length = this.front-this.rear;
			
			actualYMin = this.centerY;
			actualYMax = this.centerY;

			Rectangle2D bds = otherArea.getBounds2D();
			

			double px = bds.getMinX() - (this.centerX - length/2);
			double pz =bds.getMinY() - (this.centerZ - width/2);
			double Px = bds.getMaxX() - (this.centerX - length/2);
			double Pz =bds.getMaxY() - (this.centerZ - width/2);
			
			double cx = Math.max(0, Math.min(length, px));
			double cz = Math.max(0, Math.min(width, pz));
			double Cx = Math.max(0, Math.min(length, Px));
			double Cz = Math.max(0, Math.min(width, Pz));

			cx = (cx/length*xRes);
			cz = (cz/width*zRes);
			Cx = (Cx/length*xRes);
			Cz = (Cz/width*zRes);
			
			for (int x = (int) cx; x < (int)Cx; x++) {
				for (int z = (int) cz; z < (int)Cz; z++) {
					actualYMax = Math.max(actualYMax, this.centerY + this.height * this.heightMap[x][z]);
				}
			}

			return Pair.of(actualYMin < maxY && actualYMax > minY, actualYMax);
		}
		
		return Pair.of(true, this.maxY);
	}
	
	public boolean contains(Vec3d vec) {
		return this.intersectsAt(vec.xCoord, vec.yCoord, vec.zCoord, vec.xCoord, vec.yCoord, vec.zCoord, false).getLeft();
	}
	public RayTraceResult calculateIntercept(Vec3d vecA, Vec3d vecB) {
		// This does NOT set enumfacing.  The places where this code (entity) is used don't use that value as of 1.12.
		int steps = 10;
		double xDist = vecB.xCoord - vecA.xCoord;
		double yDist = vecB.yCoord - vecA.yCoord;
		double zDist = vecB.zCoord - vecA.zCoord;
		double xDelta = xDist / steps;
		double yDelta = yDist / steps;
		double zDelta = zDist / steps;
		for (int step = 0; step < steps; step ++) {
			Vec3d stepPos = new Vec3d(vecA.xCoord + xDelta * step, vecA.yCoord + yDelta * step, vecA.zCoord + zDelta * step);
			if (this.contains(stepPos)) {
				return new RayTraceResult(stepPos, EnumFacing.UP); 
			}
		}
		return null;
	}

	public AxisAlignedBB withHeightMap(double[][] heightMap) {
		RealBB bb = this.clone();
		bb.heightMap = heightMap;
		return bb;
	}
}
