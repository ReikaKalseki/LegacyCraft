/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft.Overrides.Entity;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderHell;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.LegacyCraft.LegacyCraft;
import Reika.LegacyCraft.LegacyOptions;

public class EntityLegacySkeleton extends EntitySkeleton {

	private EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, 1.0D, 0, this.getAttackInterval(), 15.0F);
	private EntityAIAttackOnCollide aiAttackOnCollide = new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.2D, false);

	public EntityLegacySkeleton(World par1World) {
		super(par1World);
		/*
		tasks.taskEntries.clear();
		targetTasks.taskEntries.clear();
		if (LegacyOptions.NEWAI.getState()) {
			tasks.addTask(1, new EntityAISwimming(this));
			tasks.addTask(2, new EntityAIRestrictSun(this));
			tasks.addTask(3, new EntityAIFleeSun(this, 1.0D));
			tasks.addTask(5, new EntityAIWander(this, 1.0D));
			tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
			tasks.addTask(6, new EntityAILookIdle(this));
			targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		}
		targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));*/

		if (!LegacyOptions.MOBPICKUP.getState())
			this.setCanPickUpLoot(false);
	}

	public EntityLegacySkeleton(EntitySkeleton e) {
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
	public boolean isAIEnabled()
	{
		return LegacyOptions.NEWAI.getState();
	}

	private int getAttackInterval() {
		return LegacyOptions.ARROWSPEED.getState() ? 300 : 60;
	}

	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();
		if (!LegacyOptions.NEWAI.getState() && (worldObj.getWorldTime()%2 == 0 || !LegacyOptions.ARROWSPEED.getState())) {
			targetTasks.onUpdateTasks();
			this.updateAITick();
			tasks.onUpdateTasks();
		}
		if (this.getSkeletonType() != 1 && worldObj.provider.isHellWorld) { //No normal archer skeletons in the nether
			this.setSkeletonType(1);
			this.setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
		}
	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase par1EntityLivingBase, float par2)
	{
		EntityArrow entityarrow = new EntityArrow(worldObj, this, par1EntityLivingBase, 1.6F, 14 - worldObj.difficultySetting.ordinal() * 4);
		int i = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, this.getHeldItem());
		int j = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, this.getHeldItem());
		entityarrow.setDamage(par2 * 2.0F + rand.nextGaussian() * 0.25D + worldObj.difficultySetting.ordinal() * 0.11F);

		if (i > 0)
		{
			entityarrow.setDamage(entityarrow.getDamage() + i * 0.5D + 0.5D);
		}

		if (j > 0)
		{
			entityarrow.setKnockbackStrength(j);
		}

		if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, this.getHeldItem()) > 0 || this.getSkeletonType() == 1)
		{
			if (LegacyOptions.FIREARROWS.getState())
				entityarrow.setFire(100);
		}

		this.playSound("random.bow", 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
		worldObj.spawnEntityInWorld(entityarrow);
	}

	@Override
	public float getAIMoveSpeed()
	{
		return this.isAIEnabled() ? super.getAIMoveSpeed() : LegacyCraft.getNonAIMoveSpeed();
	}
	/*
	@Override
	public void setCombatTask()
	{
		tasks.removeTask(aiAttackOnCollide);
		tasks.removeTask(aiArrowAttack);
		ItemStack itemstack = this.getHeldItem();

		if (itemstack != null && itemstack.getItem() == Items.bow)
		{
			tasks.addTask(4, aiArrowAttack);
		}
		else
		{
			tasks.addTask(4, aiAttackOnCollide);
		}
	}*/

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData par1IEntityLivingData)
	{
		par1IEntityLivingData = super.onSpawnWithEgg(par1IEntityLivingData);

		if (worldObj.provider instanceof WorldProviderHell)
		{
			//tasks.addTask(4, aiAttackOnCollide);
			this.setSkeletonType(1);
			this.setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
		}
		else
		{
			//tasks.addTask(4, aiArrowAttack);
			this.setCurrentItemOrArmor(0, new ItemStack(Items.bow));
		}
		this.setCombatTask();

		if (!LegacyOptions.MOBPICKUP.getState())
			this.setCanPickUpLoot(false);

		return par1IEntityLivingData;
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

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!LegacyOptions.MOBPICKUP.getState()) {
			for (int i = 1; i < 5; i++) {
				ItemStack is = this.getEquipmentInSlot(i);
				this.setCurrentItemOrArmor(i, null);
				if (ReikaRandomHelper.doWithChance(equipmentDropChances[i]))
					ReikaItemHelper.dropItem(worldObj, posX, posY, posZ, is);
			}
		}
	}

}
