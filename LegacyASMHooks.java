package Reika.LegacyCraft;

import java.util.Collection;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import Reika.DragonAPI.Libraries.ReikaEntityHelper;

public class LegacyASMHooks {
	/*
	public static int getSugarcaneColorization(int orig, BlockReed b, IBlockAccess iba, int x, int y, int z) {
		if (b == Blocks.reeds && LegacyOptions.SUGARCANE.getState()) {
			return 0xffffff;
		}
		return orig;
	}
	 */

	public static String getVillagerSound(EntityVillager e, String orig) {
		return LegacyOptions.SILENTVILLAGERS.getState() ? null : orig;
	}

	public static String getFlintAndSteelSound() {
		return LegacyOptions.FLINTSOUND.getState() ? LegacyCraft.getOldFlintAndSteelSound() : "fire.ignite";
	}

	public static float getNonAIMoveSpeed() {
		return 0.2F;
	}

	public static float getAIMoveSpeed(float base) {
		return LegacyOptions.NEWAI.getState() ? base : getNonAIMoveSpeed();
	}

	public static EntityZombie.GroupData interceptZombieData(EntityZombie.GroupData data, EntityZombie e) {
		if (LegacyOptions.BABYZOMBIES.getState())
			data.field_142048_a = false;
		if (!LegacyOptions.ZOMBIEVILLAGER.getState())
			data.field_142046_b = false;
		return data;
	}

	public static IEntityLivingData onEntitySpawn(EntityLiving e, IEntityLivingData el) {
		if (e instanceof EntitySpider) {
			if (!LegacyOptions.SPIDERPOTIONS.getState())
				e.clearActivePotions();
		}
		if (e instanceof EntityZombie) {
			EntityZombie ez = (EntityZombie)e;
			if (LegacyOptions.OLDZOMBIES.getState()) {
				removeModifierByName(e, SharedMonsterAttributes.knockbackResistance, "Random spawn bonus");
				removeModifierByName(e, SharedMonsterAttributes.followRange, "Random zombie-spawn bonus");
				removeModifierByName(e, EntityZombie.field_110186_bp, "Leader zombie bonus");
				removeModifierByName(e, SharedMonsterAttributes.maxHealth, "Leader zombie bonus");
			}
			if (!LegacyOptions.ZOMBIEVILLAGER.getState())
				ez.setVillager(false);
			if (LegacyOptions.BABYZOMBIES.getState())
				ez.setChild(false);
			if (ez.ridingEntity != null)
				ez.mountEntity(null);
			if (!LegacyOptions.ZOMBIEDOOR.getState()) {
				ez.func_146070_a(false);
				ez.getNavigator().setBreakDoors(false);
			}
		}
		if (e instanceof EntitySkeleton) {
			correctSkeletonType((EntitySkeleton)e);
		}
		if (!LegacyOptions.MOBPICKUP.getState())
			e.setCanPickUpLoot(false);
		return el;
	}

	public static void applyEntityAttributes(EntityLiving e) {
		if (e instanceof EntityZombie) {
			if (LegacyOptions.OLDRANGE.getState())
				e.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16.0D);
			if (LegacyOptions.ZOMBIESUMMONS.getState())
				e.getEntityAttribute(EntityZombie.field_110186_bp).setBaseValue(0.0D);
		}
	}

	private static void removeModifierByName(EntityLiving e, IAttribute ia, String n) {
		IAttributeInstance iai = e.getEntityAttribute(ia);
		Collection<AttributeModifier> c = ReikaEntityHelper.getAttributeByName(iai, n);
		if (c != null) {
			for (AttributeModifier mod : c)
				iai.removeModifier(mod);
		}
	}

	public static void correctSkeletonType(EntitySkeleton es) {
		boolean hell = es.worldObj.provider.isHellWorld;
		if ((es.getSkeletonType() == 1) != hell) { //No normal archer skeletons in the nether
			es.setSkeletonType(hell ? 1 : 0);
			es.setCurrentItemOrArmor(0, new ItemStack(hell ? Items.stone_sword : Items.bow));
			es.setCombatTask();
		}
	}

}
