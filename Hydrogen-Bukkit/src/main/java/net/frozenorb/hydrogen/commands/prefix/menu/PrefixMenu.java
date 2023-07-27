package net.frozenorb.hydrogen.commands.prefix.menu;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.prefix.Prefix;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.Menu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PrefixMenu extends Menu
{
    private static String CREATE_GRANT_PERMISSION = "minehq.prefixgrant.create";
    private String targetName;
    private UUID targetUUID;
    
    public String getTitle(Player player) {
        return ChatColor.YELLOW.toString() + ChatColor.BOLD + "Choose a Prefix";
    }
    
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        List<Prefix> prefixes = this.getAllowedPrefixes(player);
        for (int i = 0; i < prefixes.size(); ++i) {
            buttons.put(i, new PrefixButton(this.targetName, this.targetUUID, prefixes.get(i)));
        }
        return buttons;
    }
    
    private List<Prefix> getAllowedPrefixes(Player player) {
        List<Prefix> allPrefixes = Hydrogen.getInstance().getPrefixHandler().getPrefixes();
        List<Prefix> prefixes = Lists.newArrayList();
        for (int i = 0; i < allPrefixes.size(); ++i) {
            if (this.isAllowed(allPrefixes.get(i), player)) {
                prefixes.add(allPrefixes.get(i));
            }
        }
        return prefixes;
    }
    
    private boolean isAllowed(Prefix prefix, Player player) {
        return player.hasPermission("minehq.prefixgrant.create." + prefix.getId());
    }
    
    public void onClose(Player player) {
        new BukkitRunnable() {
            public void run() {
                if (!currentlyOpenedMenus.containsKey(player.getName())) {
                    player.sendMessage(ChatColor.RED + "Granting cancelled.");
                }
            }
        }.runTaskLater(Hydrogen.getInstance(), 1L);
    }
    
    public PrefixMenu(String targetName, UUID targetUUID) {
        this.targetName = targetName;
        this.targetUUID = targetUUID;
    }
}
