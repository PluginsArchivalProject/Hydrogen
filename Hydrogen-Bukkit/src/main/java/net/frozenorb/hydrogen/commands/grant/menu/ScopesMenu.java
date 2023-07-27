package net.frozenorb.hydrogen.commands.grant.menu;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.rank.Rank;
import net.frozenorb.hydrogen.server.ServerGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class ScopesMenu extends Menu {

    private final Map<String, Boolean> status = new HashMap<>();
    private boolean global;
    private boolean complete;
    private Rank rank;
    private String targetName;
    private UUID targetUUID;
    private String reason;
    private int duration;

    public String getTitle(Player player) {
        return ChatColor.YELLOW.toString() + ChatColor.BOLD + "Select the Scopes";
    }

    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        List<ServerGroup> groups = Lists.newArrayList();
        groups.addAll(Hydrogen.getInstance().getServerHandler().getServerGroups());
        groups.sort((first, second) -> first.getId().compareToIgnoreCase(second.getId()));

        for (ServerGroup scope : groups) {
            this.status.putIfAbsent(scope.getId(), false);
            buttons.put(buttons.size(), new ScopeButton(this, scope));
        }

        List<ServerGroup> scopes = Lists.newArrayList();
        scopes.addAll(this.status.keySet().stream().filter(this.status::get)
                .map(key -> Hydrogen.getInstance().getServerHandler().getServerGroup(key).orElse(null))
                .collect(Collectors.toList()));

        buttons.put(22, new GlobalButton(this));
        buttons.put(31, new GrantButton(this.rank, this.targetName, this.targetUUID, this.reason, this, scopes, this.duration));
        return buttons;
    }

    public void onClose(Player player) {
        Bukkit.getScheduler().runTaskLater(Hydrogen.getInstance(), () -> {
            if (!currentlyOpenedMenus.containsKey(player.getName()) && !this.complete)
                player.sendMessage(ChatColor.RED + "Granting cancelled.");
        }, 1L);
    }

}
