package Reika.LegacyCraft.Overrides;

import net.minecraft.block.BlockPortal;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class BlockClosedPortal extends BlockPortal {

	public BlockClosedPortal(int par1, Material mat) {
		super(par1);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity e) {
		if (e instanceof EntityPlayer) {
			super.onEntityCollidedWithBlock(world, x, y, z, e);
		}
	}
}
