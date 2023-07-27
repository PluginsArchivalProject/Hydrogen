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
import net.frozenorb.qlib.util.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class BlacklistCommand {

    @Command(names = {"blacklist", "bl"}, permission = "minehq.punishment.create.blacklist", description = "Blacklist an user from the network. This type of punishment cannot be appealed", async = true)
    public static void blacklist(CommandSender sender, @Flag(value = {"s", "silent"}, description = "Silently blacklist the player") boolean silent, @Flag(value = {"c", "clear"}, description = "Clear the player's inventory") boolean clear, @Param(name = "player") PunishmentTarget target, @Param(name = "reason", wildcard = true) String reason) {
        blacklist0(sender, target, reason, true, clear);
    }

    private static void blacklist0(CommandSender sender, PunishmentTarget target, String reason, boolean silent, boolean clear) {
        target.resolveUUID(uuid -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "Unable to locate UUID of " + target.getName() + ".");
                return;
            }
            UUID senderUUID = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;
            Player bukkitTarget = Bukkit.getPlayer(uuid);
            Hydrogen plugin = Hydrogen.getInstance();
            RequestResponse response = plugin.getPunishmentHandler().punish(uuid, senderUUID, Punishment.PunishmentType.BLACKLIST, reason, reason, -1L);
            if (response.couldNotConnect()) {
                sender.sendMessage(ChatColor.RED + "Could not reach API to complete punishment request. Adding a local punishment to the cache.");
            } else if (!response.wasSuccessful()) {
                sender.sendMessage(ChatColor.RED + response.getErrorMessage());
                return;
            }
            FancyMessage staffMessage = getStaffMessage(sender, bukkitTarget, target.getName(), reason, silent);
            if (!silent) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("minehq.punishment.create.blacklist")) {
                        staffMessage.send(player);
                    } else {
                        player.sendMessage(ChatColor.GREEN + target.getName() + " was blacklisted by " + sender.getName() + ".");
                    }
                }
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("minehq.punishment.create.blacklist")) {
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
                        bukkitTarget.kickPlayer(Constants.BLACKLIST_MESSAGE.format(settings.getServerName()));
                    }
                }.runTask(plugin);
            }
        });
    }

    private static FancyMessage getStaffMessage(CommandSender sender, Player bukkitTarget, String correctedTarget, String reason, boolean silent) {
        String senderName;
        if (sender instanceof Player) {
            senderName = ((Player) sender).getDisplayName();
        } else {
            senderName = ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Console";
        }
        FancyMessage staffMessage = new FancyMessage((bukkitTarget == null) ? (ChatColor.GREEN + correctedTarget) : bukkitTarget.getDisplayName()).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason).then(" was ").color(ChatColor.GREEN).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason);
        if (silent) {
            staffMessage.then("silently ").color(ChatColor.YELLOW).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason);
        }
        staffMessage.then("blacklisted by ").color(ChatColor.GREEN).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason).then(senderName).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason).then(".").color(ChatColor.GREEN).tooltip(ChatColor.YELLOW + "Reason: " + ChatColor.RED + reason);
        return staffMessage;
    }

}
