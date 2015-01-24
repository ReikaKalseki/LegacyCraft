/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft.Overrides;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;

public class WorldGenCustomNetherLava extends WorldGenerator
{
	private Block genBlock;
	private boolean forceUpdate;
	private static final String __OBFID = "CL_00000414";

	public WorldGenCustomNetherLava(Block gen, boolean upd)
	{
		genBlock = Blocks.flowing_lava;//gen;
		forceUpdate = upd;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z)
	{
		if (!this.canGenerate(world, x, y, z)) {
			return false;
		}
		else {
			int c = 0;

			if (world.getBlock(x-1, y, z) == Blocks.netherrack) {
				c++;
			}
			if (world.getBlock(x+1, y, z) == Blocks.netherrack) {
				c++;
			}
			if (world.getBlock(x, y, z-1) == Blocks.netherrack) {
				c++;
			}
			if (world.getBlock(x, y, z+1) == Blocks.netherrack) {
				c++;
			}
			if (world.getBlock(x, y-1, z) == Blocks.netherrack) {
				c++;
			}

			int c2 = 0;

			if (world.isAirBlock(x-1, y, z)) {
				c2++;
			}
			if (world.isAirBlock(x+1, y, z)) {
				c2++;
			}
			if (world.isAirBlock(x, y, z-1)) {
				c2++;
			}
			if (world.isAirBlock(x, y, z+1)) {
				c2++;
			}
			if (world.isAirBlock(x, y-1, z)) {
				c2++;
			}

			if (!forceUpdate && c == 4 && c2 == 1 || c == 5) {
				world.setBlock(x, y, z, genBlock, 0, 2);
				world.scheduledUpdatesAreImmediate = true;
				genBlock.updateTick(world, x, y, z, rand);
				world.scheduledUpdatesAreImmediate = false;
			}

			return true;
		}
	}

	private boolean canGenerate(World world, int x, int y, int z) {
		if (world.getBlock(x, y+1, z) != Blocks.netherrack)
			return false;
		if (world.getBlock(x, y, z).getMaterial() != Material.air && world.getBlock(x, y, z) != Blocks.netherrack)
			return false;

		//------ custom ------//
		return ReikaWorldHelper.checkForAdjMaterial(world, x, y, z, Material.air) != null;
	}
}
