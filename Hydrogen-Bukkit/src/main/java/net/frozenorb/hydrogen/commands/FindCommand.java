package net.frozenorb.hydrogen.commands;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.commands.punishment.parameter.PunishmentTarget;
import net.frozenorb.hydrogen.server.Server;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public class FindCommand {

    @Command(names = { "find" }, permission = "hydrogen.find", description = "See the server an user is currently playing on", async = true)
    public static void find(CommandSender sender, @Param(name = "player") PunishmentTarget target) {
        target.resolveUUID((uuid -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "An error occurred when contacting the Mojang API.");
                return;
            }

            Optional<Server> serverOptional = Hydrogen.getInstance().getServerHandler().find(uuid);
            if (!serverOptional.isPresent()) {
                sender.sendMessage(ChatColor.RED + target.getName() + " is currently not on the network.");
                return;
            }

            Server server = serverOptional.get();
            sender.sendMessage(ChatColor.BLUE + target.getName() + ChatColor.YELLOW + " is currently on " + ChatColor.BLUE + server.getDisplayName() + ChatColor.YELLOW + ".");
        }));
    }

}