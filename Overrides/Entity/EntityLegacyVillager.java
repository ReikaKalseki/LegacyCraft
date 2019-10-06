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

import java.util.Iterator;

import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.world.World;

import Reika.LegacyCraft.LegacyOptions;

public class EntityLegacyVillager extends EntityVillager {

	public EntityLegacyVillager(World par1World) {
		super(par1World);
	}

	public EntityLegacyVillager(EntityVillager e) {
		this(e.worldObj);
		this.setPosition(e.posX, e.posY, e.posZ);
		motionX = e.motionX;
		motionY = e.motionY;
		motionZ = e.motionZ;
		for (int i = 0; i < 5; i++) {
			this.setCurrentItemOrArmor(i, e.getEquipmentInSlot(i));
		}
		this.setHealth(e.getHealth());

		if (!LegacyOptions.ZOMBIEVILLAGER.getState() || !LegacyOptions.NEWAI.getState()) {
			Iterator<EntityAITaskEntry> it = tasks.taskEntries.iterator();
			while (it.hasNext()) {
				EntityAITaskEntry ai = it.next();
				if (ai.action instanceof EntityAIAvoidEntity) {
					EntityAIAvoidEntity ea = (EntityAIAvoidEntity)ai.action;
					if (EntityZombie.class.isAssignableFrom(ea.targetEntityClass))
						it.remove();
				}
			}
		}
	}

	@Override
	public boolean isAIEnabled()
	{
		return true;//LegacyOptions.NEWAI.getState();
	}

	@Override
	protected String getLivingSound()
	{
		return LegacyOptions.SILENTVILLAGERS.getState() ? null : super.getLivingSound();
	}

	@Override
	protected String getHurtSound()
	{
		return LegacyOptions.SILENTVILLAGERS.getState() ? null : super.getHurtSound();
	}

	@Override
	protected String getDeathSound()
	{
		return LegacyOptions.SILENTVILLAGERS.getState() ? null : super.getDeathSound();
	}

}
