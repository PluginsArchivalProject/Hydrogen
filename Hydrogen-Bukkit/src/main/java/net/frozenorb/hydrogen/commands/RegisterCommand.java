package net.frozenorb.hydrogen.commands;

import com.google.common.collect.ImmutableMap;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.JSONObject;

public class RegisterCommand {

    @Command(
            names = {"register"},
            permission = "",
            description = "Become a member of the network!"
    )
    public static void register(Player sender, @Param(name = "email") String email) {

        Bukkit.getScheduler().scheduleAsyncDelayedTask(Hydrogen.getInstance(), () -> {
            RequestResponse response = RequestHandler.post("/users/" + sender.getUniqueId() + "/registerEmail",
                    ImmutableMap.of("email", email, "userIp", sender.getAddress().getAddress().getHostAddress()));

            if (!response.wasSuccessful()) {
                sender.sendMessage(ChatColor.RED + response.getErrorMessage());
                return;
            }

            JSONObject json = response.asJSONObject();
            if (json.getBoolean("success"))
                sender.sendMessage(ChatColor.GREEN + "You should be receiving a confirmation e-mail shortly.");
        });
    }

}