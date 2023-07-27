package net.frozenorb.hydrogen.grant.menu;

import net.frozenorb.hydrogen.grant.Grant;
import net.frozenorb.qlib.menu.pagination.*;
import org.bukkit.entity.*;
import net.frozenorb.qlib.menu.*;
import com.google.common.collect.*;
import org.bukkit.*;
import org.bukkit.event.inventory.*;
import java.util.*;

public class GrantMenu extends PaginatedMenu
{
    private final Map<Grant, String> grants;

    public String getPrePaginatedTitle(final Player player) {
        return ChatColor.RED + "Grants";
    }

    public Map<Integer, Button> getGlobalButtons(final Player player) {
        final Map<Integer, Button> buttons = Maps.newHashMap();
        buttons.put(4, new Button() {
            public String getName(final Player player) {
                return ChatColor.YELLOW + "Back";
            }

            public List<String> getDescription(final Player player) {
                return null;
            }

            public Material getMaterial(final Player player) {
                return Material.PAPER;
            }

            public byte getDamageValue(final Player player) {
                return 0;
            }

            public void clicked(final Player player, final int i, final ClickType clickType) {
                player.closeInventory();
            }
        });
        return buttons;
    }

    public Map<Integer, Button> getAllPagesButtons(final Player player) {
        final Map<Integer, Button> buttons = Maps.newHashMap();
        int index = 0;
        for (final Map.Entry<Grant, String> entry : this.grants.entrySet()) {
            buttons.put(index, new GrantButton(entry.getKey(), entry.getValue()));
            ++index;
        }
        return buttons;
    }

    public GrantMenu(final Map<Grant, String> grants) {
        this.grants = grants;
    }
}