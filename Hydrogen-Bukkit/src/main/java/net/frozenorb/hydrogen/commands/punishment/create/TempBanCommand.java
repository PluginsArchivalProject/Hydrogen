package net.frozenorb.hydrogen.commands.punishment.create;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.Settings;
import net.frozenorb.hydrogen.commands.punishment.parameter.PunishmentTarget;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.punishment.Punishment;
import mkremins.fanciful.FancyMessage;
import net.frozenorb.hydrogen.util.Constants;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Flag;
import net.frozenorb.qlib.command.Param;
import net.frozenorb.qlib.command.parameter.offlineplayer.OfflinePlayerWrapper;
import net.frozenorb.qlib.util.TimeUtils;
import net.frozenorb.qlib.util.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TempBanCommand
{
    @Command(names = { "tempban", "tban", "tb" }, permission = "minehq.punishment.create.ban", description = "Temporarily banish an user from the network", async = true)
    public static void tempban(CommandSender sender, @Flag(value = { "s", "silent" }, description = "Silently ban the player") boolean silent, @Flag(value = { "c", "clear" }, description = "Clear the player's inventory") boolean clear, @Param(name = "target") PunishmentTarget target, @Param(name = "time") String timeString, @Param(name = "reason", wildcard = true) String reason) {
        tempban0(sender, target, timeString, reason, true, clear);
    }
    
    private static void tempban0(CommandSender sender, PunishmentTarget target, String timeString, String reason, boolean silent, boolean clear) {
        target.resolveUUID(uuid -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "Unable to locate UUID of " + target.getName() + ".");
                return;
            }
            UUID senderUUID = (sender instanceof Player) ? ((Player)sender).getUniqueId() : null;
            Player bukkitTarget = Bukkit.getPlayer(uuid);
            int seconds = TimeUtils.parseTime(timeString);
            if (senderUUID != null && !sender.hasPermission("minehq.punishment.create.ban.permanent") && TimeUnit.DAYS.toSeconds(91L) < seconds) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to create a ban this long. Maximum time allowed: 90 days.");
                return;
            }
            Hydrogen plugin = Hydrogen.getInstance();
            RequestResponse response = plugin.getPunishmentHandler().punish(uuid, senderUUID, Punishment.PunishmentType.BAN, reason, reason, seconds);
            if (response.couldNotConnect()) {
                sender.sendMessage(ChatColor.RED + "Could not reach API to complete punishment request. Adding a local punishment to the cache.");
            }
            else if (!response.wasSuccessful()) {
                sender.sendMessage(ChatColor.RED + response.getErrorMessage());
                return;
            }
            FancyMessage staffMessage = getStaffMessage(sender, bukkitTarget, target.getName(), reason, seconds, silent);
            if (!silent) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("minehq.punishment.create.ban")) {
                        staffMessage.send(player);
                    }
                    else {
                        player.sendMessage(ChatColor.GREEN + target.getName() + " was temporarily banned by " + sender.getName() + ".");
                    }
                }
            }
            else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("minehq.punishment.create.ban")) {
                        continue;
                    }
                    staffMessage.send(player);
                }
            }
            staffMessage.send(Bukkit.getConsoleSender());
            if (clear) {
                OfflinePlayerWrapper wrapper = new OfflinePlayerWrapper(UUIDUtils.name(uuid));
                wrapper.loadAsync(player -> Bukkit.getScheduler().runTask(plugin, () -> {
                    player.getInventory().clear();
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, player::saveData);
                }));
            }
            if (bukkitTarget != null) {
                new BukkitRunnable() {
                    public void run() {
                        Settings settings = plugin.getSettings();
                        bukkitTarget.kickPlayer(Constants.BAN_MESSAGE.format(settings.getServerName(), settings.getNetworkWebsite()));
                    }
                }.runTask(plugin);
            }
        });
    }
    
    public static FancyMessage getStaffMessage(CommandSender sender, Player bukkitTarget, String correctedTarget, String reason, int seconds, boolean silent) {
        String senderName;
        if (sender instanceof Player) {
            senderName = ((Player)sender).getDisplayName();
        }
        else {
            senderName = ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Console";
        }
        FancyMessage staffMessage = new FancyMessage((bukkitTarget == null) ? (ChatColor.GREEN + correctedTarget) : bukkitTarget.getDisplayName()).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason, ChatColor.YELLOW + "Duration: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds)).then(" was ").color(ChatColor.GREEN).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason, ChatColor.YELLOW + "Duration: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds));
        if (silent) {
            staffMessage.then("silently ").color(ChatColor.YELLOW).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason, ChatColor.YELLOW + "Duration: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds));
        }
        staffMessage.then("temporarily banned by ").color(ChatColor.GREEN).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason, ChatColor.YELLOW + "Duration: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds)).then(senderName).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason, ChatColor.YELLOW + "Duration: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds)).then(".").color(ChatColor.GREEN).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason, ChatColor.YELLOW + "Duration: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds));
        return staffMessage;
    }
}
