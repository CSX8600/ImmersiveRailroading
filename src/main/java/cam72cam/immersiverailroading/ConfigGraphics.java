package cam72cam.immersiverailroading;

import net.minecraftforge.common.config.Config.Comment;

@net.minecraftforge.common.config.Config(modid = ImmersiveRailroading.MODID, name = ImmersiveRailroading.MODID + "_graphics")
public class ConfigGraphics {
	@Comment({ "Place to draw the Train GUI as a % from the left of the screen" })
	public static int GUIPositionHorizontal = 2;

	@Comment({ "Place to draw the Train GUI as a % from the top of the screen" })
	public static int GUIPositionVertical = 95;

	@Comment("Enable Particles")
	public static boolean particlesEnabled = true;

	@Comment("Enable priming of item render cache.  Disable this if you keep crashing right before the loading screen")
	public static boolean enableItemRenderPriming = true;

	@Comment({ "Override GPU Max texture settings !DEV USE ONLY! (-1 == disable)" })
	public static int overrideGPUTexSize = -1;

	@Comment("Use Icon Cache (experimental)")
	public static boolean enableIconCache = false;

	@Comment("If you are having render problems in game, try setting this to false")
	public static boolean useShaderFriendlyRender = true;

	@Comment("Limit GPU load while models/world is loading.  1 == slow load, 3 == reasonable load, 10 = fast load, 100 = as fast as possible")
	public static int limitGraphicsLoadMS = 3;

	@Comment({ "Self explanitory" })
	public static boolean trainsOnTheBrain = true;

}
