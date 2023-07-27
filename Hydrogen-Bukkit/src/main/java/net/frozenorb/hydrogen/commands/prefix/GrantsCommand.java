package net.frozenorb.hydrogen.commands.prefix;

import com.google.common.collect.ImmutableMap;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.prefix.PrefixGrant;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.commands.punishment.parameter.PunishmentTarget;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.prefix.menu.PrefixGrantMenu;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import net.frozenorb.qlib.qLib;
import net.minecraft.util.com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;

public class GrantsCommand
{
    @Command(names = { "prefixgrants" }, permission = "hydrogen.grantprefix", description = "Check a user's grants")
    public static void grants(Player sender, @Param(name = "target") PunishmentTarget target) {
        target.resolveUUID(uuid -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "An error occurred when contacting the Mojang API.");
                return;
            }
            Bukkit.getScheduler().scheduleAsyncDelayedTask(Hydrogen.getInstance(), () -> {
                RequestResponse response = RequestHandler.get("/prefixes/grants", ImmutableMap.of("user", uuid));
                if (response.wasSuccessful()) {
                    List<PrefixGrant> allGrants = qLib.PLAIN_GSON.fromJson(response.getResponse(), new TypeToken<List<PrefixGrant>>() {}.getType());
                    allGrants.sort((first, second) -> {
                        if (first.getAddedAt() > second.getAddedAt()) {
                            return -1;
                        }
                        else {
                            return 1;
                        }
                    });
                    LinkedHashMap<PrefixGrant, String> grants = new LinkedHashMap<>();
                    allGrants.forEach(grant -> grants.put(grant, grant.resolveAddedBy()));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Hydrogen.getInstance(), () -> new PrefixGrantMenu(grants).openMenu(sender));
                }
                else {
                    sender.sendMessage(ChatColor.RED + response.getErrorMessage());
                }
            });
        });
    }
}
