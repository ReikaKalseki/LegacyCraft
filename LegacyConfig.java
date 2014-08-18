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

import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Instantiable.IO.ControlledConfig;
import Reika.DragonAPI.Interfaces.ConfigList;
import Reika.DragonAPI.Interfaces.IDRegistry;

public class LegacyConfig extends ControlledConfig {

	private boolean[] overrides = new boolean[MobOverrides.mobList.length];

	public LegacyConfig(DragonAPIMod mod, ConfigList[] option, IDRegistry[] id, int cfg) {
		super(mod, option, id, cfg);
	}

	@Override
	protected void loadAdditionalData() {
		for (int i = 0; i < MobOverrides.mobList.length; i++) {
			MobOverrides mob = MobOverrides.mobList[i];
			overrides[i] = config.get("Mob Overrides", mob.name, true).getBoolean(true);
		}
	}

	public boolean overrideMob(MobOverrides mob) {
		return overrides[mob.ordinal()];
	}

}
