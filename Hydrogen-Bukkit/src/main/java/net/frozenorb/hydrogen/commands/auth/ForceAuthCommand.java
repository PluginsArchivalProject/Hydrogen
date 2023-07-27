package net.frozenorb.hydrogen.commands.auth;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.qlib.command.Command;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.*;
import org.bukkit.*;
import net.frozenorb.qlib.command.*;

public class ForceAuthCommand {

    @Command(names = {"forceauth"}, permission = "op", description = "Forcefully authenticate a player")
    public static void forceAuth(CommandSender sender, @Param(name = "player") Player player) {
        player.removeMetadata("Locked", Hydrogen.getInstance());
        player.setMetadata("ForceAuth", new FixedMetadataValue(Hydrogen.getInstance(), true));
        sender.sendMessage(ChatColor.YELLOW + player.getName() + " has been forcefully authenticated.");
    }

}
