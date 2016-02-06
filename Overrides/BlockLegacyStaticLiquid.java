package Reika.LegacyCraft.Overrides;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;


public class BlockLegacyStaticLiquid extends BlockStaticLiquid {

	public BlockLegacyStaticLiquid(Material m) {
		super(m);
	}

	@Override
	public Block disableStats() {
		return super.disableStats();
	}

}
