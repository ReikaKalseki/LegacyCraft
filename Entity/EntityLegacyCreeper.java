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

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import Reika.DragonAPI.Libraries.Java.ReikaObfuscationHelper;
import Reika.LegacyCraft.LegacyCraft;
import Reika.LegacyCraft.LegacyOptions;

public class EntityLegacyCreeper extends EntityCreeper {

	public EntityLegacyCreeper(World par1World) {
		super(par1World);

		if (!LegacyOptions.MOBPICKUP.getState())
			this.setCanPickUpLoot(false);
	}

	@Override
	public boolean isAIEnabled()
	{
		return LegacyOptions.NEWAI.getState();
	}

	@Override
	protected void fall(float par1)
	{
		super.fall(par1);
		if (!LegacyOptions.CREEPERFALL.getState()) {
			Field f = ReikaObfuscationHelper.getField("timeSinceIgnited");
			try {
				f.set(this, 0);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onUpdate() {

		AxisAlignedBB box = AxisAlignedBB.getAABBPool().getAABB(posX, posY, posZ, posX, posY+this.getEyeHeight(), posZ).expand(2, 2, 2);
		List<EntityPlayer> li = worldObj.getEntitiesWithinAABB(EntityPlayer.class, box);
		boolean flag = false;
		for (int i = 0; i < li.size(); i++) {
			EntityPlayer ep = li.get(0);
			if (!ep.isDead && ep.getHealth() > 0 && !ep.capabilities.isCreativeMode)
				flag = true;
		}
		if (flag) {
			this.setCreeperState(1);
			motionX = motionZ = 0;
		}
		else
			this.setCreeperState(-1);

		super.onUpdate();
	}

	@Override
	public float getAIMoveSpeed()
	{
		return this.isAIEnabled() ? super.getAIMoveSpeed() : LegacyCraft.getNonAIMoveSpeed();
	}

	@Override
	protected void dropEquipment(boolean par1, int par2)
	{
		for (int j = 0; j < this.getLastActiveItems().length; ++j)
		{
			ItemStack itemstack = this.getCurrentItemOrArmor(j);
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
