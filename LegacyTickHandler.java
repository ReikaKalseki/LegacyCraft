/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;
import Reika.LegacyCraft.Entity.EntityLegacyCreeper;
import Reika.LegacyCraft.Entity.EntityLegacyEnderman;
import Reika.LegacyCraft.Entity.EntityLegacySkeleton;
import Reika.LegacyCraft.Entity.EntityLegacyZombie;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class LegacyTickHandler implements ITickHandler {

	public static final LegacyTickHandler instance = new LegacyTickHandler();

	private LegacyTickHandler() {

	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		World world = (World)tickData[0];
		if (LegacyOptions.FORCEMOBS.getState()) {
			List<Entity> li = world.loadedEntityList;
			for (int i = 0; i < li.size(); i++) {
				Entity e = li.get(i);
				if (e.getClass() == EntityZombie.class) {
					this.convertZombie(world, (EntityZombie)e);
				}
				if (e.getClass() == EntitySkeleton.class) {
					this.convertSkeleton(world, (EntitySkeleton)e);
				}
				if (e.getClass() == EntityCreeper.class) {
					this.convertCreeper(world, (EntityCreeper)e);
				}
				if (e.getClass() == EntityEnderman.class) {
					this.convertEnderman(world, (EntityEnderman)e);
				}
			}
		}
	}

	private void convertEnderman(World world, EntityEnderman e) {
		EntityLegacyEnderman z = new EntityLegacyEnderman(e);
		e.setDead();
		if (!world.isRemote)
			world.spawnEntityInWorld(z);
	}

	private void convertZombie(World world, EntityZombie e) {
		EntityLegacyZombie z = new EntityLegacyZombie(e);
		e.setDead();
		if (!world.isRemote)
			world.spawnEntityInWorld(z);
	}

	private void convertSkeleton(World world, EntitySkeleton e) {
		EntityLegacySkeleton z = new EntityLegacySkeleton(e);
		e.setDead();
		if (!world.isRemote)
			world.spawnEntityInWorld(z);
	}

	private void convertCreeper(World world, EntityCreeper e) {
		EntityLegacyCreeper z = new EntityLegacyCreeper(e);
		e.setDead();
		if (!world.isRemote)
			world.spawnEntityInWorld(z);
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {

	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() {
		return "LegacyCraft";
	}

}
