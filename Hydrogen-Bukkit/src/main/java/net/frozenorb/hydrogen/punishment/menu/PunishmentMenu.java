package net.frozenorb.hydrogen.punishment.menu;

import net.frozenorb.hydrogen.punishment.Punishment;
import lombok.RequiredArgsConstructor;
import net.frozenorb.qlib.menu.pagination.*;
import org.bukkit.entity.*;
import net.frozenorb.qlib.menu.*;
import com.google.common.collect.*;
import org.bukkit.*;
import org.bukkit.event.inventory.*;

import java.util.*;

@RequiredArgsConstructor
public class PunishmentMenu extends PaginatedMenu {

    private final String targetUUID;
    private final String targetName;
    private final Punishment.PunishmentType type;
    private final Map<Punishment, String> punishments;

    public String getPrePaginatedTitle(Player player) {
        return ChatColor.RED + this.type.getName() + "s";
    }

    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();

        for (Map.Entry<Punishment, String> entry : this.punishments.entrySet())
            buttons.put(buttons.size(), new PunishmentButton(entry.getKey(), entry.getValue(), player.hasPermission("minehq.punishments.view.reason")));

        return buttons;
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
                new MainPunishmentMenu(targetUUID, targetName).openMenu(player);
            }
        });

        return buttons;
    }

}