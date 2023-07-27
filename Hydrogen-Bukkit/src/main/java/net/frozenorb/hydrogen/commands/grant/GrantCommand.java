package net.frozenorb.hydrogen.commands.grant;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.commands.grant.menu.RanksMenu;
import net.frozenorb.hydrogen.commands.punishment.parameter.PunishmentTarget;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GrantCommand {

    @Command(names = {"grant"}, permission = "hydrogen.grant", description = "Add a grant to an user's account - Cute GUI!")
    public static void grant(Player sender, @Param(name = "player") PunishmentTarget target) {
        target.resolveUUID(uuid -> Bukkit.getScheduler().runTask(Hydrogen.getInstance(), () ->
                new RanksMenu(target.getName(), uuid).openMenu(sender)));
    }

}
