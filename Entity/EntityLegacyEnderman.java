/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft.Entity;

import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.world.World;
import Reika.LegacyCraft.LegacyOptions;

public class EntityLegacyEnderman extends EntityEnderman {

	public EntityLegacyEnderman(World par1World) {
		super(par1World);
	}

	public EntityLegacyEnderman(EntityEnderman e) {
		this(e.worldObj);
		this.setPosition(e.posX, e.posY, e.posZ);
		motionX = e.motionX;
		motionY = e.motionY;
		motionZ = e.motionZ;
		for (int i = 0; i < 5; i++) {
			this.setCurrentItemOrArmor(i, e.getCurrentItemOrArmor(i));
		}
		this.setHealth(e.getHealth());
	}

	@Override
	public boolean isScreaming()
	{
		return LegacyOptions.ENDERSOUNDS.getState() && super.isScreaming();
	}
	/*
	private void setStareTimer(int time) {
		ReikaObfuscationHelper.getField("stareTimer").set(this, time);
	}

	public int getStareTimer() {
		return ReikaObfuscationHelper.getField("stareTimer").getInt(this);
	}

	private void setAggressive(boolean agg) {
		ReikaObfuscationHelper.getField("isAggressive").set(this, agg);
	}

	public boolean isAggressive() {
		return ReikaObfuscationHelper.getField("isAggressive").getBoolean(this);
	}
	 */
	static
	{
		if (LegacyOptions.ENDERBLOCKS.getState()) {
			carriableBlocks[Block.stone.blockID] = true;
			carriableBlocks[Block.planks.blockID] = true;
			carriableBlocks[Block.cobblestone.blockID] = true;
			carriableBlocks[Block.stoneBrick.blockID] = true;
		}
	}

}
