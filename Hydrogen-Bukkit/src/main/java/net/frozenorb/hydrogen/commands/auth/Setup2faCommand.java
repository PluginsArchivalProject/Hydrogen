package net.frozenorb.hydrogen.commands.auth;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.totp.prompt.InitialDisclaimerPrompt;
import net.frozenorb.qlib.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

public class Setup2faCommand {

    @Command(names = {"setup2fa", "2fasetup"}, permission = "minehq.totp.setup", description = "Sign up to use 2FA to verify your identity")
    public static void setup2fa(Player sender) {
        Hydrogen plugin = Hydrogen.getInstance();
        if (plugin.getProfileHandler().getTotpEnabled().contains(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You already have 2FA setup!");
            return;
        }

        ConversationFactory factory = new ConversationFactory(plugin)
                .withFirstPrompt(new InitialDisclaimerPrompt(plugin.getSettings()))
                .withLocalEcho(false)
                .thatExcludesNonPlayersWithMessage("Go away evil console!");

        sender.beginConversation(factory.buildConversation(sender));
    }

}
