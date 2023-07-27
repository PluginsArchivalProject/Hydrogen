package net.frozenorb.hydrogen.commands;

import net.frozenorb.hydrogen.commands.prefix.setmenu.PrefixMenu;
import org.bukkit.entity.*;
import net.frozenorb.qlib.command.*;

public class PrefixCommand {

    @Command(names = {"prefix", "setprefix", "setprefixes"}, permission = "")
    public static void prefix(Player sender) {
        new PrefixMenu(sender.getName(), sender.getUniqueId()).openMenu(sender);
    }

}
