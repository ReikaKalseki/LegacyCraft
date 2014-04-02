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

import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenHills;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Instantiable.IO.ControlledConfig;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Libraries.ReikaRecipeHelper;
import Reika.DragonAPI.Libraries.ReikaRegistryHelper;
import Reika.DragonAPI.Libraries.Java.ReikaObfuscationHelper;
import Reika.LegacyCraft.Overrides.BlockClosedEndPortal;
import Reika.LegacyCraft.Overrides.BlockClosedPortal;
import Reika.LegacyCraft.Overrides.LegacyPotionHealth;
import Reika.LegacyCraft.Overrides.LegacyPotionRegen;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyCreeper;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyEnderman;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacySkeleton;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacySpider;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyZombie;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod( modid = "LegacyCraft", name="LegacyCraft", version="beta", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI")
@NetworkMod(clientSideRequired = true, serverSideRequired = true)
public class LegacyCraft extends DragonAPIMod {

	@Instance("LegacyCraft")
	public static LegacyCraft instance = new LegacyCraft();

	public static final ControlledConfig config = new ControlledConfig(instance, LegacyOptions.optionList, null, null, null, 1);

	public static ModLogger logger;

	@Override
	@EventHandler
	public void preload(FMLPreInitializationEvent evt) {
		config.loadSubfolderedConfigFile(evt);
		config.initProps(evt);

		logger = new ModLogger(instance, LegacyOptions.LOGLOADING.getState(), LegacyOptions.DEBUGMODE.getState(), false);
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.TERRAIN_GEN_BUS.register(this);

		ReikaRegistryHelper.setupModData(instance, evt);
		ReikaRegistryHelper.setupVersionChecking(evt);
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		//Overrides vanilla
		EntityList.addMapping(EntityLegacyCreeper.class, "Creeper", 50, 894731, 0);
		EntityList.addMapping(EntityLegacySkeleton.class, "Skeleton", 51, 12698049, 4802889);
		EntityList.addMapping(EntityLegacyZombie.class, "Zombie", 54, 44975, 7969893);
		EntityList.addMapping(EntityLegacySpider.class, "Spider", 52, 3419431, 11013646);
		EntityList.addMapping(EntityLegacyEnderman.class, "Enderman", 58, 1447446, 0);

		if (LegacyOptions.OLDPOTIONS.getState()) { //overwrite vanilla
			LegacyPotionHealth health = new LegacyPotionHealth();
			LegacyPotionRegen regen = new LegacyPotionRegen();
		}

		if (LegacyOptions.GOLDENAPPLE.getState()) {
			List<ShapedRecipes> li = ReikaRecipeHelper.getShapedRecipesByOutput(new ItemStack(Item.appleGold.itemID, 1, 0));
			for (int i = 0; i < li.size(); i++) {
				ShapedRecipes ir = li.get(i);
				for (int j = 0; j < ir.recipeItems.length; j++) {
					ItemStack is = ir.recipeItems[j];
					if (is != null && is.itemID == Item.ingotGold.itemID) {
						ir.recipeItems[j] = new ItemStack(Item.goldNugget);
					}
				}
			}
		}

		if (LegacyOptions.OLDBOOK.getState()) {
			List<ShapedRecipes> li = ReikaRecipeHelper.getShapedRecipesByOutput(new ItemStack(Item.book.itemID, 1, 0));
			for (int i = 0; i < li.size(); i++) {
				ShapedRecipes ir = li.get(i);
				CraftingManager.getInstance().getRecipeList().remove(ir);
			}
			GameRegistry.addRecipe(new ItemStack(Item.book), "P", "P", "P", 'P', Item.paper);
		}

		if (LegacyOptions.OLDBOOK.getState()) {
			List<ShapedRecipes> li = ReikaRecipeHelper.getShapedRecipesByOutput(new ItemStack(Item.speckledMelon.itemID, 1, 0));
			for (int i = 0; i < li.size(); i++) {
				ShapedRecipes ir = li.get(i);
				CraftingManager.getInstance().getRecipeList().remove(ir);
			}
			GameRegistry.addShapelessRecipe(new ItemStack(Item.speckledMelon), Item.melon, Item.goldNugget);
		}

		if (LegacyOptions.SILVERFISH.getState()) {
			BiomeGenHills ex = (BiomeGenHills)BiomeGenBase.extremeHills;
			BiomeGenHills ex2 = (BiomeGenHills)BiomeGenBase.extremeHillsEdge;
			Class c = BiomeGenHills.class;
			WorldGenMinable dummy = new WorldGenMinable(Block.stone.blockID, 0);
			Field f = ReikaObfuscationHelper.getField("theWorldGenerator");
			try {
				f.set(ex, dummy);
				f.set(ex2, dummy);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (LegacyOptions.PIGPORTALS.getState()) {
			Block.portal.setTickRandomly(false);
		}

		if (LegacyOptions.CLOSEDPORTALS.getState()) {
			ReikaRegistryHelper.overrideBlock(instance, "portal", BlockClosedPortal.class);
			ReikaRegistryHelper.overrideBlock(instance, "endPortal", BlockClosedEndPortal.class);
		}

		TickRegistry.registerTickHandler(LegacyTickHandler.instance, Side.SERVER);
	}

	@Override
	@EventHandler
	public void postload(FMLPostInitializationEvent evt) {

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
		return DragonAPICore.getReikaForumPage(instance);
	}

	@Override
	public boolean hasWiki() {
		return false;
	}

	@Override
	public URL getWiki() {
		return null;
	}

	@Override
	public boolean hasVersion() {
		return false;
	}

	@Override
	public String getVersionName() {
		return null;
	}

	@Override
	public ModLogger getModLogger() {
		return logger;
	}

	@ForgeSubscribe(priority = EventPriority.LOWEST)
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
	/*
	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void noZombieGroups(SummonAidEvent ev) {
		ev.setResult(LegacyOptions.BACKUP.getState() ? Result.DENY : ev.getResult());
	}

	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void noZombieRegen(SummonAidEvent ev) {
		ev.setResult(LegacyOptions.BACKUP.getState() ? Result.DENY : ev.getResult());
	}
	 */
	@ForgeSubscribe(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void bonemeal(BonemealEvent evt) {
		if (LegacyOptions.BONEMEAL.getState()) {
			if (evt.ID == Block.sapling.blockID) {
				BlockSapling sap = (BlockSapling)Block.sapling;
				int meta = evt.world.getBlockMetadata(evt.X, evt.Y, evt.Z)+8;
				evt.world.setBlockMetadataWithNotify(evt.X, evt.Y, evt.Z, meta, 3);
				sap.markOrGrowMarked(evt.world, evt.X, evt.Y, evt.Z, new Random());
				evt.setResult(Result.ALLOW);
			}
		}
	}
	/*
	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void noHiddenLava(PopulateChunkEvent.Populate ev) {
		ev.setResult(LegacyOptions.HIDDENLAVA.getState() ? Result.DENY : Result.DEFAULT);
	}*/

	public static float getNonAIMoveSpeed() {
		return 0.2F;
	}

}
