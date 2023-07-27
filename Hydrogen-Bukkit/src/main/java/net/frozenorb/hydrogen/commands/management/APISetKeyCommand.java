package net.frozenorb.hydrogen.commands.management;

import net.frozenorb.qlib.command.Command;
import org.bukkit.command.*;
import org.bukkit.*;
import net.frozenorb.qlib.command.*;

public class APISetKeyCommand {

    @Command(names = {"api setkey"}, permission = "super", description = "Set the API key we use to authenticate")
    public static void apiSetKey(CommandSender sender, @Param(name = "key") String key) {
        sender.sendMessage(ChatColor.RED + "This command is no longer in use. Please configure the server-name in server.properties.");
    }

}
