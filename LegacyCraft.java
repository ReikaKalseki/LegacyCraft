/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft;

import java.net.URL;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenHills;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.DragonOptions;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Auxiliary.Trackers.CommandableUpdateChecker;
import Reika.DragonAPI.Auxiliary.Trackers.PlayerHandler;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.DragonAPIMod.LoadProfiler.LoadPhase;
import Reika.DragonAPI.Instantiable.Event.AddRecipeEvent;
import Reika.DragonAPI.Instantiable.Event.MobTargetingEvent;
import Reika.DragonAPI.Instantiable.Event.Client.LightmapEvent;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Interfaces.Registry.ModCrop;
import Reika.DragonAPI.Libraries.ReikaRecipeHelper;
import Reika.DragonAPI.Libraries.IO.ReikaColorAPI;
import Reika.DragonAPI.Libraries.Java.ReikaArrayHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaCropHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.TinkerBlockHandler;
import Reika.DragonAPI.ModRegistry.ModCropList;
import Reika.LegacyCraft.Overrides.LegacyPotionHealth;
import Reika.LegacyCraft.Overrides.LegacyPotionRegen;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyCreeper;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyEnderman;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacySkeleton;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyVillager;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyZombie;
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
import cpw.mods.fml.relauncher.SideOnly;

