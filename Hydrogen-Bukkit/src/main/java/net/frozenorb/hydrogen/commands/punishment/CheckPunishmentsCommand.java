package net.frozenorb.hydrogen.commands.punishment;

import net.frozenorb.hydrogen.commands.punishment.parameter.PunishmentTarget;
import net.frozenorb.hydrogen.punishment.menu.MainPunishmentMenu;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import net.frozenorb.qlib.util.Callback;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CheckPunishmentsCommand
{
    @Command(names = { "checkpunishments", "cp", "c" }, permission = "minehq.punishment.view", description = "Check a user's punishments")
    public static void checkPunishments(Player sender, @Param(name = "target") PunishmentTarget target) {
        target.resolveUUID((Callback<UUID>)(uuid -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "An error occurred when contacting the Mojang API.");
                return;
            }
            new MainPunishmentMenu(uuid.toString(), target.getName()).openMenu(sender);
        }));
    }
}
