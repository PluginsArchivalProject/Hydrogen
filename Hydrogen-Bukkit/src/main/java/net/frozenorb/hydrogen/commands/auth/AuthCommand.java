package net.frozenorb.hydrogen.commands.auth;

import com.google.common.collect.ImmutableMap;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.profile.Profile;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.util.Optional;

public class AuthCommand {
    @Command(names = {"auth", "authenticate", "2fa", "otp"}, permission = "", description = "Authenticate with the API, verifying your identity")
    public static void auth(Player sender, @Param(name = "code", wildcard = true) String input) {
        if (!sender.hasMetadata("Locked")) {
            sender.sendMessage(ChatColor.RED + "You don't need to authenticate at the moment.");
            return;
        }

        input = input.replace(" ", "");
        int code = Integer.parseInt(input);

        Bukkit.getScheduler().scheduleAsyncDelayedTask(Hydrogen.getInstance(), () -> {
            RequestResponse response = RequestHandler.post("/users/" + sender.getUniqueId().toString() + "/verifyTotp",
                    ImmutableMap.of("totpCode", code, "userIp", sender.getAddress().getAddress().getHostAddress()));

            if (!response.wasSuccessful()) {
                sender.sendMessage(ChatColor.RED + response.getErrorMessage());
                Bukkit.getLogger().warning("TOTP - Failed to authenticate " + sender.getUniqueId() + ": " + response.getErrorMessage());
                return;
            }

            Bukkit.getScheduler().runTask(Hydrogen.getInstance(), () -> {
                JSONObject object = response.asJSONObject();
                boolean authorized = object.getBoolean("authorized");

                if (!authorized) {
                    sender.sendMessage(ChatColor.RED + object.getString("message"));
                    return;
                }

                Optional<Profile> profileOptional = Hydrogen.getInstance().getProfileHandler().getProfile(sender.getUniqueId());
                if (!profileOptional.isPresent()) {
                    sender.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Your profile hasn't yet been loaded.");
                } else {
                    Profile profile = profileOptional.get();
                    profile.authenticated();
                    profile.updatePlayer(sender);
                    sender.sendMessage(ChatColor.GREEN + "Your identity has been verified.");
                }
            });
        });
    }
}
