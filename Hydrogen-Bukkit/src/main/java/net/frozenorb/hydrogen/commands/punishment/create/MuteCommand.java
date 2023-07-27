package net.frozenorb.hydrogen.commands.punishment.create;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.commands.punishment.parameter.PunishmentTarget;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.punishment.Punishment;
import mkremins.fanciful.FancyMessage;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import net.frozenorb.qlib.util.Callback;
import net.frozenorb.qlib.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MuteCommand
{
    @Command(names = { "mute" }, permission = "minehq.punishment.create.mute", description = "Temporarily mute an user, stopping them from talking in public chat", async = true)
    public static void mute(CommandSender sender, @Param(name = "target") PunishmentTarget target, @Param(name = "time") String timeString, @Param(name = "reason", wildcard = true) String reason) {
        target.resolveUUID((Callback<UUID>)(uuid -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "Unable to locate UUID of " + target.getName() + ".");
                return;
            }
            UUID senderUUID = (sender instanceof Player) ? ((Player)sender).getUniqueId() : null;
            Player bukkitTarget = Bukkit.getPlayer(uuid);
            int seconds = TimeUtils.parseTime(timeString);
            if (senderUUID != null && !((Player)sender).hasPermission("minehq.punishment.create.mute.permanent") && TimeUnit.DAYS.toSeconds(31L) < seconds) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to create a mute this long. Maximum time allowed: 30 days.");
                return;
            }
            RequestResponse response = Hydrogen.getInstance().getPunishmentHandler().punish(uuid, senderUUID, Punishment.PunishmentType.MUTE, reason, reason, seconds);
            if (response.couldNotConnect()) {
                sender.sendMessage(ChatColor.RED + "Could not reach API to complete punishment request. Adding a local punishment to the cache.");
                return;
            }
            if (!response.wasSuccessful()) {
                sender.sendMessage(ChatColor.RED + response.getErrorMessage());
                return;
            }
            FancyMessage staffMessage = getStaffMessage(sender, bukkitTarget, target.getName(), seconds, reason);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("minehq.punishment.create.mute")) {
                    continue;
                }
                staffMessage.send(player);
            }
            staffMessage.send((CommandSender)Bukkit.getConsoleSender());
            if (bukkitTarget != null) {
                bukkitTarget.sendMessage(ChatColor.RED + "You have been silenced for " + TimeUtils.formatIntoDetailedString(seconds) + ".");
            }
        }));
    }
    
    public static FancyMessage getStaffMessage(CommandSender sender, Player bukkitTarget, String correctedTarget, int seconds, String reason) {
        String senderName;
        if (sender instanceof Player) {
            senderName = ((Player)sender).getDisplayName();
        }
        else {
            senderName = ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Console";
        }
        return new FancyMessage((bukkitTarget == null) ? (ChatColor.GREEN + correctedTarget) : bukkitTarget.getDisplayName()).tooltip(new String[] { ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason, ChatColor.YELLOW + "Duration: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds) }).then(" was temporarily muted by ").color(ChatColor.GREEN).tooltip(new String[] { ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason, ChatColor.YELLOW + "Duration: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds) }).then(senderName).tooltip(new String[] { ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason, ChatColor.YELLOW + "Duration: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds) }).then(".").color(ChatColor.GREEN).tooltip(new String[] { ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason, ChatColor.YELLOW + "Duration: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds) });
    }
}
