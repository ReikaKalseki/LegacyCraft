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

import java.util.EnumSet;

import net.minecraft.world.World;
import Reika.DragonAPI.Auxiliary.Trackers.TickRegistry.TickHandler;
import Reika.DragonAPI.Auxiliary.Trackers.TickRegistry.TickType;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class LegacyTickHandler implements TickHandler {

	public static final LegacyTickHandler instance = new LegacyTickHandler();

	private LegacyTickHandler() {

	}

	@Override
	public void tick(TickType type, Object... tickData) {
		World world = (World)tickData[0];


	}

	@Override
	public EnumSet<TickType> getType() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public boolean canFire(Phase p) {
		return p == Phase.START;
	}

	@Override
	public String getLabel() {
		return "LegacyCraft";
	}

}
