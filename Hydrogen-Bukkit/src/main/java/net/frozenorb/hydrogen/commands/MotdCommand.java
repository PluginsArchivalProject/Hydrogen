package net.frozenorb.hydrogen.commands;

import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import net.frozenorb.qlib.qLib;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MotdCommand {

    private static String line1;
    private static String line2;

    @Command(names = {"motd push"}, permission = "super", async = true)
    public static void motdPush(CommandSender sender) {
        if (line1 == null) {
            sender.sendMessage(ChatColor.RED + "Line 1 is not set! Unable to push.");
            return;
        }
        if (line2 == null) {
            sender.sendMessage(ChatColor.RED + "Line 2 is not set! Unable to push.");
            return;
        }

        qLib.getInstance().runBackboneRedisCommand(jedis -> {
            jedis.set("BungeeCordMOTD", line1 + "\n" + line2);
            sender.sendMessage(ChatColor.GREEN + "MOTD set.");
            return null;
        });
    }

    @Command(names = {"motd set"}, permission = "super")
    public static void motdSet(CommandSender sender, @Param(name = "line") int line, @Param(name = "text", wildcard = true) String text) {
        if (line == 1)
            line1 = text;
        else {
            if (line != 2) {
                sender.sendMessage(ChatColor.RED + "Invalid line number...");
                return;
            }

            line2 = text;
        }

        sender.sendMessage(ChatColor.GREEN + "Line " + line + " set to \"" + text + "\"");
    }
}
