/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft.Entity;

import Reika.LegacyCraft.LegacyOptions;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.world.World;

public class EntityLegacySpider extends EntitySpider {

	public EntityLegacySpider(World par1World) {
		super(par1World);
	}

	@Override
	public EntityLivingData onSpawnWithEgg(EntityLivingData par1EntityLivingData)
	{
		Object par1EntityLivingData1 = super.onSpawnWithEgg(par1EntityLivingData);

		if (!LegacyOptions.SPIDERPOTIONS.getState())
			this.clearActivePotions();

		return (EntityLivingData)par1EntityLivingData1;
	}

}
