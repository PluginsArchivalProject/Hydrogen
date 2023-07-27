package net.frozenorb.hydrogen.nametag;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.profile.Profile;
import net.frozenorb.hydrogen.rank.Rank;
import net.frozenorb.qlib.nametag.NametagInfo;
import net.frozenorb.qlib.nametag.NametagProvider;
import org.bukkit.entity.Player;

import java.util.Optional;

public class HNametagProvider extends NametagProvider
{
    public HNametagProvider() {
        super("Hydrogen Provider", 1);
    }
    
    public NametagInfo fetchNametag(Player player, Player watcher) {
        Optional<Profile> profileOptional = Hydrogen.getInstance().getProfileHandler().getProfile(player.getUniqueId());
        if (!profileOptional.isPresent())
            return createNametag("", "");

        Profile profile = profileOptional.get();
        Rank bestDisplayRank = profile.getBestDisplayRank();

        if (bestDisplayRank == null)
            return createNametag("", "");

        return createNametag(bestDisplayRank.getGameColor(), "");
    }
}
