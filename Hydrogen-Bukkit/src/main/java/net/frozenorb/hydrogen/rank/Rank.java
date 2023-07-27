package net.frozenorb.hydrogen.rank;

import com.google.common.primitives.Ints;
import net.frozenorb.hydrogen.Hydrogen;
import lombok.Data;
import net.frozenorb.qlib.qLib;
import org.bukkit.ChatColor;

import java.util.Comparator;

@Data
public class Rank {

    private String id;
    private String inheritsFromId;
    private int generalWeight;
    private int displayWeight;
    private String displayName;
    private String gamePrefix;
    private String gameColor;
    private boolean staffRank;
    private boolean grantRequiresTotp;
    private String queueMessage;

    public static Comparator<Rank> GENERAL_WEIGHT_COMPARATOR = ((a, b) -> Ints.compare(b.getGeneralWeight(), a.getGeneralWeight()));
    public static Comparator<Rank> DISPLAY_WEIGHT_COMPARATOR = ((a, b) -> Ints.compare(b.getDisplayWeight(), a.getDisplayWeight()));

    @Override
    public String toString() {
        return qLib.GSON.toJson(this);
    }

    public String getGameColor() {
        if (Hydrogen.getInstance().getSettings().isClean())
            return ChatColor.translateAlternateColorCodes('&', this.gameColor.replace("&l", ""));

        return ChatColor.translateAlternateColorCodes('&', this.gameColor);
    }

    public String getGamePrefix() {
        if (Hydrogen.getInstance().getSettings().isClean())
            return ChatColor.translateAlternateColorCodes('&', this.gamePrefix.replace("&l", ""));

        return ChatColor.translateAlternateColorCodes('&', this.gamePrefix);
    }

    public String getFormattedName() {
        return gameColor + displayName;
    }

}