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

import net.minecraft.entity.IEntityLivingData;
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
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData par1IEntityLivingData)
	{
		Object par1IEntityLivingData1 = super.onSpawnWithEgg(par1IEntityLivingData);

		if (!LegacyOptions.SPIDERPOTIONS.getState())
			this.clearActivePotions();

		return (IEntityLivingData)par1IEntityLivingData1;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!LegacyOptions.MOBPICKUP.getState()) {
			for (int i = 0; i < 5; i++) {
				ItemStack is = this.getEquipmentInSlot(i);
				this.setCurrentItemOrArmor(i, null);
				if (ReikaRandomHelper.doWithChance(equipmentDropChances[i]))
					ReikaItemHelper.dropItem(worldObj, posX, posY, posZ, is);
			}
		}
	}

	@Override
	protected void enchantEquipment()
	{
		if (LegacyOptions.HELDENCHANT.getState()) {
			super.enchantEquipment();
		}
	}

}
