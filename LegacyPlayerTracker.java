package Reika.LegacyCraft;

import net.minecraft.entity.player.EntityPlayer;
import Reika.DragonAPI.Auxiliary.Trackers.PlayerHandler.PlayerTracker;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;


public class LegacyPlayerTracker implements PlayerTracker {

	@Override
	public void onPlayerLogin(EntityPlayer ep) {

	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {

	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player, int dimFrom, int dimTo) {
		if ((dimFrom == -1 || dimTo == -1) && dimFrom+dimTo == -1) { //one being Nether and one overworld
			ReikaSoundHelper.playSoundFromServer(player.worldObj, player.posX, player.posY, player.posZ, "portal.travel", 0.55F, 1, true);
		}
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {

	}

}
