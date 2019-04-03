package cam72cam.immersiverailroading.net;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Predicate;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.entity.EntityRidableRollingStock;
import cam72cam.immersiverailroading.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DebugPacket implements IMessage {
	private Vec3d v1;
	private Vec3d v2;
	private int dimension;
	private UUID stockID;
	
	public DebugPacket () {
		//Reflection
	}
	
	public DebugPacket (Vec3d v1, Vec3d v2, EntityRidableRollingStock stock) {
		this.v1 = v1;
		this.v2 = v2;
		this.dimension = stock.getEntityWorld().provider.getDimension();
		this.stockID = stock.getPersistentID();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		v1 = BufferUtil.readVec3d(buf);
		v2 = BufferUtil.readVec3d(buf);
		stockID = BufferUtil.readUUID(buf);
		dimension = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		BufferUtil.writeVec3d(buf, v1);
		BufferUtil.writeVec3d(buf, v2);
		BufferUtil.writeUUID(buf, stockID);
		buf.writeInt(dimension);
	}
	
	public static class Handler implements IMessageHandler<DebugPacket, IMessage> {
		@Override
		public IMessage onMessage(DebugPacket message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private void handle(DebugPacket message, MessageContext ctx) {
			List<EntityRidableRollingStock> matches = ImmersiveRailroading.proxy.getWorld(message.dimension).getEntities(EntityRidableRollingStock.class, new Predicate<EntityRidableRollingStock>()
		    {
		        @Override
				public boolean apply(@Nullable EntityRidableRollingStock entity)
		        {
		            return entity != null && entity.getPersistentID().equals(message.stockID);
		        }
		    });
			
			if (matches.size() != 1) {
				return;
			}
			
			EntityRidableRollingStock entity = matches.get(0);
			entity.rays.put(0, Pair.of(message.v1, message.v1.add(message.v2.scale(20))));
			
			Map<Integer, Pair<Vec3d, Vec3d>> map = entity.rays;
			
			map.values().forEach(k -> System.out.println(k));
		}
	}
}
