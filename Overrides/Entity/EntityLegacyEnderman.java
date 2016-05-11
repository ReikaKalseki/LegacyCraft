/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft.Overrides.Entity;

import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
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
			this.setCurrentItemOrArmor(i, e.getEquipmentInSlot(i));
		}
		this.setHealth(e.getHealth());
	}

	@Override
	public void onLivingUpdate() {
		if (!LegacyOptions.ENDERSOUNDS.getState())
			stareTimer = 100000;

		super.onLivingUpdate();
	}

	@Override
	public boolean isScreaming()
	{
		return LegacyOptions.ENDERSOUNDS.getState() && super.isScreaming();
	}

	public int getStareTimer() {
		return stareTimer;
	}

	public boolean isAggressive() {
		return isAggressive;
	}

	static
	{
		if (LegacyOptions.ENDERBLOCKS.getState()) {
			setCarriable(Blocks.stone, true);
			setCarriable(Blocks.planks, true);
			setCarriable(Blocks.cobblestone, true);
			setCarriable(Blocks.stonebrick, true);
		}
	}

}
