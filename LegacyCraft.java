/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft;

import java.net.URL;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenHills;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Auxiliary.CommandableUpdateChecker;
import Reika.DragonAPI.Auxiliary.TickRegistry;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Libraries.ReikaRecipeHelper;
import Reika.DragonAPI.Libraries.ReikaRegistryHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaCropHelper;
import Reika.DragonAPI.ModRegistry.ModCropList;
import Reika.LegacyCraft.Overrides.BlockClosedEndPortal;
import Reika.LegacyCraft.Overrides.BlockClosedPortal;
import Reika.LegacyCraft.Overrides.LegacyPotionHealth;
import Reika.LegacyCraft.Overrides.LegacyPotionRegen;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod( modid = "LegacyCraft", name="LegacyCraft", version="beta", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI")

public class LegacyCraft extends DragonAPIMod {

	@Instance("LegacyCraft")
	public static LegacyCraft instance = new LegacyCraft();

	public static final LegacyConfig config = new LegacyConfig(instance, LegacyOptions.optionList, null, 1);

	public static ModLogger logger;

	@Override
	@EventHandler
	public void preload(FMLPreInitializationEvent evt) {
		config.loadSubfolderedConfigFile(evt);
		config.initProps(evt);

		logger = new ModLogger(instance, false);
		MinecraftForge.TERRAIN_GEN_BUS.register(this);

		this.basicSetup(evt);
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		//Overrides vanilla
		MobOverrides.registerAll();

		if (LegacyOptions.OLDPOTIONS.getState()) { //overwrite vanilla
			LegacyPotionHealth health = new LegacyPotionHealth();
			LegacyPotionRegen regen = new LegacyPotionRegen();
		}

		if (LegacyOptions.GOLDENAPPLE.getState()) {
			List<ShapedRecipes> li = ReikaRecipeHelper.getShapedRecipesByOutput(new ItemStack(Items.golden_apple, 1, 0));
			for (int i = 0; i < li.size(); i++) {
				ShapedRecipes ir = li.get(i);
				for (int j = 0; j < ir.recipeItems.length; j++) {
					ItemStack is = ir.recipeItems[j];
					if (is != null && is.getItem() == Items.gold_ingot) {
						ir.recipeItems[j] = new ItemStack(Items.gold_nugget);
					}
				}
			}
		}

		if (LegacyOptions.OLDBOOK.getState()) {
			List<ShapedRecipes> li = ReikaRecipeHelper.getShapedRecipesByOutput(new ItemStack(Items.book, 1, 0));
			for (int i = 0; i < li.size(); i++) {
				ShapedRecipes ir = li.get(i);
				CraftingManager.getInstance().getRecipeList().remove(ir);
			}
			GameRegistry.addRecipe(new ItemStack(Items.book), "P", "P", "P", 'P', Items.paper);
		}

		if (LegacyOptions.OLDBOOK.getState()) {
			List<ShapedRecipes> li = ReikaRecipeHelper.getShapedRecipesByOutput(new ItemStack(Items.speckled_melon, 1, 0));
			for (int i = 0; i < li.size(); i++) {
				ShapedRecipes ir = li.get(i);
				CraftingManager.getInstance().getRecipeList().remove(ir);
			}
			GameRegistry.addShapelessRecipe(new ItemStack(Items.speckled_melon), Items.melon, Items.gold_nugget);
		}

		if (LegacyOptions.SILVERFISH.getState()) {
			if (BiomeGenBase.extremeHills instanceof BiomeGenHills) { //Because BoP
				BiomeGenHills ex = (BiomeGenHills)BiomeGenBase.extremeHills;
				BiomeGenHills ex2 = (BiomeGenHills)BiomeGenBase.extremeHillsEdge;
				Class c = BiomeGenHills.class;
				WorldGenMinable dummy = new WorldGenMinable(Blocks.stone, 0);
				ex.theWorldGenerator = dummy;
				ex2.theWorldGenerator = dummy;
			}
		}

		if (LegacyOptions.PIGPORTALS.getState()) {
			Blocks.portal.setTickRandomly(false);
		}

		if (LegacyOptions.CLOSEDPORTALS.getState()) {
			ReikaRegistryHelper.overrideBlock(instance, "portal", BlockClosedPortal.class);
			ReikaRegistryHelper.overrideBlock(instance, "endPortal", BlockClosedEndPortal.class);
		}

		TickRegistry.instance.registerTickHandler(LegacyTickHandler.instance, Side.SERVER);
	}

	@Override
	@EventHandler
	public void postload(FMLPostInitializationEvent evt) {

		if (LegacyOptions.OLDFIRE.getState()) {
			for (Object o : Block.blockRegistry.getKeys()) {
				String name = (String)o;
				Block b = Block.getBlockFromName(name);
				int spread = Blocks.fire.getEncouragement(b);
				int flamm = Blocks.fire.getFlammability(b);
				Blocks.fire.setFireInfo(b, spread*3, flamm*3);
			}
		}

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

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void controlMobs(LivingSpawnEvent.CheckSpawn ev) {
		EntityLivingBase e = ev.entityLiving;
		if (e instanceof EntityBat) {
			ev.setResult(LegacyOptions.BATS.getState() ? ev.getResult() : Result.DENY);
		}
		if (e instanceof EntityWitch) {
			ev.setResult(LegacyOptions.WITCHES.getState() ? ev.getResult() : Result.DENY);
		}
		if (e instanceof EntityZombie) {
			EntityZombie ez = (EntityZombie)e;
			if (ez.isChild() && LegacyOptions.BABYZOMBIES.getState()) {
				ev.setResult(Result.DENY);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void noZombieGroups(SummonAidEvent ev) {
		ev.setResult(LegacyOptions.BACKUP.getState() ? Result.DENY : ev.getResult());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void noZombieRegen(SummonAidEvent ev) {
		ev.setResult(LegacyOptions.BACKUP.getState() ? Result.DENY : ev.getResult());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void bonemeal(BonemealEvent evt) {
		if (LegacyOptions.BONEMEAL.getState()) {
			Block b = evt.block;
			if (b == Blocks.sapling) {
				BlockSapling sap = (BlockSapling)Blocks.sapling;
				int meta = evt.world.getBlockMetadata(evt.x, evt.y, evt.z)+8;
				evt.world.setBlockMetadataWithNotify(evt.x, evt.y, evt.z, meta, 3);
				sap.func_149879_c(evt.world, evt.x, evt.y, evt.z, new Random());
				evt.setResult(Result.ALLOW);
			}
			else {
				int meta = evt.world.getBlockMetadata(evt.x, evt.y, evt.z);
				ReikaCropHelper crop = ReikaCropHelper.getCrop(b);
				ModCropList mod = ModCropList.getModCrop(b, meta);
				if (crop != null) {
					int metato = crop.ripeMeta;
					evt.world.setBlockMetadataWithNotify(evt.x, evt.y, evt.z, metato, 3);
				}
				else if (mod != null) {
					if (mod == ModCropList.MAGIC) {
						//mod.makeRipe(evt.world, evt.x, evt.y, evt.z); //maybe want to specifically exclude magic crops?
					}
					else {
						mod.makeRipe(evt.world, evt.x, evt.y, evt.z);
					}
				}
			}
		}
	}
	/*
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void noHiddenLava(PopulateChunkEvent.Populate ev) {
		ev.setResult(LegacyOptions.HIDDENLAVA.getState() ? Result.DENY : Result.DEFAULT);
	}*/

	public static float getNonAIMoveSpeed() {
		return 0.2F;
	}

}
