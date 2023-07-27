package net.frozenorb.hydrogen.commands.management;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.Settings;
import net.frozenorb.qlib.command.Command;
import org.bukkit.command.*;
import org.bukkit.*;
import net.frozenorb.qlib.command.*;

public class APISetHostNameCommand {

    @Command(names = {"api sethostname"}, permission = "super", description = "Set the API hostname")
    public static void apiSetHostName(CommandSender sender, @Param(name = "hostname") String hostName) {
        Settings settings = Hydrogen.getInstance().getSettings();
        settings.setApiHost(hostName);
        settings.save();

        sender.sendMessage(ChatColor.GREEN + "API hostname set to \"" + ChatColor.WHITE + hostName + ChatColor.GREEN + "\"");
    }

}
