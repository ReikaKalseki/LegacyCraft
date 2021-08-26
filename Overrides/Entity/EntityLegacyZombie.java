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

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import Reika.LegacyCraft.LegacyOptions;

public class EntityLegacyZombie extends EntityZombie {

	public EntityLegacyZombie(World par1World) {
		super(par1World);
		tasks.taskEntries.clear();
		targetTasks.taskEntries.clear();
		this.getNavigator().setBreakDoors(LegacyOptions.ZOMBIEDOOR.getState());
		tasks.addTask(0, new EntityAISwimming(this));
		if (LegacyOptions.ZOMBIEDOOR.getState())
			tasks.addTask(1, new EntityAIBreakDoor(this));
		tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		if (LegacyOptions.ZOMBIEVILLAGER.getState())
			tasks.addTask(3, new EntityAIAttackOnCollide(this, EntityVillager.class, 1.0D, true));
		tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 1.0D));
		if (LegacyOptions.ZOMBIEVILLAGER.getState())
			tasks.addTask(5, new EntityAIMoveThroughVillage(this, 1.0D, false));
		tasks.addTask(6, new EntityAIWander(this, 1.0D));
		tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		tasks.addTask(7, new EntityAILookIdle(this));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		if (LegacyOptions.ZOMBIEVILLAGER.getState())
			targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityVillager.class, 0, false));

	}

	public EntityLegacyZombie(EntityZombie e) {
		this(e.worldObj);
	}

	@Override
	public boolean attackEntityAsMob(Entity e)
	{
		boolean flag = super.attackEntityAsMob(e);
		if (flag && this.isBurning() && !LegacyOptions.ZOMBIEFIRE.getState()) {
			e.extinguish();
		}
		if (e instanceof EntityVillager && !LegacyOptions.ZOMBIEVILLAGER.getState()) {
			float f = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
			((EntityVillager)e).heal(f);
		}
		return flag;
	}

}
