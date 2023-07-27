package net.frozenorb.hydrogen.commands.grant.menu;

import com.google.common.collect.Lists;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.rank.Rank;
import lombok.AllArgsConstructor;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.util.TimeUtils;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class RankButton extends Button {

    private String targetName;
    private UUID targetUUID;
    private Rank rank;

    public String getName(Player player) {
        return this.rank.getFormattedName();
    }

    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();

        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 30));
        description.add(ChatColor.BLUE + "Click to grant " + ChatColor.WHITE + this.targetName + ChatColor.BLUE + " the "
                + ChatColor.WHITE + this.rank.getFormattedName() + ChatColor.BLUE + " rank.");
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 30));

        return description;
    }

    public Material getMaterial(Player player) {
        return Material.WOOL;
    }

    public byte getDamageValue(Player player) {
        return this.getColor().getWoolData();
    }

    public void clicked(Player player, int i, ClickType clickType) {
        player.closeInventory();
        ConversationFactory factory = new ConversationFactory(Hydrogen.getInstance()).withModality(true).withPrefix(new NullConversationPrefix()).withFirstPrompt(new StringPrompt() {
            public String getPromptText(ConversationContext context) {
                return ChatColor.YELLOW + "Please type a reason for this grant to be added, or type " + ChatColor.RED + "cancel" + ChatColor.YELLOW + " to cancel.";
            }

            public Prompt acceptInput(ConversationContext context, String input) {
                if (input.equalsIgnoreCase("cancel")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + "Granting cancelled.");
                    return END_OF_CONVERSATION;
                }

                Bukkit.getScheduler().runTask(Hydrogen.getInstance(), () -> promptTime(player, input));
                return END_OF_CONVERSATION;
            }
        }).withEscapeSequence("/no").withLocalEcho(false).withTimeout(10).thatExcludesNonPlayersWithMessage("Go away evil console!");

        Conversation con = factory.buildConversation(player);
        player.beginConversation(con);
    }

    private void promptTime(Player player, String reason) {
        ConversationFactory factory = new ConversationFactory(Hydrogen.getInstance()).withModality(true).withPrefix(new NullConversationPrefix()).withFirstPrompt(new StringPrompt() {
            public String getPromptText(ConversationContext context) {
                return ChatColor.YELLOW + "Please type a duration for this grant, (\"perm\" for permanent) or type " + ChatColor.RED + "cancel" + ChatColor.YELLOW + " to cancel.";
            }

            public Prompt acceptInput(ConversationContext context, String input) {
                if (input.equalsIgnoreCase("cancel")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + "Granting cancelled.");
                    return END_OF_CONVERSATION;
                }

                int duration = -1;
                if (!StringUtils.startsWithIgnoreCase(input, "perm"))
                    duration = TimeUtils.parseTime(input);

                if (duration > 1 || StringUtils.startsWithIgnoreCase(input, "perm")) {
                    int finalDuration = duration;

                    Bukkit.getScheduler().runTask(Hydrogen.getInstance(), () ->
                            new ScopesMenu(false, false, rank, targetName, targetUUID, reason, finalDuration).openMenu(player));

                    return END_OF_CONVERSATION;
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + "Invalid duration.");
                return END_OF_CONVERSATION;
            }
        }).withEscapeSequence("/no").withLocalEcho(false).withTimeout(10).thatExcludesNonPlayersWithMessage("Go away evil console!");

        Conversation con = factory.buildConversation(player);
        player.beginConversation(con);
    }

    private DyeColor getColor() {
        ChatColor color = ChatColor.getByChar(this.rank.getGameColor().charAt(1));
        switch (color) {
            case DARK_BLUE:
            case BLUE: {
                return DyeColor.BLUE;
            }
            case DARK_GREEN: {
                return DyeColor.GREEN;
            }
            case DARK_AQUA:
            case AQUA: {
                return DyeColor.CYAN;
            }
            case DARK_RED:
            case RED: {
                return DyeColor.RED;
            }
            case DARK_PURPLE: {
                return DyeColor.PURPLE;
            }
            case GOLD: {
                return DyeColor.ORANGE;
            }
            case GRAY:
            case DARK_GRAY: {
                return DyeColor.GRAY;
            }
            case GREEN: {
                return DyeColor.LIME;
            }
            case LIGHT_PURPLE: {
                return DyeColor.PINK;
            }
            case YELLOW: {
                return DyeColor.YELLOW;
            }
            case WHITE: {
                return DyeColor.WHITE;
            }
            default: {
                return DyeColor.BLACK;
            }
        }
    }

}