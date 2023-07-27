package net.frozenorb.hydrogen.commands.grant.menu;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.rank.Rank;
import lombok.AllArgsConstructor;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
public class RanksMenu extends Menu {

    private String targetName;
    private UUID targetUUID;

    public String getTitle(Player player) {
        return ChatColor.YELLOW.toString() + ChatColor.BOLD + "Choose a Rank";
    }

    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        AtomicInteger atomicInteger = new AtomicInteger(0);

        for (Rank rank : Hydrogen.getInstance().getRankHandler().getRanks())
            buttons.put(atomicInteger.getAndIncrement(), new RankButton(this.targetName, this.targetUUID, rank));

        return buttons;
    }

    private List<Rank> getAllowedRanks(Player player) {
        List<Rank> allRanks = Hydrogen.getInstance().getRankHandler().getRanks();
        List<Rank> ranks = Lists.newArrayList();
        for (int i = 0; i < allRanks.size(); ++i) {
            if (i == 0)
                continue;

            if (this.isAllowed(allRanks.get(i), player)) {
                ranks.add(allRanks.get(i));
            }
        }

        ranks.sort(Rank.GENERAL_WEIGHT_COMPARATOR);
        return ranks;
    }

    private boolean isAllowed(Rank rank, Player player) {
        return player.hasPermission("minehq.grant.create." + rank.getId());
    }

    public void onClose(Player player) {
        Bukkit.getScheduler().runTaskLater(Hydrogen.getInstance(), () -> {
            if (!currentlyOpenedMenus.containsKey(player.getName()))
                player.sendMessage(ChatColor.RED + "Granting cancelled.");
        }, 1L);
    }

}
