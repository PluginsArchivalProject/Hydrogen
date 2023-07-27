package net.frozenorb.hydrogen.prefix.menu;

import net.frozenorb.hydrogen.prefix.PrefixGrant;
import lombok.RequiredArgsConstructor;
import net.frozenorb.qlib.menu.pagination.*;
import org.bukkit.entity.*;
import net.frozenorb.qlib.menu.*;
import com.google.common.collect.*;
import org.bukkit.*;
import org.bukkit.event.inventory.*;

import java.util.*;

@RequiredArgsConstructor
public class PrefixGrantMenu extends PaginatedMenu {
    
    private final Map<PrefixGrant, String> prefixGrants;

    public String getPrePaginatedTitle(Player player) {
        return ChatColor.RED + "Prefix Grants";
    }

    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();

        buttons.put(4, new Button() {
            public String getName(Player player) {
                return ChatColor.YELLOW + "Back";
            }

            public List<String> getDescription(Player player) {
                return null;
            }

            public Material getMaterial(Player player) {
                return Material.PAPER;
            }

            public byte getDamageValue(Player player) {
                return 0;
            }

            public void clicked(Player player, int i, ClickType clickType) {
                player.closeInventory();
            }
        });

        return buttons;
    }

    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();

        for (Map.Entry<PrefixGrant, String> entry : this.prefixGrants.entrySet())
            buttons.put(buttons.size(), new PrefixGrantButton(entry.getKey(), entry.getValue()));

        return buttons;
    }

}