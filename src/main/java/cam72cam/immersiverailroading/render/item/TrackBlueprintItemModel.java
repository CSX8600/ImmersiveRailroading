package cam72cam.immersiverailroading.render.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.render.rail.RailBaseRender;
import cam72cam.immersiverailroading.render.rail.RailBuilderRender;
import cam72cam.immersiverailroading.util.GLBoolTracker;
import cam72cam.immersiverailroading.util.RailInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class TrackBlueprintItemModel implements IBakedModel {
	private RailInfo info;

	public TrackBlueprintItemModel() {
	}
	
	public TrackBlueprintItemModel(ItemStack stack, World world) {
		if (world == null) {
			world = Minecraft.getMinecraft().world;
		}
		info = new RailInfo(stack, world, 360-10, new BlockPos(0, 0, 0), 0.5f, 0.5f, 0.5f);
		info.length = 10;
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		if (info == null) {
			return new ArrayList<BakedQuad>();
		}
		
		GL11.glPushMatrix();

		if (info.type == TrackItems.TURN || info.type == TrackItems.SWITCH) {
			GL11.glTranslated(0, 0, -0.1 * info.quarters);
		}
		
		
		GL11.glRotated(-90, 0, 1, 0);
		GL11.glRotated(-90, 1, 0, 0);
		
		
			
		double scale = 0.95/info.length;
		if (info.type == TrackItems.CROSSING) {
			scale = 0.95 / 3;
		}
		if (info.type == TrackItems.TURNTABLE) {
			scale *= 0.25;
		}
		GL11.glScaled(scale, -scale*2, scale);

		GLBoolTracker cull = new GLBoolTracker(GL11.GL_CULL_FACE, false);
		GLBoolTracker lighting = new GLBoolTracker(GL11.GL_LIGHTING, false);
		RailBaseRender.draw(info);
		RailBuilderRender.renderRailBuilder(info);
		
		lighting.restore();
		cull.restore();
		
		GL11.glPopMatrix();
		
		return new ArrayList<BakedQuad>();
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return null;
	}
	
	public class ItemOverrideListHack extends ItemOverrideList {
		public ItemOverrideListHack() {
			super(new ArrayList<ItemOverride>());
		}

		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
			return new TrackBlueprintItemModel(stack, world);
		}
	}

	@Override
	public ItemOverrideList getOverrides() {
		return new ItemOverrideListHack();
	}

	/*
	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
		Pair<? extends IBakedModel, Matrix4f> defaultVal = ForgeHooksClient.handlePerspective(this, cameraTransformType);
		switch (cameraTransformType) {
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
			return Pair.of(defaultVal.getLeft(),
					new Matrix4().rotate(Math.toRadians(90), 0, 1, 0).rotate(Math.toRadians(-60), 0, 0, 1).translate(0.5,0.25,0.5).toMatrix4f());
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			return Pair.of(defaultVal.getLeft(),
					new Matrix4().rotate(Math.toRadians(90), 0, 1, 0).rotate(Math.toRadians(-30), 0, 0, 1).translate(0.5,0.25,0.5).toMatrix4f());
		case GROUND:
			return Pair.of(defaultVal.getLeft(), new Matrix4().translate(0.5,0,0.5).toMatrix4f());
		case FIXED:
			// Item Frame
			return Pair.of(defaultVal.getLeft(), new Matrix4().rotate(Math.toRadians(-90), 0, 1, 0).toMatrix4f());
		case GUI:
			return Pair.of(defaultVal.getLeft(), new Matrix4().translate(0.5, 0, 0).rotate(Math.toRadians(+5+90), 0, 1, 0).toMatrix4f());
		case HEAD:
			return Pair.of(defaultVal.getLeft(),
					new Matrix4().translate(0, 0, 0.5).rotate(Math.toRadians(-90), 0, 1, 0).toMatrix4f());
		case NONE:
			return defaultVal;
		}
		return defaultVal;
	}*/
	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return new ItemCameraTransforms(ItemCameraTransforms.DEFAULT) {
			public ItemTransformVec3f getTransform(ItemCameraTransforms.TransformType type) {
				switch (type) {
				case THIRD_PERSON_LEFT_HAND:
				case THIRD_PERSON_RIGHT_HAND:
					return new ItemTransformVec3f(new Vector3f(60, -90, 0), new Vector3f(0f,0f,0f), new Vector3f(1f, 1f, 1f));
				case FIRST_PERSON_LEFT_HAND:
				case FIRST_PERSON_RIGHT_HAND:
					return new ItemTransformVec3f(new Vector3f(10, -90, 0), new Vector3f(0f,0f,0f), new Vector3f(1f, 1f, 1f));
				case GROUND:
					return new ItemTransformVec3f(new Vector3f(0, -90, 0), new Vector3f(0f,0f,0f), new Vector3f(1f, 1f, 1f));
				case FIXED:
					// Item Frame
					return new ItemTransformVec3f(new Vector3f(0, -90, 0), new Vector3f(0f,0f,0f), new Vector3f(1f, 1f, 1f));
				case GUI:
					return new ItemTransformVec3f(new Vector3f(0, 95, 0), new Vector3f(0.5f,0f,0f), new Vector3f(1f, 1f, 1f));
				case HEAD:
					return new ItemTransformVec3f(new Vector3f(0, -90, 0), new Vector3f(0f, 0f, 0.5f), new Vector3f(1f, 1f, 1f));
				case NONE:
					break;
				default:
					break;
				}
				
				return super.getTransform(type);
			}
		};
	}
}