@Mod( modid = "LegacyCraft", name="LegacyCraft", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI")

public class LegacyCraft extends DragonAPIMod {

	@Instance("LegacyCraft")
	public static LegacyCraft instance = new LegacyCraft();

	public static final LegacyConfig config = new LegacyConfig(instance, LegacyOptions.optionList, null);

	public static ModLogger logger;

	private static final Random rand = new Random();

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
		MinecraftForge.TERRAIN_GEN_BUS.register(this);


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
		MobOverrides.registerAll();

		PlayerHandler.instance.registerTracker(new LegacyPlayerTracker());

		if (LegacyOptions.OLDPOTIONS.getState()) { //overwrite vanilla
			LegacyPotionHealth health = new LegacyPotionHealth();
			LegacyPotionRegen regen = new LegacyPotionRegen();
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
	public void onAddRecipe(AddRecipeEvent evt) {
		IRecipe ir = evt.recipe;
		ItemStack out = ir.getRecipeOutput();

		if (out != null) {
			if (LegacyOptions.GOLDENAPPLE.getState()) {
				if (ReikaItemHelper.matchStacks(new ItemStack(Items.golden_apple, 1, 0), out)) {
					ReikaRecipeHelper.replaceIngredientInRecipe(new ItemStack(Items.gold_ingot), new ItemStack(Items.gold_nugget), ir);
				}
			}

			if (LegacyOptions.OLDBOOK.getState()) {
				if (out.getItem() == Items.book && evt.isVanillaPass) {
					evt.setCanceled(true);
				}
			}

			if (LegacyOptions.OLDMELON.getState()) {
				if (ir instanceof ShapedRecipes && out.getItem() == Items.speckled_melon && evt.isVanillaPass) {
					evt.setCanceled(true);
				}
			}

			if (LegacyOptions.ROSES.getState()) {
				if (ir instanceof ShapelessRecipes) {
					ShapelessRecipes sr = (ShapelessRecipes)ir;
					if (ReikaItemHelper.matchStacks(out, ReikaItemHelper.redDye)) {
						if (sr.recipeItems.size() == 1) {
							Object o = sr.recipeItems.get(0);
							if (o instanceof ItemStack && ((ItemStack)o).getItem() == Item.getItemFromBlock(Blocks.red_flower)) {
								sr.getRecipeOutput().stackSize = Math.max(2, sr.getRecipeOutput().stackSize);
							}
						}
					}
				}
			}
		}
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
	public void enforceMobs(EntityJoinWorldEvent evt) {
		if (LegacyOptions.FORCEMOBS.getState() && !evt.world.isRemote) {
			Entity e = evt.entity;
			if (e.getClass() == EntityZombie.class && MobOverrides.ZOMBIE.isActive()) {
				evt.world.spawnEntityInWorld(new EntityLegacyZombie((EntityZombie)e));
				evt.setCanceled(true);
			}
			else if (e.getClass() == EntitySkeleton.class && MobOverrides.SKELETON.isActive()) {
				evt.world.spawnEntityInWorld(new EntityLegacySkeleton((EntitySkeleton)e));
				evt.setCanceled(true);
			}
			else if (e.getClass() == EntityCreeper.class && MobOverrides.CREEPER.isActive()) {
				evt.world.spawnEntityInWorld(new EntityLegacyCreeper((EntityCreeper)e));
				evt.setCanceled(true);
			}
			else if (e.getClass() == EntityEnderman.class && MobOverrides.ENDERMAN.isActive()) {
				evt.world.spawnEntityInWorld(new EntityLegacyEnderman((EntityEnderman)e));
				evt.setCanceled(true);
			}
			else if (e.getClass() == EntityVillager.class && MobOverrides.VILLAGER.isActive()) {
				evt.world.spawnEntityInWorld(new EntityLegacyVillager((EntityVillager)e));
				evt.setCanceled(true);
			}
		}
	}

	@SubscribeEvent //Fixes a TiC bug
	@ModDependent(ModList.TINKERER)
	public void necroticBones(LivingDropsEvent evt) {
		if (evt.entityLiving.getClass() == EntityLegacySkeleton.class) {
			if (((EntitySkeleton)evt.entityLiving).getSkeletonType() == 1) {
				if (rand.nextInt(Math.max(1, 5-evt.lootingLevel)) == 0) //Same formula as TiC
					ReikaItemHelper.dropItem(evt.entityLiving, TinkerBlockHandler.Materials.NECROTICBONE.getItem());
			}
		}
	}

	@SubscribeEvent()
	public void animalSpawn(PopulateChunkEvent.Populate evt) {
		if (evt.type == EventType.ANIMALS && LegacyOptions.ANIMALSPAWN.getState()) {
			if (ReikaRandomHelper.doWithChance(80)) {
				evt.setResult(Result.DENY);
			}
		}
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
				//ev.setResult(Result.DENY);
				ez.setChild(false);
			}
		}
		if (e instanceof EntityHorse) {
			ev.setResult(LegacyOptions.NOHORSES.getState() ? Result.DENY : ev.getResult());
		}
	}

	public void clearControlMobs(EntityJoinWorldEvent evt) {
		Entity e = evt.entity;
		if (e instanceof EntityHorse) {
			evt.setCanceled(LegacyOptions.NOHORSES.getState() ? true : evt.isCanceled());
		}
	}

	@SubscribeEvent()
	public void sheepPunch(AttackEntityEvent ev) {
		if (LegacyOptions.SHEEPUNCH.getState()) {
			Entity e = ev.target;
			if (e instanceof EntitySheep) {
				EntitySheep es = (EntitySheep)e;
				if (!es.getSheared() && !es.isChild() && es.getHealth() > 0) {
					int n = 1+es.worldObj.rand.nextInt(3);
					for (int i = 0; i < n; i++) {
						ReikaItemHelper.dropItem(es.worldObj, es.posX, es.posY+0.5, es.posZ, new ItemStack(Blocks.wool, 1, es.getFleeceColor()));
					}
					es.setSheared(true);
				}
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
		if (LegacyOptions.BONEMEAL.getState() && !evt.world.isRemote) {
			boolean flag = false;
			Block b = evt.block;
			if (b == Blocks.sapling) {
				BlockSapling sap = (BlockSapling)Blocks.sapling;
				int meta = evt.world.getBlockMetadata(evt.x, evt.y, evt.z)+8;
				evt.world.setBlockMetadataWithNotify(evt.x, evt.y, evt.z, meta, 3);
				sap.func_149879_c(evt.world, evt.x, evt.y, evt.z, new Random());
				evt.setResult(Result.ALLOW);
				return;
			}
			else {
				int meta = evt.world.getBlockMetadata(evt.x, evt.y, evt.z);
				ReikaCropHelper crop = ReikaCropHelper.getCrop(b);
				ModCrop mod = ModCropList.getModCrop(b, meta);
				if (crop != null) {
					int metato = crop.ripeMeta;
					evt.world.setBlockMetadataWithNotify(evt.x, evt.y, evt.z, metato, 3);
					flag = true;
				}
				else if (mod != null) {
					if (mod == ModCropList.MAGIC) {
						//mod.makeRipe(evt.world, evt.x, evt.y, evt.z); //maybe want to specifically exclude magic crops?
					}
					else if (mod == ModCropList.OREBERRY) {
						//mod.makeRipe(evt.world, evt.x, evt.y, evt.z); //maybe want to specifically exclude magic crops?
					}
					else {
						mod.makeRipe(evt.world, evt.x, evt.y, evt.z);
						flag = true;
					}
				}
			}
			if (flag) {
				if (evt.entityPlayer != null) {
					ItemStack is = evt.entityPlayer.getCurrentEquippedItem();
					if (is != null) {
						is.stackSize--;
						if (is.stackSize <= 0)
							evt.entityPlayer.setCurrentItemOrArmor(0, null);
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void makeMobsCreativeHostile(MobTargetingEvent.Pre evt) {
		if (LegacyOptions.HOSTILECREATIVE.getState()) {
			if (!evt.defaultResult && evt.getResult() != Result.DENY) {
				evt.setResult(Result.ALLOW);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void adjustLightMap(LightmapEvent evt) {
		if (LegacyOptions.OLDLIGHT.getState()) {
			int[] colors = Minecraft.getMinecraft().entityRenderer.lightmapColors;
			for (int i = 0; i < colors.length; i++) {
				int color = colors[i];
				int[] c = ReikaColorAPI.HexToRGB(color);
				int avg = (c[0]+c[1]+c[2])/3;
				color = 0xff000000 | ReikaColorAPI.GStoHex(avg);
				colors[i] = color;
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
