package cam72cam.immersiverailroading.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class BakedScaledModel implements IBakedModel {
	// I know this is evil and I love it :D
	
	private IBakedModel source;
	private final Vec3d scale;
	private final Vec3d transform;
	
	public BakedScaledModel(IBakedModel source, Vec3d scale, Vec3d transform) {
		this.source = source;
		this.scale = scale;
		this.transform = transform;
	}
	
	public BakedScaledModel(IBakedModel source, float height) {
		this.source = source;
		this.scale = new Vec3d(1, height, 1);
		transform = new Vec3d(0,0,0);
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		List<BakedQuad> quads = source.getQuads(state, side, rand);
		List<BakedQuad> newQuads = new ArrayList<BakedQuad>();
		for (BakedQuad quad : quads) {
			int[] newData = Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length);

            VertexFormat format = quad.getFormat();
			
			for (int i = 0; i < 4; ++i)
	        {
				int j = format.getIntegerSize() * i;
	            newData[j + 0] = Float.floatToRawIntBits(Float.intBitsToFloat(newData[j + 0]) * (float)scale.xCoord + (float)transform.xCoord);
	            newData[j + 1] = Float.floatToRawIntBits(Float.intBitsToFloat(newData[j + 1]) * (float)scale.yCoord + (float)transform.yCoord);
	            newData[j + 2] = Float.floatToRawIntBits(Float.intBitsToFloat(newData[j + 2]) * (float)scale.zCoord + (float)transform.zCoord);
	        }
			
			newQuads.add(new BakedQuad(newData, quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat()));
		}
		
		return newQuads;
	}

	@Override
	public boolean isAmbientOcclusion() { return source.isAmbientOcclusion(); }
	@Override
	public boolean isGui3d() { return source.isGui3d(); }
	@Override
	public boolean isBuiltInRenderer() { return source.isBuiltInRenderer(); }
	@Override
	public TextureAtlasSprite getParticleTexture() { return source.getParticleTexture(); }
	@Override
	public ItemOverrideList getOverrides() { return source.getOverrides(); }
	@Override
	public ItemCameraTransforms getItemCameraTransforms() { return source.getItemCameraTransforms(); }
}