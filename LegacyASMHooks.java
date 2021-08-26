package Reika.LegacyCraft;

import net.minecraft.block.BlockReed;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;

public class LegacyASMHooks {

	public static int getSugarcaneColorization(int orig, BlockReed b, IBlockAccess iba, int x, int y, int z) {
		if (b == Blocks.reeds && LegacyOptions.SUGARCANE.getState()) {
			return 0xffffff;
		}
		return orig;
	}

}
