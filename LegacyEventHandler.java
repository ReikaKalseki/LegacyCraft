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

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.client.Minecraft;
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.Event.AddRecipeEvent;
import Reika.DragonAPI.Instantiable.Event.MobTargetingEvent;
import Reika.DragonAPI.Instantiable.Event.Client.LightmapEvent;
import Reika.DragonAPI.Interfaces.Registry.ModCrop;
import Reika.DragonAPI.Libraries.ReikaRecipeHelper;
import Reika.DragonAPI.Libraries.IO.ReikaColorAPI;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaCropHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.TinkerBlockHandler;
import Reika.DragonAPI.ModRegistry.ModCropList;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyCreeper;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyEnderman;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacySkeleton;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyVillager;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyZombie;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class LegacyEventHandler {

	public static final LegacyEventHandler instance = new LegacyEventHandler();

	private LegacyEventHandler() {

	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void enforceMobs(EntityJoinWorldEvent evt) {
		if (LegacyOptions.FORCEMOBS.getState() && !evt.world.isRemote) {
			Entity e = evt.entity;
			if (e.getClass() == EntityZombie.class && MobOverrides.ZOMBIE.isActive()) {
				evt.world.spawnEntityInWorld(new EntityLegacyZombie((EntityZombie)e));
				evt.setCanceled(true);
			}
			else if (e.getClass() == EntitySkeleton.class) {
				if (MobOverrides.SKELETON.isActive()) {
					evt.world.spawnEntityInWorld(new EntityLegacySkeleton((EntitySkeleton)e));
					evt.setCanceled(true);
				}
				else {
					((EntitySkeleton)e).setSkeletonType(evt.world.provider.isHellWorld ? 1 : 0);
				}
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
				if (LegacyCraft.rand.nextInt(Math.max(1, 5-evt.lootingLevel)) == 0) //Same formula as TiC
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

	@SubscribeEvent(priority = EventPriority.LOWEST)
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
				evt.setResult(Result.ALLOW);
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

}
