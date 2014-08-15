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

import Reika.DragonAPI.Auxiliary.TickRegistry.TickHandler;
import Reika.DragonAPI.Auxiliary.TickRegistry.TickType;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyCreeper;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyEnderman;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacySkeleton;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyVillager;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyZombie;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.world.World;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class LegacyTickHandler implements TickHandler {

	public static final LegacyTickHandler instance = new LegacyTickHandler();

	private LegacyTickHandler() {

	}

	@Override
	public void tick(Object... tickData) {
		World world = (World)tickData[0];
		if (LegacyOptions.FORCEMOBS.getState()) {
			List<Entity> li = world.loadedEntityList;
			for (int i = 0; i < li.size(); i++) {
				Entity e = li.get(i);
				if (e.getClass() == EntityZombie.class) {
					this.convertZombie(world, (EntityZombie)e);
				}
				else if (e.getClass() == EntitySkeleton.class) {
					this.convertSkeleton(world, (EntitySkeleton)e);
				}
				else if (e.getClass() == EntityCreeper.class) {
					this.convertCreeper(world, (EntityCreeper)e);
				}
				else if (e.getClass() == EntityEnderman.class) {
					this.convertEnderman(world, (EntityEnderman)e);
				}
				else if (e.getClass() == EntityVillager.class) {
					this.convertVillager(world, (EntityVillager)e);
				}
			}
		}
	}

	private void convertVillager(World world, EntityVillager e) {
		EntityLegacyVillager z = new EntityLegacyVillager(e);
		e.setDead();
		if (!world.isRemote)
			world.spawnEntityInWorld(z);
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
	public TickType getType() {
		return TickType.WORLD;
	}

	@Override
	public Phase getPhase() {
		return Phase.START;
	}

	@Override
	public String getLabel() {
		return "LegacyCraft";
	}

}