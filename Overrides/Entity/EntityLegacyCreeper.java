/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft.Overrides.Entity;

import java.util.List;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Event.MobTargetingEvent;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.LegacyCraft.LegacyOptions;

import cpw.mods.fml.common.eventhandler.Event.Result;

public class EntityLegacyCreeper extends EntityCreeper {

	public EntityLegacyCreeper(World par1World) {
		super(par1World);

		if (!LegacyOptions.MOBPICKUP.getState())
			this.setCanPickUpLoot(false);
	}

	public EntityLegacyCreeper(EntityCreeper e) {
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
	public void onUpdate() {

		if (!worldObj.isRemote) {
			AxisAlignedBB box = AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX, posY+this.getEyeHeight(), posZ).expand(2, 2, 2);
			List<EntityPlayer> li = worldObj.getEntitiesWithinAABB(EntityPlayer.class, box);
			boolean flag = false;
			for (EntityPlayer ep : li) {
				if (!ep.isDead && ep.getHealth() > 0 && !ep.capabilities.isCreativeMode) {
					Result res = MobTargetingEvent.firePre(ep, worldObj, posX, posY, posZ, 2);
					if (res != Result.DENY)
						flag = true;
				}
			}
			if (flag) {
				this.setCreeperState(1);
				motionX = motionZ = 0;
			}
			else
				this.setCreeperState(-1);
		}

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
	protected void dropEquipment(boolean par1, int par2)
	{
		for (int j = 0; j < this.getLastActiveItems().length; ++j)
		{
			ItemStack itemstack = this.getEquipmentInSlot(j);
			boolean flag1 = equipmentDropChances[j] > 1.0F;

			if (itemstack != null && (par1 || flag1) && rand.nextFloat() - par2 * 0.01F < equipmentDropChances[j])
			{
				if (!flag1 && itemstack.isItemStackDamageable() && LegacyOptions.DAMAGEDDROPS.getState())
				{
					int k = Math.max(itemstack.getMaxDamage() - 25, 1);
					int l = itemstack.getMaxDamage() - rand.nextInt(rand.nextInt(k) + 1);

					if (l > k)
					{
						l = k;
					}

					if (l < 1)
					{
						l = 1;
					}

					itemstack.setItemDamage(l);
				}

				this.entityDropItem(itemstack, 0.0F);
			}
		}
	}

}
