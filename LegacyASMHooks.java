package Reika.LegacyCraft;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;

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

	public static boolean enableNewAI(EntityLivingBase e) {
		return LegacyOptions.NEWAI.getState();
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
		if (!LegacyOptions.BABYZOMBIES.getState())
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
			if (!LegacyOptions.BABYZOMBIES.getState())
				ez.setChild(false);
			if (ez.ridingEntity != null)
				ez.mountEntity(null);
			if (!LegacyOptions.ZOMBIEDOOR.getState()) {
				ez.func_146070_a(false);
				ez.getNavigator().setBreakDoors(false);
			}
		}
		if (!LegacyOptions.MOBPICKUP.getState())
			e.setCanPickUpLoot(false);
		return el;
	}

	private static void removeModifierByName(EntityLiving e, IAttribute ia, String n) {
		IAttributeInstance iai = e.getEntityAttribute(ia);
		AttributeModifier mod = ReikaEntityHelper.getAttributeByName(iai, n);
		iai.removeModifier(mod);
	}

}
