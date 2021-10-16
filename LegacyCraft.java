/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenHills;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.DragonOptions;
import Reika.DragonAPI.Auxiliary.Trackers.CommandableUpdateChecker;
import Reika.DragonAPI.Auxiliary.Trackers.PlayerHandler;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.DragonAPIMod.LoadProfiler.LoadPhase;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Instantiable.IO.SingleSound;
import Reika.DragonAPI.Instantiable.IO.SoundLoader;
import Reika.DragonAPI.Libraries.Java.ReikaArrayHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.LegacyCraft.ASM.LegacyASMHandler;
import Reika.LegacyCraft.Overrides.LegacyPotionHealth;
import Reika.LegacyCraft.Overrides.LegacyPotionRegen;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod( modid = "LegacyCraft", name="LegacyCraft", version = "v@MAJOR_VERSION@@MINOR_VERSION@", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI")

public class LegacyCraft extends DragonAPIMod {

	@Instance("LegacyCraft")
	public static LegacyCraft instance = new LegacyCraft();

	public static final LegacyConfig config = new LegacyConfig(instance, LegacyOptions.optionList, null);

	public static ModLogger logger;

	static final Random rand = new Random();

	private static SingleSound flintAndSteel;
	private static SoundLoader sounds;

	@Override
	@EventHandler
	public void preload(FMLPreInitializationEvent evt) {
		this.startTiming(LoadPhase.PRELOAD);
		this.verifyInstallation();
		config.loadSubfolderedConfigFile(evt);
		config.initProps(evt);

		logger = new ModLogger(instance, false);
		if (DragonOptions.FILELOG.getState())
			logger.setOutput("**_Loading_Log.log");
		MinecraftForge.TERRAIN_GEN_BUS.register(LegacyEventHandler.instance);
		MinecraftForge.EVENT_BUS.register(LegacyEventHandler.instance);


		if (LegacyOptions.NETHERICE.getState()) {
			/*
			BlockLiquid b1 = (BlockLiquid)(new BlockLegacyDynamicLiquid(Material.water).disableStats().setHardness(100.0F).setLightOpacity(3).setBlockName("water").setBlockTextureName("water_flow"));
			BlockLiquid b2 = (BlockLiquid)(new BlockLegacyStaticLiquid(Material.water).disableStats().setHardness(100.0F).setLightOpacity(3).setBlockName("water").setBlockTextureName("water_still"));

			Block.blockRegistry.addObject(8, "flowing_water", b1);
			Block.blockRegistry.addObject(9, "water", b2);

			//Blocks.water = b2;
			//Blocks.flowing_water = b1;
			try {
				String f1 = FMLForgePlugin.RUNTIME_DEOBF ? "field_150355_j" : "water";
				String f2 = FMLForgePlugin.RUNTIME_DEOBF ? "field_150358_i" : "flowing_water";
				ReikaReflectionHelper.setFinalField(Blocks.class, f1, null, b2);
				ReikaReflectionHelper.setFinalField(Blocks.class, f2, null, b1);
				logger.log("Patched water block fields to ensure functionality of Nether Ice behavior");
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new RegistrationException(this, "Could not patch water block fields!");
			}
			 */
			if (Blocks.water.getClass() != BlockStaticLiquid.class || Blocks.flowing_water.getClass() != BlockDynamicLiquid.class) {
				logger.logError("Water block overridden with "+Blocks.water.getClass()+", "+Blocks.flowing_water.getClass()+", Ice in Nether behavior may not function. This is a serious mistake in "+ReikaItemHelper.getRegistrantMod(new ItemStack(Blocks.water)));
			}
		}


		this.basicSetup(evt);
		this.finishTiming();
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		this.startTiming(LoadPhase.LOAD);
		//Overrides vanilla
		//MobOverrides.registerAll();

		PlayerHandler.instance.registerTracker(new LegacyPlayerTracker());

		flintAndSteel = new SingleSound("flintsteel", "Reika/LegacyCraft/flintsteel.ogg");
		sounds = new SoundLoader(flintAndSteel);
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			flintAndSteel.setSoundCategory(SoundCategory.BLOCKS);
			sounds.register();
		}

		if (LegacyOptions.OLDPOTIONS.getState()) { //overwrite vanilla
			LegacyPotionHealth health = new LegacyPotionHealth();
			LegacyPotionRegen regen = new LegacyPotionRegen();
		}

		if (LegacyOptions.ENDERBLOCKS.getState()) {
			EntityEnderman.setCarriable(Blocks.stone, true);
			EntityEnderman.setCarriable(Blocks.planks, true);
			EntityEnderman.setCarriable(Blocks.cobblestone, true);
			EntityEnderman.setCarriable(Blocks.stonebrick, true);
		}

		if (LegacyOptions.SILVERFISH.getState()) {
			if (BiomeGenBase.extremeHills instanceof BiomeGenHills) { //Because BoP
				BiomeGenHills ex = (BiomeGenHills)BiomeGenBase.extremeHills;
				BiomeGenHills ex2 = (BiomeGenHills)BiomeGenBase.extremeHillsEdge;
				BiomeGenHills ex3 = (BiomeGenHills)BiomeGenBase.biomeList[BiomeGenBase.extremeHills.biomeID+128];
				BiomeGenHills ex4 = (BiomeGenHills)BiomeGenBase.extremeHillsPlus;
				BiomeGenHills ex5 = (BiomeGenHills)BiomeGenBase.biomeList[BiomeGenBase.extremeHillsPlus.biomeID+128];
				WorldGenMinable dummy = new WorldGenMinable(Blocks.stone, 0);
				ex.theWorldGenerator = dummy;
				ex2.theWorldGenerator = dummy;
				ex3.theWorldGenerator = dummy;
				ex4.theWorldGenerator = dummy;
				ex5.theWorldGenerator = dummy;
			}
		}

		if (LegacyOptions.OLDBOOK.getState()) {
			GameRegistry.addRecipe(new ItemStack(Items.book), "P", "P", "P", 'P', Items.paper);
		}

		if (LegacyOptions.OLDMELON.getState()) {
			GameRegistry.addShapelessRecipe(new ItemStack(Items.speckled_melon), Items.melon, Items.gold_nugget);
		}

		if (LegacyOptions.ROSES.getState()) {
			ItemMultiTexture item = (ItemMultiTexture)Item.getItemFromBlock(Blocks.red_flower);
			item.field_150942_c[0] = "rose";
			BlockFlower.field_149859_a[0] = "rose";
			BlockFlower.field_149860_M[1][0] = "rose";
		}

		if (LegacyOptions.PIGPORTALS.getState()) {
			Blocks.portal.setTickRandomly(false);
		}

		//TickRegistry.instance.registerTickHandler(LegacyTickHandler.instance);
		this.finishTiming();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void setIcons(TextureStitchEvent evt) {
		if (evt.map.getTextureType() == 0) {
			if (LegacyOptions.ROSES.getState())
				Blocks.red_flower.field_149861_N[0] = evt.map.registerIcon("legacycraft:rose");

			if (LegacyOptions.ALPHAGRASS.getState()) {
				Blocks.grass.field_149994_N = evt.map.registerIcon("legacycraft:alpha/grass_side");
				Blocks.grass.blockIcon = evt.map.registerIcon("legacycraft:alpha/grass_side");
				Blocks.grass.field_149991_b = evt.map.registerIcon("legacycraft:alpha/grass_top");

				for (int m = 0; m <= 3; m++) {
					for (int i = 0; i <= 1; i++) {
						String alpha = i == 0 ? "trans" : "opq";
						String s = "legacycraft:alpha/leaf_"+/*m*/0+"_"+alpha;
						Blocks.leaves.field_150129_M[i][m] = evt.map.registerIcon(s);
					}
				}

				for (int m = 0; m <= 1; m++) {
					for (int i = 0; i <= 1; i++) {
						String alpha = i == 0 ? "trans" : "opq";
						String s = "legacycraft:alpha/leaf_"+0+"_"+alpha;//"legacycraft:alpha/leaf2_"+m+"_"+alpha;
						Blocks.leaves2.field_150129_M[i][m] = evt.map.registerIcon(s);
					}
				}

				for (int i = 1; i <= 2; i++) {
					Blocks.tallgrass.field_149870_b[i] = evt.map.registerIcon("legacycraft:alpha/"+Blocks.tallgrass.field_149871_a[i]);
				}

				//for (int m = 0; m <= 5; m++) {
				BlockSapling.field_149881_b[0] = evt.map.registerIcon("legacycraft:alpha/sapling");
				//}
				Blocks.vine.blockIcon = evt.map.registerIcon("legacycraft:alpha/vine");

				int[] data = ReikaArrayHelper.getArrayOf(0xffffff, 65536);
				ColorizerGrass.setGrassBiomeColorizer(data);
				ColorizerFoliage.setFoliageBiomeColorizer(data);

				SimpleReloadableResourceManager mgr = (SimpleReloadableResourceManager)Minecraft.getMinecraft().getResourceManager();
				Iterator<IResourceManagerReloadListener> it = mgr.reloadListeners.iterator();
				while (it.hasNext()) {
					IResourceManagerReloadListener rl = it.next();
					if (rl.getClass().getSimpleName().contains("ColorReloadListener")) {
						it.remove();
					}
				}
			}
		}
	}

	@Override
	@EventHandler
	public void postload(FMLPostInitializationEvent evt) {
		this.startTiming(LoadPhase.POSTLOAD);

		if (LegacyOptions.OLDFIRE.getState()) {
			for (Object o : Block.blockRegistry.getKeys()) {
				String name = (String)o;
				Block b = Block.getBlockFromName(name);
				if (b != Blocks.air) {
					int spread = Blocks.fire.getEncouragement(b);
					int flamm = Blocks.fire.getFlammability(b);
					Blocks.fire.setFireInfo(b, spread*3, flamm*3);
				}
			}
		}

		this.finishTiming();
	}

	@Override
	public String getDisplayName() {
		return "LegacyCraft";
	}

	@Override
	public String getModAuthorName() {
		return "Reika";
	}

	@Override
	public URL getDocumentationSite() {
		return DragonAPICore.getReikaForumPage();
	}

	@Override
	public URL getBugSite() {
		return DragonAPICore.getReikaGithubPage();
	}

	@Override
	public String getWiki() {
		return null;
	}

	@Override
	public String getUpdateCheckURL() {
		return CommandableUpdateChecker.reikaURL;
	}

	@Override
	public ModLogger getModLogger() {
		return logger;
	}

	@Override
	public File getConfigFolder() {
		return config.getConfigFolder();
	}

	@Override
	protected Class<? extends IClassTransformer> getASMClass() {
		return LegacyASMHandler.LegacyTransformer.class;
	}

	/*
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void noHiddenLava(PopulateChunkEvent.Populate ev) {
		ev.setResult(LegacyOptions.HIDDENLAVA.getState() ? Result.DENY : Result.DEFAULT);
	}*/

	public static String getOldFlintAndSteelSound() {
		ResourceLocation s = sounds != null ? sounds.getResource(flintAndSteel) : null;
		return s != null ? s.toString() : "";
	}

}
