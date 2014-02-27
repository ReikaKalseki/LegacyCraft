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

import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.LegacyCraft.LegacyOptions;

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

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!LegacyOptions.MOBPICKUP.getState()) {
			for (int i = 0; i < 5; i++) {
				ItemStack is = this.getCurrentItemOrArmor(i);
				this.setCurrentItemOrArmor(i, null);
				if (ReikaRandomHelper.doWithChance(equipmentDropChances[i]))
					ReikaItemHelper.dropItem(worldObj, posX, posY, posZ, is);
			}
		}
	}

}
