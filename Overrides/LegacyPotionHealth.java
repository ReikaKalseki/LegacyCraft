/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft.Overrides;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionHealth;
import net.minecraft.util.DamageSource;
import Reika.LegacyCraft.LegacyOptions;

public class LegacyPotionHealth extends PotionHealth {

	public LegacyPotionHealth() {
		super(Potion.heal.id, false, Potion.heal.getLiquidColor());
	}

	@Override
	public void performEffect(EntityLivingBase par1EntityLivingBase, int par2)
	{
		if ((id != heal.id || par1EntityLivingBase.isEntityUndead()) && (id != harm.id || !par1EntityLivingBase.isEntityUndead()))
		{
			if (id == harm.id && !par1EntityLivingBase.isEntityUndead() || id == heal.id && par1EntityLivingBase.isEntityUndead())
			{
				par1EntityLivingBase.attackEntityFrom(DamageSource.magic, this.getAttackAmount() << par2);
			}
		}
		else
		{
			par1EntityLivingBase.heal(Math.max(this.getHealAmount() << par2, 0));
		}
	}

	private int getAttackAmount() {
		return LegacyOptions.OLDPOTIONS.getState() ? 8 : 6;
	}

	private int getHealAmount() {
		return LegacyOptions.OLDPOTIONS.getState() ? 6 : 4;
	}

	/**
	 * Hits the provided entity with this potion's instant effect.
	 */
	@Override
	public void affectEntity(EntityLivingBase par1EntityLivingBase, EntityLivingBase par2EntityLivingBase, int par3, double par4)
	{
		int j;

		if ((id != heal.id || par2EntityLivingBase.isEntityUndead()) && (id != harm.id || !par2EntityLivingBase.isEntityUndead()))
		{
			if (id == harm.id && !par2EntityLivingBase.isEntityUndead() || id == heal.id && par2EntityLivingBase.isEntityUndead())
			{
				j = (int)(par4 * (this.getAttackAmount() << par3) + 0.5D);

				if (par1EntityLivingBase == null)
				{
					par2EntityLivingBase.attackEntityFrom(DamageSource.magic, j);
				}
				else
				{
					par2EntityLivingBase.attackEntityFrom(DamageSource.causeIndirectMagicDamage(par2EntityLivingBase, par1EntityLivingBase), j);
				}
			}
		}
		else
		{
			j = (int)(par4 * (this.getHealAmount() << par3) + 0.5D);
			par2EntityLivingBase.heal(j);
		}
	}

}
