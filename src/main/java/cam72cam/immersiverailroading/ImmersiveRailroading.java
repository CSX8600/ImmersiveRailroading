package cam72cam.immersiverailroading;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import cam72cam.immersiverailroading.blocks.BlockMultiblock;
import cam72cam.immersiverailroading.blocks.BlockRail;
import cam72cam.immersiverailroading.blocks.BlockRailGag;
import cam72cam.immersiverailroading.blocks.BlockRailPreview;
import cam72cam.immersiverailroading.items.ItemCastRail;
import cam72cam.immersiverailroading.items.ItemHook;
import cam72cam.immersiverailroading.items.ItemLargeWrench;
import cam72cam.immersiverailroading.items.ItemManual;
import cam72cam.immersiverailroading.items.ItemPlate;
import cam72cam.immersiverailroading.items.ItemRail;
import cam72cam.immersiverailroading.items.ItemTrackBlueprint;
import cam72cam.immersiverailroading.items.ItemRailAugment;
import cam72cam.immersiverailroading.items.ItemRollingStock;
import cam72cam.immersiverailroading.items.ItemRollingStockComponent;
import cam72cam.immersiverailroading.proxy.ChunkManager;
import cam72cam.immersiverailroading.proxy.CommonProxy;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid = ImmersiveRailroading.MODID, name="ImmersiveRailroading", version = ImmersiveRailroading.VERSION, acceptedMinecraftVersions = "[1.10,1.11)", dependencies = "required-after:trackapi;required-after:immersiveengineering")
public class ImmersiveRailroading
{
    public static final String MODID = "immersiverailroading";
    public static final String VERSION = "0.4.7";
	public static final int ENTITY_SYNC_DISTANCE = 512;
    public static final String ORE_RAIL_BED = "railBed";
    
	
	public static final BlockRailGag BLOCK_RAIL_GAG = new BlockRailGag();
	
	public static final BlockRail BLOCK_RAIL = new BlockRail();
	
	public static final BlockRailPreview BLOCK_RAIL_PREVIEW = new BlockRailPreview();
	
	
	public static ItemRollingStock ITEM_ROLLING_STOCK = new ItemRollingStock();
	
	
	public static ItemRollingStockComponent ITEM_ROLLING_STOCK_COMPONENT = new ItemRollingStockComponent();
	
	
	public static ItemLargeWrench ITEM_LARGE_WRENCH = new ItemLargeWrench();
	
	
	public static ItemHook ITEM_HOOK = new ItemHook();
	
	public static ItemRailAugment ITEM_AUGMENT = new ItemRailAugment();
	
	public static Item ITEM_RAIL_BLOCK = new ItemTrackBlueprint();
	
	public static BlockMultiblock BLOCK_MULTIBLOCK = new BlockMultiblock();

	public static ItemManual ITEM_MANUAL = new ItemManual();
	
	public static ItemRail ITEM_RAIL = new ItemRail();
	
	public static ItemPlate ITEM_PLATE = new ItemPlate();
	
	public static ItemCastRail ITEM_CAST_RAIL = new ItemCastRail();
	
	private static Logger logger;
	public static ImmersiveRailroading instance;
	
	public static final SimpleNetworkWrapper net = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
	
	@SidedProxy(clientSide="cam72cam.immersiverailroading.proxy.ClientProxy", serverSide="cam72cam.immersiverailroading.proxy.ServerProxy")
	public static CommonProxy proxy;
	
	private ChunkManager chunker;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) throws IOException {
        logger = event.getModLog();
        instance = this;
        
        World.MAX_ENTITY_RADIUS = 32;
        
    	proxy.preInit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	proxy.init(event);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) throws IOException {
		chunker = new ChunkManager();
		chunker.init();
		proxy.postInit();
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
    	proxy.serverStarting(event);
    }
    
    public static void debug(String msg, Object...params) {
    	if (logger == null) {
    		System.out.println("DEBUG: " + String.format(msg, params));
    		return;
    	}
    	
    	if (Config.debugLog) {
    		logger.info(String.format(msg, params));
    	}
    }
    public static void info(String msg, Object...params) {
    	if (logger == null) {
    		System.out.println("INFO: " + String.format(msg, params));
    		return;
    	}
    	
    	logger.info(String.format(msg, params));
    }
    public static void warn(String msg, Object...params) {
    	if (logger == null) {
    		System.out.println("WARN: " + String.format(msg, params));
    		return;
    	}
    	
    	logger.warn(String.format(msg, params));
    }
    public static void error(String msg, Object...params) {
    	if (logger == null) {
    		System.out.println("ERROR: " + String.format(msg, params));
    		return;
    	}
    	
    	logger.error(String.format(msg, params));
    }
	public static void catching(Throwable ex) {
    	if (logger == null) {
    		ex.printStackTrace();
    		return;
    	}
    	
		logger.catching(ex);
	}
}
