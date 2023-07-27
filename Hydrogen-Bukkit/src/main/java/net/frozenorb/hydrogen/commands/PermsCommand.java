package net.frozenorb.hydrogen.commands;

import com.google.common.collect.Lists;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.profile.Profile;
import net.frozenorb.hydrogen.rank.Rank;
import net.frozenorb.hydrogen.util.pagination.PaginatedResult;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class PermsCommand {

    @Command(names = {"perms"}, permission = "hydrogen.viewperms", description = "View an user's permissions")
    public static void perms(CommandSender sender, @Param(name = "player", defaultValue = "self") Player player, @Param(name = "page", defaultValue = "1") int page) {
        Optional<Profile> optionalProfile = Hydrogen.getInstance().getProfileHandler().getProfile(player.getUniqueId());
        if (!optionalProfile.isPresent()) {
            sender.sendMessage(ChatColor.RED + player.getName() + "'s profile isn't loaded.");
            return;
        }

        Rank bestDisplayRank = optionalProfile.get().getBestDisplayRank();
        Map<String, Boolean> sortedPermissions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sortedPermissions.putAll(optionalProfile.get().getPermissions());

        List<String> toSend = Lists.newArrayList();
        for (String permission : sortedPermissions.keySet())
            toSend.add((player.hasPermission(permission) ? (ChatColor.GREEN + " + ") : (ChatColor.RED + " - "))
                    + ChatColor.WHITE + permission + " (" + sortedPermissions.get(permission) + ")");

        new PaginatedResult<String>() {
            @Override
            public String getHeader(int page, int maxPages) {
                return ChatColor.translateAlternateColorCodes('&', "&c" + StringUtils.repeat('-', 3) + " &r" + player.getDisplayName() + "&7(&r" + bestDisplayRank.getFormattedName() + "&7)'s Permissions (&e" + page + "&7/&e" + maxPages + "&7) &c" + StringUtils.repeat('-', 3));
            }

            @Override
            public String format(String entry, int index) {
                return entry;
            }
        }.display(sender, toSend, page);
    }

    @Command(names = {"rankPerms"}, permission = "hydrogen.viewperms", description = "View an user's permissions")
    public static void rankPerms(CommandSender sender, @Param(name = "page", defaultValue = "default") String rank) {
        Hydrogen.getInstance().getPermissionHandler().getPermissions(Hydrogen.getInstance().getRankHandler().getRank(rank).get())
                .forEach((perm, value) -> sender.sendMessage(perm + ":" + value));
    }

}