package net.frozenorb.hydrogen.commands.prefix.setmenu;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.prefix.Prefix;
import net.frozenorb.hydrogen.profile.Profile;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.Menu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PrefixMenu extends Menu
{
    private String targetName;
    private UUID targetUUID;
    
    public String getTitle(Player player) {
        return ChatColor.YELLOW.toString() + ChatColor.BOLD + "Choose a Prefix";
    }
    
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        List<Prefix> prefixes = Hydrogen.getInstance().getPrefixHandler().getPrefixes();
        Profile playerProfile = Hydrogen.getInstance().getProfileHandler().getProfile(player.getUniqueId()).get();
        Set<Prefix> authorizedPrefixes = playerProfile.getAuthorizedPrefixes();
        for (int i = 0; i < prefixes.size(); ++i) {
            buttons.put(i, new PrefixButton(this.targetName, this.targetUUID, prefixes.get(i), Objects.equal(prefixes.get(i), playerProfile.getActivePrefix()), authorizedPrefixes.contains(prefixes.get(i))));
        }
        return buttons;
    }
    
    public PrefixMenu(String targetName, UUID targetUUID) {
        this.targetName = targetName;
        this.targetUUID = targetUUID;
    }
}
