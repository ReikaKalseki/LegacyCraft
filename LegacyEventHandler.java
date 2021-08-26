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

import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntityMob;
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
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;

import Reika.DragonAPI.Instantiable.Event.AddRecipeEvent;
import Reika.DragonAPI.Instantiable.Event.MobTargetingEvent;
import Reika.DragonAPI.Instantiable.Event.Client.LightmapEvent;
import Reika.DragonAPI.Interfaces.Registry.ModCrop;
import Reika.DragonAPI.Libraries.ReikaRecipeHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaCropHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.DragonAPI.ModRegistry.ModCropList;

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
	public void enforceMobs(LivingUpdateEvent evt) {
		if (evt.entityLiving instanceof EntityMob) {
			if (!LegacyOptions.MOBPICKUP.getState()) {
				boolean held = !(evt.entityLiving instanceof EntitySkeleton);
				for (int i = held ? 0 : 1; i <= 4; i++) {
					ItemStack is = evt.entityLiving.getEquipmentInSlot(i);
					if (is != null) {
						evt.entityLiving.setCurrentItemOrArmor(i, null);
						if (ReikaRandomHelper.doWithChance(evt.entityLiving.equipmentDropChances[i]))
							ReikaItemHelper.dropItem(evt.entityLiving.worldObj, evt.entityLiving.posX, evt.entityLiving.posY, evt.entityLiving.posZ, is);
					}
				}
			}
			if (evt.entityLiving instanceof EntitySkeleton) {
				EntitySkeleton es = (EntitySkeleton)evt.entityLiving;
				LegacyASMHooks.correctSkeletonType(es);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void enforceMobs(EntityJoinWorldEvent evt) {
		if (!evt.world.isRemote) {
			if (evt.entity instanceof EntityLiving) {
				EntityLiving e = (EntityLiving)evt.entity;
				if (e.canPickUpLoot() && !LegacyOptions.MOBPICKUP.getState())
					e.setCanPickUpLoot(false);
			}
			if (evt.entity instanceof EntityZombie) {
				EntityZombie e = (EntityZombie)evt.entity;
				this.filterAI(e.tasks);
				this.filterAI(e.targetTasks);
			}
			if (evt.entity instanceof EntityVillager) {
				EntityVillager ev = (EntityVillager)evt.entity;
				if (!LegacyOptions.ZOMBIEVILLAGER.getState() || !LegacyOptions.NEWAI.getState()) {
					Iterator<EntityAITaskEntry> it = ev.tasks.taskEntries.iterator();
					while (it.hasNext()) {
						EntityAITaskEntry ai = it.next();
						if (ai.action instanceof EntityAIAvoidEntity) {
							EntityAIAvoidEntity ea = (EntityAIAvoidEntity)ai.action;
							if (EntityZombie.class.isAssignableFrom(ea.targetEntityClass))
								it.remove();
						}
					}
				}
			}
		}
	}

	private void filterAI(EntityAITasks tasks) {
		Iterator<EntityAIBase> it = tasks.taskEntries.iterator();
		while (it.hasNext()) {
			EntityAIBase ai = it.next();
			if (!LegacyOptions.ZOMBIEDOOR.getState() && ai instanceof EntityAIBreakDoor)
				it.remove();
			else if (!LegacyOptions.ZOMBIEVILLAGER.getState() && ai instanceof EntityAIMoveThroughVillage)
				it.remove();
			else if (!LegacyOptions.ZOMBIEVILLAGER.getState() && ai instanceof EntityAIAttackOnCollide && ((EntityAIAttackOnCollide)ai).classTarget == EntityVillager.class)
				it.remove();
			else if (!LegacyOptions.ZOMBIEVILLAGER.getState() && ai instanceof EntityAINearestAttackableTarget && ((EntityAINearestAttackableTarget)ai).targetClass == EntityVillager.class)
				it.remove();
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
		if (e instanceof EntityBat && !LegacyOptions.BATS.getState()) {
			ev.setResult(Result.DENY);
		}
		else if (e instanceof EntityWitch && !LegacyOptions.WITCHES.getState()) {
			ev.setResult(Result.DENY);
		}
		else if (e instanceof EntityHorse && LegacyOptions.NOHORSES.getState()) {
			ev.setResult(Result.DENY);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void clearControlMobs(EntityJoinWorldEvent evt) {
		Entity e = evt.entity;
		if (e instanceof EntityHorse && LegacyOptions.NOHORSES.getState()) {
			evt.setCanceled(true);
		}
	}

	@SubscribeEvent
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
	public void noZombieBackup(SummonAidEvent ev) {
		if (LegacyOptions.BACKUP.getState())
			ev.setResult(Result.DENY);
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
