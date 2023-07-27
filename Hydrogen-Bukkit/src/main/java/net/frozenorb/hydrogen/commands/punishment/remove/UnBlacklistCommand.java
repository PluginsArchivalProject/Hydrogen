package net.frozenorb.hydrogen.commands.punishment.remove;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.commands.punishment.parameter.PunishmentTarget;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.punishment.Punishment;
import mkremins.fanciful.FancyMessage;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import net.frozenorb.qlib.util.Callback;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UnBlacklistCommand
{
    @Command(names = { "unblacklist" }, permission = "minehq.punishment.remove.blacklist", description = "Lift an user's blacklist", async = true)
    public static void unblacklist(CommandSender sender, @Param(name = "player") PunishmentTarget target, @Param(name = "reason", wildcard = true) String reason) {
        target.resolveUUID((Callback<UUID>)(uuid -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "An error occurred when contacting the Mojang API.");
                return;
            }
            UUID senderUUID = (sender instanceof Player) ? ((Player)sender).getUniqueId() : null;
            Player bukkitTarget = Bukkit.getPlayer(uuid);
            RequestResponse response = Hydrogen.getInstance().getPunishmentHandler().pardon(uuid, senderUUID, Punishment.PunishmentType.BLACKLIST, reason);
            if (response.couldNotConnect()) {
                sender.sendMessage(ChatColor.RED + "Could not reach API to complete pardon request. Adding request to queue.");
                return;
            }
            if (!response.wasSuccessful()) {
                sender.sendMessage(ChatColor.RED + response.getErrorMessage());
                return;
            }
            FancyMessage staffMessage = getStaffMessage(sender, bukkitTarget, target.getName(), reason);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("minehq.punishment.create.blacklist")) {
                    continue;
                }
                staffMessage.send(player);
            }
            staffMessage.send((CommandSender)Bukkit.getConsoleSender());
        }));
    }
    
    private static FancyMessage getStaffMessage(CommandSender sender, Player bukkitTarget, String correctedTarget, String reason) {
        String senderName;
        if (sender instanceof Player) {
            senderName = ((Player)sender).getDisplayName();
        }
        else {
            senderName = ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Console";
        }
        FancyMessage staffMessage = new FancyMessage((bukkitTarget == null) ? (ChatColor.GREEN + correctedTarget) : bukkitTarget.getDisplayName()).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason).then(" was ").color(ChatColor.GREEN).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason);
        staffMessage.then("un-blacklisted by ").color(ChatColor.GREEN).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason).then(senderName).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason).then(".").color(ChatColor.GREEN).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason);
        return staffMessage;
    }
}
