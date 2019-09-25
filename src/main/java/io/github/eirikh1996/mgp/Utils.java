package io.github.eirikh1996.mgp;

import me.ryanhamshire.GriefPrevention.Claim;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;

public class Utils {
    public static Claim getClaimCraftIsIn(Craft craft){
        Claim output = null;
        for (MovecraftLocation ml : craft.getHitBox()){
            output = MGP.getInstance().getGriefPreventionPlugin().dataStore.getClaimAt(ml.toBukkit(craft.getW()), false, null);
            if (output != null){
                break;
            }
        }
        return output;
    }
}
