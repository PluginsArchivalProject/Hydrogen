package net.frozenorb.hydrogen.totp.prompt;

import net.frozenorb.hydrogen.Settings;
import lombok.RequiredArgsConstructor;
import org.bukkit.*;
import org.bukkit.conversations.*;

@RequiredArgsConstructor
public class InitialDisclaimerPrompt extends StringPrompt {

    private final Settings settings;

    public String getPromptText(ConversationContext context) {
        return ChatColor.RED.toString() + ChatColor.BOLD + "Take a minute to read over this, it's important. " + ChatColor.RED +
                "2FA can be enabled to protect against hackers getting into your Minecraft account." +
                " If you enable 2FA, you'll be required to enter a code every time you log in. If you lose your 2FA device," +
                " you won't be able to log in to " + this.settings.getNetworkName() + ". " + ChatColor.GRAY +
                "If you've read the above and would like to proceed, type \"yes\" in chat. Otherwise, type anything else.";
    }

    public Prompt acceptInput(ConversationContext context, String s) {
        if (s.equalsIgnoreCase("yes"))
            return new ScanMapPrompt();

        context.getForWhom().sendRawMessage(ChatColor.GREEN + "Aborted 2FA setup.");
        return END_OF_CONVERSATION;
    }
}
