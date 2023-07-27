package net.frozenorb.hydrogen.commands.grant;

import com.google.common.collect.ImmutableList;
import net.frozenorb.hydrogen.commands.punishment.parameter.PunishmentTarget;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import net.frozenorb.qlib.util.TimeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class OGrantCommand
{
    @Command(names = { "ogrant" }, permission = "hydrogen.grant", description = "Add a grant to an user's account", async = true)
    public static void grant(CommandSender sender, @Param(name = "player") PunishmentTarget target, @Param(name = "rank") String rank, @Param(name = "duration") String timeString, @Param(name = "scopes") String scopes, @Param(name = "reason", wildcard = true) String reason) {
        int duration = -1;
        if (!StringUtils.startsWithIgnoreCase(timeString, "perm")) {
            duration = TimeUtils.parseTime(timeString) + 1;
            if (duration <= 0) {
                sender.sendMessage(ChatColor.RED + "'" + timeString + "' is not a valid duration.");
                return;
            }
        }
        if (scopes.equalsIgnoreCase("global")) {
            scopes = "";
        }
        String finalScopes = scopes;
        int expiresIn = duration;
        if (!sender.hasPermission("minehq.grant.create." + rank.toLowerCase())) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
            return;
        }
        target.resolveUUID(uuid -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "An error occurred when contacting the Mojang API.");
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("user", uuid);
            body.put("reason", reason);
            body.put("scopes", finalScopes.isEmpty() ? ImmutableList.of() : finalScopes.split(","));
            body.put("rank", rank);
            if (expiresIn > 0) {
                body.put("expiresIn", expiresIn);
            }
            if (sender instanceof Player) {
                body.put("addedBy", ((Player)sender).getUniqueId().toString());
                body.put("addedByIp", ((Player)sender).getAddress().getAddress().getHostAddress());
            }
            RequestResponse response = RequestHandler.post("/grants", body);
            if (response.wasSuccessful()) {
                sender.sendMessage(ChatColor.GREEN + "Successfully granted " + ChatColor.WHITE + target.getName() + ChatColor.GREEN + " the " + ChatColor.WHITE + rank + ChatColor.GREEN + " rank.");
            }
            else {
                sender.sendMessage(ChatColor.RED + response.getErrorMessage());
            }
        });
    }
}
