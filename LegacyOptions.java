/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft;

import Reika.DragonAPI.Interfaces.Configuration.BooleanConfig;

public enum LegacyOptions implements BooleanConfig {

	NEWAI("New Mob AI", false), //1.2 - Whether to enable the smarter mob AI added in that version. If disabled restores the classic "they walk straight at you and will fall into a pit" behavior.
	BABYZOMBIES("Disable Baby Zombies", true), //1.6 - Set to true to revert to behavior prior to that version
	OLDRANGE("Old Zombie Sight Range", true), //1.6 - Whether to revert the increased sight range zombies were given in that version
	OLDZOMBIES("Old Zombie HP/Damage", true), //1.6 - Set to true to revert to behavior prior to that version
	BONEMEAL("Old Bonemeal Mechanics", true), //1.5 - Whether to restore "always works" bonemeal
	ZOMBIEVILLAGER("Zombies Target Villagers", false), //1.2 - Set to false to revert to behavior prior to that version
	BACKUP("Disable Zombie Reinforcements", true), //1.6 - Set to true to revert to behavior prior to that version
	ZOMBIEDOOR("Zombies Break Doors", false), //1.2 - Set to false to revert to behavior prior to that version
	ZOMBIETOOLS("Zombies Can Spawn With Tools", false), //1.4 - Set to false to revert to behavior prior to that version
	MOBARMOR("Mobs Can Spawn With Armor", false), //1.4 - Set to false to revert to behavior prior to that version
	ZOMBIEFIRE("Zombies Attack with Fire", false), //1.4 - Whether an on-fire zombie will set you on fire if they attack, as was added in that version
	FIREARROWS("Allow Skeleton Flaming Arrows", false), //1.4 - Set to false to revert to behavior prior to that version
	ARROWSPEED("Old Skeleton Fire Rate", true), //1.4 - Set to true to revert to behavior prior to that version
	CREEPERFALL("Creepers Explode on Fall", false), //1.5 - Set to false to revert to behavior prior to that version
	BATS("Spawn Bats", false), //1.4 - Set to false to revert to behavior prior to that version
	WITCHES("Spawn Witches", false), //1.4 - Set to false to revert to behavior prior to that version
	DAMAGEDDROPS("Damaged Mob Weapon Drops", false), //1.4 - Set to false to revert to behavior prior to that version
	MOBPICKUP("Mobs Pick Up Drops", false), //1.4 - Set to false to revert to behavior prior to that version
	OLDPOTIONS("Old Regen and Heal Potions", true), //1.6 - Set to true to revert to behavior prior to that version
	GOLDENAPPLE("Golden Apple Level 0 Uses Nuggets", true), //1.6 - Set to true to revert to behavior prior to that version
	HIDDENLAVA("Disable Nether Hidden Lava Pockets", true), //1.5 - Set to true to revert to behavior prior to that version
	SPIDERPOTIONS("Spider Potion Effects on Spawn", false), //1.6 - Whether to disable spiders from spawning with potions as was added in that version
	//FORCEMOBS("Enforced Mob Controls", true),
	ENDERSOUNDS("New Angry Enderman Sounds", true), //1.4 - Set to false to revert to behavior prior to that version
	ENDERBLOCKS("Allow Enderman Pickup of Cobble, Planks And Stone", false), //beta 1.9 - Set to true to revert to behavior prior to that version
	ENDERDAY("Disable Random Enderman Teleporting in Daylight", true), //1.2? - Set to true to revert to behavior prior to that version
	OLDBOOK("Old Book Recipe", true), //1.4 - Whether to remove the leather from the book recipe that was added in that version
	OLDMELON("Old Glistering Melon Recipe", true), //1.6 - Whether to only need one gold nugget per glistering melon, as was the case before that version
	SILVERFISH("Disable silverfish stone in Extreme Hills", true), //1.4 - Set to true to revert to behavior prior to that version
	OLDFIRE("Original Fire Spread", false), //b1.6 - Set to true to revert to behavior prior to that version. <b>Be warned this fire was infamous for a reason</b>
	CLOSEDPORTALS("Disable Entities Travelling Through Portals", false), //1.3 - Set to true to revert to behavior prior to that version
	PIGPORTALS("Disable Portal Pigmen Spawns", true), //1.4 - Whether to disable the spawning of zombie pigmen around nether portals in the overworld, added in that version
	SILENTVILLAGERS("Disable Villager Noises", false), //1.6 - Set to true to revert to behavior prior to that version
	SUGARCANE("Disable Biome Colors on Sugarcane", true), //1.7 - Set to true to revert to behavior prior to that version
	ROSES("Turn poppies into roses", true), //1.7 - Set to true to revert to behavior prior to that version
	OLDLIGHT("Disable yellowed block light", false), //b1.8 - Set to true to restore the grayscaled underground lighting. This has a <b>profound</b> effect on the aesthetic of the game, and more than any other option (aside from the grass color) can restore the "early MC feel"
	ALPHAGRASS("Alpha Grass and Leaf Color", false), //a1.2 - Set to true to restore bright green alpha-era grass and leaf colors. This has a <b>profound</b> effect on the game aesthetic, and can make the game feel like alpha again when paired with the lighting colors.
	SHEEPUNCH("Old Sheep Wool Harvesting", false), //b1.7 - Set to true to revert to behavior prior to that version
	ANIMALSPAWN("Pre Adventure Update Animal Spawning", false), //b1.8 - Set to true to revert to "animals spawn on lit grass", rather than generating with the chunk
	HELDENCHANT("Allow Mobs to Hold Enchanted Weapons", true), //1.4 - Set to false to revert to behavior prior to that version
	NETHERICE("Enable Ice to Water in Nether", true), //1.5 - Set to true to allow ice to melt into water in the nether, bypassing the bucket limitation
	LAVAHISS("Lava Movement Hiss", true), //1.5 - Set to false to revert to behavior prior to that version
	HOSTILECREATIVE("Mobs Target Creative Players", true), //1.0 - Set to true to revert to behavior prior to that version
	NOHORSES("Disable Horses", false), //1.7 - Set to true to revert to behavior prior to that version
	PORTALSOUNDS("Old Nether Portal Sounds", true), //Broken in 1.3, probably a bug - Whether nether portals should make ambient noises again
	FLINTSOUND("Old Flint And Steel Sound", true), //1.4.2 - Restores the deep "thunk" noise flint and steel used to make
	;

	private String label;
	private boolean defaultState;
	private Class type;

	public static final LegacyOptions[] optionList = LegacyOptions.values();

	private LegacyOptions(String l, boolean d) {
		label = l;
		defaultState = d;
		type = boolean.class;
	}

	public boolean isBoolean() {
		return type == boolean.class;
	}

	public Class getPropertyType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public boolean getState() {
		return (Boolean)LegacyCraft.config.getControl(this.ordinal());
	}

	public boolean isDummiedOut() {
		return type == null;
	}

	@Override
	public boolean getDefaultState() {
		return defaultState;
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
