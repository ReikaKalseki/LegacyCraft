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

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyCreeper;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyEnderman;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacySkeleton;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacySpider;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyVillager;
import Reika.LegacyCraft.Overrides.Entity.EntityLegacyZombie;

public enum MobOverrides {

	CREEPER(EntityLegacyCreeper.class, "Creeper", 50, 894731, 0),
	ZOMBIE(EntityLegacySkeleton.class, "Skeleton", 51, 12698049, 4802889),
	SKELETON(EntityLegacyZombie.class, "Zombie", 54, 44975, 7969893),
	SPIDER(EntityLegacySpider.class, "Spider", 52, 3419431, 11013646),
	ENDERMAN(EntityLegacyEnderman.class, "Enderman", 58, 1447446, 0),
	VILLAGER(EntityLegacyVillager.class, "Villager", 120, 5651507, 12422002);

	private final Class mobClass;
	public final String name;
	public final int entityID;
	public final int eggColor1;
	public final int eggColor2;

	public static final MobOverrides[] mobList = values();

	private MobOverrides(Class<? extends EntityLiving> c, String n, int id, int c1, int c2) {
		mobClass = c;
		name = n;
		entityID = id;
		eggColor1 = c1;
		eggColor2 = c2;
	}

	public void register() {
		EntityList.addMapping(mobClass, name, entityID, eggColor1, eggColor2);
	}

	public static void registerAll() {
		for (int i = 0; i < mobList.length; i++) {
			MobOverrides mob = mobList[i];
			if (LegacyCraft.config.overrideMob(mob)) {
				mob.register();
			}
		}
	}

}
