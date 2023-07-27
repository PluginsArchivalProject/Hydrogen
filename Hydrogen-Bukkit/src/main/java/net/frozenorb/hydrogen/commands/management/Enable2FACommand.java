package net.frozenorb.hydrogen.commands.management;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.Settings;
import net.frozenorb.qlib.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Enable2FACommand {

    @Command(names = {"enable2fa"}, permission = "super", description = "Enable two-factor authentication on the server")
    public static void enable2fa(CommandSender sender) {
        Settings settings = Hydrogen.getInstance().getSettings();
        settings.setForceStaffTotp(true);
        settings.save();

        sender.sendMessage(ChatColor.GREEN + "2FA set to \"" + ChatColor.WHITE + true + ChatColor.GREEN + "\"");
    }

    @Command(names = {"disable2fa"}, permission = "super", description = "Enable two-factor authentication on the server")
    public static void disable2fa(CommandSender sender) {
        Settings settings = Hydrogen.getInstance().getSettings();
        settings.setForceStaffTotp(false);
        settings.save();

        sender.sendMessage(ChatColor.GREEN + "2FA set to \"" + ChatColor.WHITE + false + ChatColor.GREEN + "\"");
    }

}
