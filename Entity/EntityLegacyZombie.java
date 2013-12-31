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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingData;
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
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import Reika.DragonAPI.Instantiable.ModifiableAttributeMap;
import Reika.LegacyCraft.LegacyCraft;
import Reika.LegacyCraft.LegacyOptions;

public class EntityLegacyZombie extends EntityZombie {

	private ModifiableAttributeMap map;

	public EntityLegacyZombie(World par1World) {
		super(par1World);
		tasks.taskEntries.clear();
		targetTasks.taskEntries.clear();
		this.getNavigator().setBreakDoors(LegacyOptions.ZOMBIEDOOR.getState());
		tasks.addTask(0, new EntityAISwimming(this));
		if (LegacyOptions.ZOMBIEDOOR.getState())
			tasks.addTask(1, new EntityAIBreakDoor(this));
		tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		if (LegacyOptions.VILLAGER.getState())
			tasks.addTask(3, new EntityAIAttackOnCollide(this, EntityVillager.class, 1.0D, true));
		tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 1.0D));
		if (LegacyOptions.VILLAGER.getState())
			tasks.addTask(5, new EntityAIMoveThroughVillage(this, 1.0D, false));
		tasks.addTask(6, new EntityAIWander(this, 1.0D));
		tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		tasks.addTask(7, new EntityAILookIdle(this));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		if (LegacyOptions.VILLAGER.getState())
			targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityVillager.class, 0, false));

		if (this.isVillager() && !LegacyOptions.VILLAGER.getState())
			this.setVillager(false);
		if (this.isChild() && !LegacyOptions.BABYZOMBIES.getState())
			this.setChild(false);

		if (!LegacyOptions.MOBPICKUP.getState())
			this.setCanPickUpLoot(false);
	}

	public EntityLegacyZombie(EntityZombie e) {
		this(e.worldObj);
		this.setPosition(e.posX, e.posY, e.posZ);
		motionX = e.motionX;
		motionY = e.motionY;
		motionZ = e.motionZ;
		for (int i = 0; i < 5; i++) {
			this.setCurrentItemOrArmor(i, e.getCurrentItemOrArmor(i));
		}
		this.setHealth(e.getHealth());
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		if (LegacyOptions.OLDRANGE.getState()) {
			this.getEntityAttribute(SharedMonsterAttributes.followRange).setAttribute(16.0D);
		}

		if (LegacyOptions.BABYZOMBIES.getState())
			this.getAttributeMap().func_111150_b(field_110186_bp).setAttribute(0.0D);

		if (this.isVillager() && !LegacyOptions.VILLAGER.getState())
			this.setVillager(false);
		if (this.isChild() && !LegacyOptions.BABYZOMBIES.getState())
			this.setChild(false);
	}

	@Override
	public BaseAttributeMap getAttributeMap()
	{
		super.getAttributeMap(); /*to avoid NPE due to bad coding:
		java.lang.NullPointerException
		2013-12-07 10:25:41 [INFO] [STDOUT] 	at net.minecraft.entity.EntityLivingBase.onUpdate(EntityLivingBase.java:1819)
		2013-12-07 10:25:41 [INFO] [STDOUT] 	at net.minecraft.entity.EntityLiving.onUpdate(EntityLiving.java:256)*/
		if (map == null)
			map = new ModifiableAttributeMap();
		return map;
	}

	@Override
	protected boolean isAIEnabled()
	{
		return LegacyOptions.NEWAI.getState();
	}

	@Override
	public float getAIMoveSpeed()
	{
		return this.isAIEnabled() ? super.getAIMoveSpeed() : LegacyCraft.getNonAIMoveSpeed();
	}

	@Override
	public boolean attackEntityAsMob(Entity e)
	{
		boolean flag = super.attackEntityAsMob(e);
		if (flag && this.isBurning() && !LegacyOptions.ZOMBIEFIRE.getState()) {
			e.extinguish();
		}
		if (e instanceof EntityVillager && !LegacyOptions.VILLAGER.getState()) {
			float f = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
			((EntityVillager)e).heal(f);
		}
		return flag;
	}

	@Override
	public EntityLivingData onSpawnWithEgg(EntityLivingData par1EntityLivingData)
	{
		Object par1EntityLivingData1 = super.onSpawnWithEgg(par1EntityLivingData);

		this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).removeModifier(new AttributeModifier("Random spawn bonus", rand.nextDouble() * 0.05000000074505806D, 0));
		this.getEntityAttribute(SharedMonsterAttributes.followRange).removeModifier(new AttributeModifier("Random zombie-spawn bonus", rand.nextDouble() * 1.5D, 2));

		this.getEntityAttribute(field_110186_bp).removeModifier(new AttributeModifier("Leader zombie bonus", rand.nextDouble() * 0.25D + 0.5D, 0));
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).removeModifier(new AttributeModifier("Leader zombie bonus", rand.nextDouble() * 3.0D + 1.0D, 2));

		if (this.isVillager() && !LegacyOptions.VILLAGER.getState())
			this.setVillager(false);
		if (this.isChild() && !LegacyOptions.BABYZOMBIES.getState())
			this.setChild(false);

		if (!LegacyOptions.MOBPICKUP.getState())
			this.setCanPickUpLoot(false);

		return (EntityLivingData)par1EntityLivingData1;
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
