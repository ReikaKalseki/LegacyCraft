package Reika.LegacyCraft;

import net.minecraft.entity.EntityLivingBase;

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

}
