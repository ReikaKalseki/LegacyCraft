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

import net.minecraftforge.common.config.Configuration;
import Reika.DragonAPI.Exception.RegistrationException;
import Reika.DragonAPI.Interfaces.ConfigList;

public enum LegacyOptions implements ConfigList {

	NEWAI("New Mob AI", false), //1.2
	BABYZOMBIES("Disable Baby Zombies", true), //1.6
	OLDRANGE("Old Zombie Sight Range", true), //1.6
	BONEMEAL("Old Bonemeal Mechanics", true), //1.5
	VILLAGER("Zombies Target Villagers", false), //1.2
	BACKUP("Disable Zombie Reinforcements", true), //1.6
	ZOMBIEDOOR("Zombies Break Doors", false), //1.2
	ZOMBIEFIRE("Zombies Attack with Fire", false), //1.4
	FIREARROWS("Allow Skeleton Flaming Arrows", false), //1.4
	ARROWSPEED("Old Skeleton Fire Rate", true), //1.4
	CREEPERFALL("Creepers Explode on Fall", false), //1.5
	BATS("Spawn Bats", false), //1.4
	WITCHES("Spawn Witches", false), //1.4
	DAMAGEDDROPS("Damaged Mob Weapon Drops", false), //1.4
	MOBPICKUP("Mobs Pick Up Drops", false), //1.4
	OLDPOTIONS("Old Regen and Heal Potions", true), //1.6
	GOLDENAPPLE("Golden Apple Level 0 Uses Nuggets", true), //1.6
	HIDDENLAVA("Disable Nether Hidden Lava Pockets", true), //1.5
	SPIDERPOTIONS("Spider Potion Effects on Spawn", false), //1.6
	FORCEMOBS("Enforced Mob Controls", true),
	ENDERSOUNDS("New Angry Enderman Sounds", true), //1.4
	ENDERBLOCKS("Allow Enderman Pickup of Cobble, Planks And Stone", false), //b1.9
	ENDERDAY("Disable Random Enderman Teleporting in Daylight", true), //1.2?
	OLDBOOK("Old Book Recipe", true), //1.4
	OLDMELON("Old Glistering Melon Recipe", true), //1.6
	SILVERFISH("Disable silverfish stone in Extreme Hills", true), //1.4
	OLDFIRE("Original Fire Spread", false), //b1.6
	CLOSEDPORTALS("Disable Entities Travelling Through Portals", false), //1.3
	PIGPORTALS("Disable Portal Pigmen Spawns", true), //1.4?
	SILENTVILLAGERS("Disable Villager Noises", false), //1.6
	SUGARCANE("Disable Biome Colors on Sugarcane", true), //1.7
	ROSES("Turn poppies into roses", true), //1.7
	OLDLIGHT("Disable yellowed block light", false), //b1.8
	ALPHAGRASS("Alpha Grass and Leaf Color", false), //a1.2
	SHEEPUNCH("Old Sheep Wool Harvesting", false), //b1.7
	ANIMALSPAWN("Pre Adventure Update Animal Spawning", false); //b1.8

	private String label;
	private boolean defaultState;
	private int defaultValue;
	private float defaultFloat;
	private Class type;

	public static final LegacyOptions[] optionList = LegacyOptions.values();

	private LegacyOptions(String l, boolean d) {
		label = l;
		defaultState = d;
		type = boolean.class;
	}

	private LegacyOptions(String l, int d) {
		label = l;
		defaultValue = d;
		type = int.class;
	}

	public boolean isBoolean() {
		return type == boolean.class;
	}

	public boolean isNumeric() {
		return type == int.class;
	}

	public boolean isDecimal() {
		return type == float.class;
	}

	public float setDecimal(Configuration config) {
		if (!this.isDecimal())
			throw new RegistrationException(LegacyCraft.instance, "Config Property \""+this.getLabel()+"\" is not decimal!");
		return (float)config.get("Control Setup", this.getLabel(), defaultFloat).getDouble(defaultFloat);
	}

	public float getFloat() {
		return (Float)LegacyCraft.config.getControl(this.ordinal());
	}

	public Class getPropertyType() {
		return type;
	}

	public int setValue(Configuration config) {
		if (!this.isNumeric())
			throw new RegistrationException(LegacyCraft.instance, "Config Property \""+this.getLabel()+"\" is not numerical!");
		return config.get("Control Setup", this.getLabel(), defaultValue).getInt();
	}

	public String getLabel() {
		return label;
	}

	public boolean setState(Configuration config) {
		if (!this.isBoolean())
			throw new RegistrationException(LegacyCraft.instance, "Config Property \""+this.getLabel()+"\" is not boolean!");
		return config.get("Control Setup", this.getLabel(), defaultState).getBoolean(defaultState);
	}

	public boolean getState() {
		return (Boolean)LegacyCraft.config.getControl(this.ordinal());
	}

	public int getValue() {
		return (Integer)LegacyCraft.config.getControl(this.ordinal());
	}

	public boolean isDummiedOut() {
		return type == null;
	}

	@Override
	public boolean getDefaultState() {
		return defaultState;
	}

	@Override
	public int getDefaultValue() {
		return defaultValue;
	}

	@Override
	public float getDefaultFloat() {
		return defaultFloat;
	}

	@Override
	public boolean isEnforcingDefaults() {
		return false;
	}

	@Override
	public boolean shouldLoad() {
		return true;
	}

}
