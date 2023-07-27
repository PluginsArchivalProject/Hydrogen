package net.frozenorb.hydrogen.commands;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.commands.punishment.parameter.PunishmentTarget;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.listener.GeneralListener;
import net.frozenorb.hydrogen.server.Server;
import mkremins.fanciful.FancyMessage;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import net.frozenorb.qlib.util.TimeUtils;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

public class SeenCommand
{
    @Command(names = { "seen" }, permission = "hydrogen.seen", description = "See an user's IP and other info", async = true)
    public static void seen(CommandSender sender, @Param(name = "player") PunishmentTarget target) {
        target.resolveUUID(uuid -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "An error occurred when contacting the Mojang API.");
                return;
            }

            Server server = Hydrogen.getInstance().getServerHandler().find(uuid).isPresent() ? Hydrogen.getInstance().getServerHandler().find(uuid).get() : null;
            RequestResponse response = RequestHandler.get("/users/" + uuid.toString() + "/details");

            if (!response.wasSuccessful()) {
                sender.sendMessage(ChatColor.RED + response.getErrorMessage());
                return;
            }

            JSONObject details = response.asJSONObject();
            JSONObject lastIpLog = null;

            if (details.has("ipLog")) {
                JSONArray array = details.getJSONArray("ipLog");
                for (Object logObject : array) {
                    JSONObject log = (JSONObject)logObject;
                    if (lastIpLog == null || log.getLong("lastSeenAt") > lastIpLog.getLong("lastSeenAt"))
                        lastIpLog = log;
                }
            }

            sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 53));
            sender.sendMessage(ChatColor.RED + target.getName() + ChatColor.YELLOW + ":");

            if (server != null) {
                long joinTime = GeneralListener.joinTime.get(uuid);
                int playSeconds = (int)((System.currentTimeMillis() - joinTime) / 1000L);
                sender.sendMessage(ChatColor.YELLOW + "Currently on " + ChatColor.RED + server.getDisplayName() + ChatColor.YELLOW
                        + ". Online for " + ChatColor.RED + TimeUtils.formatIntoDetailedString(playSeconds) + ChatColor.YELLOW + ".");
            } else {
                Date lastSeen = new Date(details.getJSONObject("user").getLong("lastSeenAt"));
                sender.sendMessage(ChatColor.YELLOW + "Currently " + ChatColor.RED + "offline" + ChatColor.YELLOW
                        + ". Last seen at " + ChatColor.RED + TimeUtils.formatIntoCalendarString(lastSeen) + ChatColor.YELLOW + ".");
            }

            if (sender.hasPermission("hydrogen.seen.ip") && lastIpLog != null) {
                FancyMessage ipMessage = new FancyMessage("(Hover to show last known IP)").color(ChatColor.YELLOW);
                ipMessage.tooltip(ChatColor.RED + "sike bitch twitter.com/bizarreaiex nigga stay woketh :pray:");
                ipMessage.send(sender);
            }

            sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 53));
        });
    }
}
