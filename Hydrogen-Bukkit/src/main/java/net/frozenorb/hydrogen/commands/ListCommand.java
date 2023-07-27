package net.frozenorb.hydrogen.commands;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.profile.Profile;
import net.frozenorb.hydrogen.rank.Rank;
import net.frozenorb.qlib.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ListCommand
{
    @Command(names = { "list", "who", "players" }, permission = "hydrogen.list", description = "See a list of online players")
    public static void list(CommandSender sender) {
        Map<Rank, List<String>> sorted = new TreeMap<>(Rank.DISPLAY_WEIGHT_COMPARATOR);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (sender instanceof Player && !((Player)sender).canSee(player) && player.hasMetadata("invisible"))
                continue;

            Optional<Profile> profileOptional = Hydrogen.getInstance().getProfileHandler().getProfile(player.getUniqueId());
            Rank rank = null;

            if (profileOptional.isPresent()) {
                Profile profile = profileOptional.get();
                rank = profile.getBestDisplayRank();
            }

            Rank defaultRank = Hydrogen.getInstance().getRankHandler().getRank("default").orElse(null);
            if (rank == null || (defaultRank != null && player.isDisguised()))
                rank = defaultRank;

            String displayName = player.getDisplayName();
            if (player.hasMetadata("invisible"))
                displayName = ChatColor.GRAY + "*" + displayName;

            sorted.putIfAbsent(rank, new LinkedList<>());
            sorted.get(rank).add(displayName);
        }

        List<String> merged = new LinkedList<>();
        for (List<String> part : sorted.values()) {
            part.sort(String.CASE_INSENSITIVE_ORDER);
            merged.addAll(part);
        }

        sender.sendMessage(getHeader());
        sender.sendMessage("(" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers() + ") " + merged);
    }
    
    private static String getHeader() {
        StringBuilder builder = new StringBuilder();

        for (Rank rank : Hydrogen.getInstance().getRankHandler().getRanks()) {
            boolean displayed = rank.getDisplayWeight() > 0;
            if (displayed)
                builder.append(rank.getFormattedName()).append(ChatColor.RESET).append(", ");
        }

        if (builder.length() > 2)
            builder.setLength(builder.length() - 2);

        return builder.toString();
    }
}
