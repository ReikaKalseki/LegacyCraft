/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft;

import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Instantiable.IO.ControlledConfig;
import Reika.DragonAPI.Interfaces.Configuration.ConfigList;
import Reika.DragonAPI.Interfaces.Registry.IDRegistry;

public class LegacyConfig extends ControlledConfig {

	private DataElement<Boolean>[] overrides = new DataElement[MobOverrides.mobList.length];

	public LegacyConfig(DragonAPIMod mod, ConfigList[] option, IDRegistry[] id) {
		super(mod, option, id);

		for (int i = 0; i < MobOverrides.mobList.length; i++) {
			MobOverrides mob = MobOverrides.mobList[i];
			overrides[i] = this.registerAdditionalOption("Mob Overrides", mob.name, true);
		}
	}

	public boolean overrideMob(MobOverrides mob) {
		return overrides[mob.ordinal()].getData();
	}

}
