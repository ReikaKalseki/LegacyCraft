package Reika.LegacyCraft;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.ReikaEntityHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;

public class LegacyASMHooks {
	/*
	public static int getSugarcaneColorization(int orig, BlockReed b, IBlockAccess iba, int x, int y, int z) {
		if (b == Blocks.reeds && LegacyOptions.SUGARCANE.getState()) {
			return 0xffffff;
		}
		return orig;
	}
	 */

	public static String getVillagerSound(String orig, EntityVillager e) {
		return LegacyOptions.SILENTVILLAGERS.getState() ? null : orig;
	}

	public static String getFlintAndSteelSound() {
		return LegacyOptions.FLINTSOUND.getState() ? LegacyCraft.getOldFlintAndSteelSound() : "fire.ignite";
	}

	public static float getNonAIMoveSpeed() {
		return 0.2F;
	}

	public static float getAIMoveSpeed(float base) {
		return LegacyOptions.NEWAI.getState() ? base : getNonAIMoveSpeed();
	}

	public static EntityZombie.GroupData interceptZombieData(EntityZombie.GroupData data, EntityZombie e) {
		if (LegacyOptions.BABYZOMBIES.getState())
			data.field_142048_a = false;
		if (!LegacyOptions.ZOMBIEVILLAGER.getState())
			data.field_142046_b = false;
		return data;
	}

	public static IEntityLivingData onEntitySpawn(EntityLiving e, IEntityLivingData el) {
		if (e instanceof EntitySpider) {
			if (!LegacyOptions.SPIDERPOTIONS.getState())
				e.clearActivePotions();
		}
		if (e instanceof EntityZombie) {
			EntityZombie ez = (EntityZombie)e;
			if (LegacyOptions.OLDZOMBIES.getState()) {
				removeModifierByName(e, SharedMonsterAttributes.knockbackResistance, "Random spawn bonus");
				removeModifierByName(e, SharedMonsterAttributes.followRange, "Random zombie-spawn bonus");
				removeModifierByName(e, EntityZombie.field_110186_bp, "Leader zombie bonus");
				removeModifierByName(e, SharedMonsterAttributes.maxHealth, "Leader zombie bonus");
			}
			if (!LegacyOptions.ZOMBIEVILLAGER.getState())
				ez.setVillager(false);
			if (LegacyOptions.BABYZOMBIES.getState())
				ez.setChild(false);
			if (ez.ridingEntity != null)
				ez.mountEntity(null);
			if (!LegacyOptions.ZOMBIEDOOR.getState()) {
				ez.func_146070_a(false);
				ez.getNavigator().setBreakDoors(false);
			}
		}
		if (e instanceof EntitySkeleton) {
			correctSkeletonType((EntitySkeleton)e);
		}
		if (!LegacyOptions.MOBPICKUP.getState())
			e.setCanPickUpLoot(false);
		return el;
	}

	public static void applyEntityAttributes(EntityLiving e) {
		if (e instanceof EntityZombie) {
			if (LegacyOptions.OLDRANGE.getState())
				e.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16.0D);
			if (LegacyOptions.BACKUP.getState())
				e.getEntityAttribute(EntityZombie.field_110186_bp).setBaseValue(0.0D);
		}
	}

	private static void removeModifierByName(EntityLiving e, IAttribute ia, String n) {
		IAttributeInstance iai = e.getEntityAttribute(ia);
		Collection<AttributeModifier> c = ReikaEntityHelper.getAttributeByName(iai, n);
		if (c != null) {
			for (AttributeModifier mod : c)
				iai.removeModifier(mod);
		}
	}

	public static void correctSkeletonType(EntitySkeleton es) {
		boolean hell = es.worldObj.provider.isHellWorld;
		if ((es.getSkeletonType() == 1) != hell) { //No normal archer skeletons in the nether
			es.setSkeletonType(hell ? 1 : 0);
			es.setCurrentItemOrArmor(0, new ItemStack(hell ? Items.stone_sword : Items.bow));
			es.setCombatTask();
		}
	}

	public static PathEntity getPathEntityToEntity(World world, Entity src, Entity tgt, float dist, boolean useOpenDoors, boolean useClosedDoors, boolean avoidWater, boolean canSwim) {
		int x0 = MathHelper.floor_double(src.posX);
		int y0 = MathHelper.floor_double(src.posY + 1.0D);
		int z0 = MathHelper.floor_double(src.posZ);
		if (!LegacyOptions.NEWAI.getState() && src instanceof EntityMob) {
			return getDumbPath(world, src, MathHelper.floor_double(tgt.posX), MathHelper.floor_double(tgt.posY), MathHelper.floor_double(tgt.posZ), dist);
		}
		world.theProfiler.startSection("pathfind");
		int sr = (int)(dist + 16.0F);
		int x1 = x0 - sr;
		int y1 = y0 - sr;
		int z1 = z0 - sr;
		int x2 = x0 + sr;
		int y2 = y0 + sr;
		int z2 = z0 + sr;
		ChunkCache chunkcache = new ChunkCache(world, x1, y1, z1, x2, y2, z2, 0);
		PathEntity pathentity = (new PathFinder(chunkcache, useOpenDoors, useClosedDoors, avoidWater, canSwim)).createEntityPathTo(src, tgt, dist);
		world.theProfiler.endSection();
		return pathentity;
	}

	public static PathEntity getEntityPathToXYZ(World world, Entity src, int x, int y, int z, float dist, boolean useOpenDoors, boolean useClosedDoors, boolean avoidWater, boolean canSwim) {
		if (!LegacyOptions.NEWAI.getState() && src instanceof EntityMob) {
			return getDumbPath(world, src, x, y, z, dist);
		}
		world.theProfiler.startSection("pathfind");
		int x0 = MathHelper.floor_double(src.posX);
		int y0 = MathHelper.floor_double(src.posY);
		int z0 = MathHelper.floor_double(src.posZ);
		int sr = (int)(dist + 8.0F);
		int x1 = x0 - sr;
		int y1 = y0 - sr;
		int z1 = z0 - sr;
		int x2 = x0 + sr;
		int y2 = y0 + sr;
		int z2 = z0 + sr;
		ChunkCache chunkcache = new ChunkCache(world, x1, y1, z1, x2, y2, z2, 0);
		PathEntity pathentity = (new PathFinder(chunkcache, useOpenDoors, useClosedDoors, avoidWater, canSwim)).createEntityPathTo(src, x, y, z, dist);
		world.theProfiler.endSection();
		return pathentity;
	}

	private static PathEntity getDumbPath(World world, Entity src, int x0, int y0, int z0, double dist) {
		double dx = x0+0.5-src.posX;
		double dy = y0-src.posY;
		double dz = z0+0.5-src.posZ;
		double dd = ReikaMathLibrary.py3d(dx, dy, dz);
		if (dd > dist)
			return null;
		double dl = 0.25D/dd;
		ArrayList<PathPoint> li = new ArrayList();
		Path p = new Path(); //this is necessary to init the points
		Coordinate cur = null;
		for (double d = 0; d <= 1; d += dl) {
			double x = x0+dx*d;
			double y = y0+dy*d;
			double z = z0+dz*d;
			Coordinate c2 = new Coordinate(x, y, z);
			if (!c2.equals(cur)) {
				cur = c2;
				PathPoint pp = new PathPoint(c2.xCoord, c2.yCoord, c2.zCoord);
				li.add(pp);
				p.addPoint(pp);
			}
		}
		return new PathEntity(li.toArray(new PathPoint[li.size()]));
	}

}
