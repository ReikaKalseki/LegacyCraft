/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft;

import net.minecraft.potion.Potion;

public class LegacyPotionRegen extends Potion {

	public LegacyPotionRegen() {
		super(Potion.regeneration.id, false, Potion.regeneration.getLiquidColor());
	}

	@Override
	public boolean isReady(int par1, int par2)
	{
		int k = this.getInterval() >> par2;
		return k > 0 ? par1 % k == 0 : true;
	}

	private int getInterval() {
		return LegacyOptions.OLDPOTIONS.getState() ? 25 : 50;
	}
}
