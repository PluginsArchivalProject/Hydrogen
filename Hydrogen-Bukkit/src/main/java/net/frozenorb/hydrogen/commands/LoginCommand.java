package net.frozenorb.hydrogen.commands;

import com.google.common.collect.ImmutableMap;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.qlib.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.JSONObject;

public class LoginCommand {
    @Command(
            names = {"login"},
            permission = "",
            description = "Generate a disposable token which can be used to log into the website"
    )
    public static void login(Player sender) {
        Hydrogen plugin = Hydrogen.getInstance();
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, () -> {
            RequestResponse response = RequestHandler.post("/disposableLoginTokens", ImmutableMap.of(
                    "user", sender.getUniqueId(),
                    "userIp", sender.getAddress().getAddress().getHostAddress())
            );

            if (!response.wasSuccessful()) {
                sender.sendMessage(ChatColor.RED + response.getErrorMessage());
                return;
            }

            JSONObject json = response.asJSONObject();
            if (json.getBoolean("success")) {
                sender.sendMessage(ChatColor.GREEN + "www." + plugin.getSettings().getNetworkWebsite() + "/login/" + json.getString("token"));
                sender.sendMessage(ChatColor.YELLOW + "The link above expires in 5 minutes.");
            }
        });
    }
}
