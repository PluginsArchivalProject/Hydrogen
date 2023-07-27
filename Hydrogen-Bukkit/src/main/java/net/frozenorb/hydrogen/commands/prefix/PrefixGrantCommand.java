package net.frozenorb.hydrogen.commands.prefix;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.commands.prefix.menu.PrefixMenu;
import net.frozenorb.hydrogen.commands.punishment.parameter.PunishmentTarget;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PrefixGrantCommand
{
    @Command(names = { "grantprefix", "prefixgrant" }, permission = "hydrogen.grantprefix", description = "Add a prefix grant to a users account - Cute GUI!")
    public static void grant(Player sender, @Param(name = "player") PunishmentTarget target) {
        target.resolveUUID(uuid -> new BukkitRunnable() {
            public void run() {
                new PrefixMenu(target.getName(), uuid).openMenu(sender);
            }
        }.runTask(Hydrogen.getInstance()));
    }
}
