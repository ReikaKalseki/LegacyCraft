package Reika.LegacyCraft.Overrides;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.material.Material;


public class BlockLegacyDynamicLiquid extends BlockDynamicLiquid {

	public BlockLegacyDynamicLiquid(Material m) {
		super(m);
	}

	@Override
	public Block disableStats() {
		return super.disableStats();
	}

}
