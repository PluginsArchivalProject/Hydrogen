package net.frozenorb.hydrogen.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;

import java.text.MessageFormat;

@Getter
@RequiredArgsConstructor
public enum Constants {

    BAN_MESSAGE(ChatColor.RED + "Your account has been suspended from the {0} Network. \n\nAppeal at {1}/support"),
    BLACKLIST_MESSAGE(ChatColor.RED + "Your account has been blacklisted from the {0} Network. \n\nThis type of punishment cannot be appealed.");
    ;

    private final String message;

    public String format(Object... strings) {
        return MessageFormat.format(this.message, strings);
    }

}
