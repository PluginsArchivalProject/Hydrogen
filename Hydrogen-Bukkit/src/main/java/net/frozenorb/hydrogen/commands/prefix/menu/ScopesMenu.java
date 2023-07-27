package net.frozenorb.hydrogen.commands.prefix.menu;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.prefix.Prefix;
import net.frozenorb.hydrogen.server.Server;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.Menu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ScopesMenu extends Menu
{
    private Map<String, Boolean> status;
    private boolean global;
    private boolean complete;
    private Prefix prefix;
    private String targetName;
    private UUID targetUUID;
    private String reason;
    private int duration;
    
    public String getTitle(Player player) {
        return ChatColor.YELLOW.toString() + ChatColor.BOLD + "Select the Scopes";
    }
    
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        List<Server> groups = Lists.newArrayList();
        groups.addAll(Hydrogen.getInstance().getServerHandler().getServers());
        groups.sort((first, second) -> first.getId().compareToIgnoreCase(second.getId()));
        int i = 0;
        for (Server scope : groups) {
            if (!scope.getServerGroup().toLowerCase().contains("bunker") && !scope.getServerGroup().toLowerCase().contains("skywar")) {
                if (scope.getServerGroup().toLowerCase().contains("hub")) {
                    continue;
                }
                this.status.putIfAbsent(scope.getId(), false);
                buttons.put(i, new ScopeButton(this, scope));
                ++i;
            }
        }
        List<Server> scopes = Lists.newArrayList();
        scopes.addAll(this.status.keySet().stream().filter(this.status::get).map(key -> Hydrogen.getInstance().getServerHandler().getServer(key).orElse(null)).collect(Collectors.toList()));
        buttons.put(22, new GlobalButton(this));
        buttons.put(31, new GrantButton(this.prefix, this.targetName, this.targetUUID, this.reason, this, scopes, this.duration));
        return buttons;
    }
    
    public void onClose(Player player) {
        new BukkitRunnable() {
            public void run() {
                if (!currentlyOpenedMenus.containsKey(player.getName()) && !ScopesMenu.this.complete) {
                    player.sendMessage(ChatColor.RED + "Granting cancelled.");
                }
            }
        }.runTaskLater(Hydrogen.getInstance(), 1L);
    }
    
    public ScopesMenu(boolean global, boolean complete, Prefix prefix, String targetName, UUID targetUUID, String reason, int duration) {
        this.status = Maps.newHashMap();
        this.global = false;
        this.global = global;
        this.complete = complete;
        this.prefix = prefix;
        this.targetName = targetName;
        this.targetUUID = targetUUID;
        this.reason = reason;
        this.duration = duration;
    }
    
    public Map<String, Boolean> getStatus() {
        return this.status;
    }
    
    public boolean isGlobal() {
        return this.global;
    }
    
    public void setGlobal(boolean global) {
        this.global = global;
    }
    
    public boolean isComplete() {
        return this.complete;
    }
    
    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
