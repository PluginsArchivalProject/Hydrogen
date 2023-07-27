package net.frozenorb.hydrogen.commands.prefix;

import com.google.common.collect.ImmutableList;
import net.frozenorb.hydrogen.commands.punishment.parameter.PunishmentTarget;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import net.frozenorb.qlib.util.Callback;
import net.frozenorb.qlib.util.TimeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OGrantCommand
{
    @Command(names = { "oprefixgrant", "ograntprefix" }, permission = "hydrogen.grantprefix", description = "Add a grant to an user's account", async = true)
    public static void grant(CommandSender sender, @Param(name = "player") PunishmentTarget target, @Param(name = "prefix") String prefix, @Param(name = "duration") String timeString, @Param(name = "scopes") String scopes, @Param(name = "reason", wildcard = true) String reason) {
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
        if (!sender.hasPermission("minehq.prefixgrant.create." + prefix.toLowerCase())) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
            return;
        }
        target.resolveUUID((Callback<UUID>)(uuid -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "An error occurred when contacting the Mojang API.");
                return;
            }
            Map<String, Object> body = new HashMap<String, Object>();
            body.put("user", uuid);
            body.put("reason", reason);
            body.put("scopes", finalScopes.isEmpty() ? ImmutableList.of() : finalScopes.split(","));
            body.put("prefix", prefix);
            if (expiresIn > 0) {
                body.put("expiresIn", expiresIn);
            }
            if (sender instanceof Player) {
                body.put("addedBy", ((Player)sender).getUniqueId().toString());
                body.put("addedByIp", ((Player)sender).getAddress().getAddress().getHostAddress());
            }
            RequestResponse response = RequestHandler.post("/prefixes", body);
            if (response.wasSuccessful()) {
                sender.sendMessage(ChatColor.GREEN + "Successfully granted " + ChatColor.WHITE + target.getName() + ChatColor.GREEN + " the " + ChatColor.WHITE + prefix + ChatColor.GREEN + " prefix.");
            }
            else {
                sender.sendMessage(ChatColor.RED + response.getErrorMessage());
            }
        }));
    }
}
