package net.frozenorb.hydrogen.commands.prefix.menu;

import com.google.common.collect.Lists;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.prefix.Prefix;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.util.TimeUtils;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class PrefixButton extends Button
{
    private String targetName;
    private UUID targetUUID;
    private Prefix prefix;
    
    public String getName(Player player) {
        return this.prefix.getDisplayName();
    }
    
    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 30));
        description.add(ChatColor.BLUE + "Click to grant " + ChatColor.WHITE + this.targetName + ChatColor.BLUE + " the " + ChatColor.WHITE + this.prefix.getDisplayName() + ChatColor.BLUE + " prefix.");
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
        ConversationFactory factory = new ConversationFactory((Plugin) Hydrogen.getInstance()).withModality(true).withPrefix((ConversationPrefix)new NullConversationPrefix()).withFirstPrompt((Prompt)new StringPrompt() {
            public String getPromptText(ConversationContext context) {
                return ChatColor.YELLOW + "Please type a reason for this prefix grant to be added, or type " + ChatColor.RED + "cancel" + ChatColor.YELLOW + " to cancel.";
            }
            
            public Prompt acceptInput(ConversationContext context, String input) {
                if (input.equalsIgnoreCase("cancel")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + "Granting cancelled.");
                    return END_OF_CONVERSATION;
                }
                new BukkitRunnable() {
                    public void run() {
                        PrefixButton.this.promptTime(player, input);
                    }
                }.runTask((Plugin) Hydrogen.getInstance());
                return END_OF_CONVERSATION;
            }
        }).withEscapeSequence("/no").withLocalEcho(false).withTimeout(10).thatExcludesNonPlayersWithMessage("Go away evil console!");
        Conversation con = factory.buildConversation((Conversable)player);
        player.beginConversation(con);
    }
    
    private void promptTime(Player player, String reason) {
        ConversationFactory factory = new ConversationFactory((Plugin) Hydrogen.getInstance()).withModality(true).withPrefix((ConversationPrefix)new NullConversationPrefix()).withFirstPrompt((Prompt)new StringPrompt() {
            public String getPromptText(ConversationContext context) {
                return ChatColor.YELLOW + "Please type a duration for this grant, (\"perm\" for permanent) or type " + ChatColor.RED + "cancel" + ChatColor.YELLOW + " to cancel.";
            }
            
            public Prompt acceptInput(ConversationContext context, String input) {
                if (input.equalsIgnoreCase("cancel")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + "Granting cancelled.");
                    return END_OF_CONVERSATION;
                }
                int duration = -1;
                if (!StringUtils.startsWithIgnoreCase((CharSequence)input, (CharSequence)"perm")) {
                    duration = TimeUtils.parseTime(input);
                }
                if (duration > 1 || StringUtils.startsWithIgnoreCase((CharSequence)input, (CharSequence)"perm")) {
                    int finalDuration = duration;
                    new BukkitRunnable() {
                        public void run() {
                            new ScopesMenu(false, false, PrefixButton.this.prefix, PrefixButton.this.targetName, PrefixButton.this.targetUUID, reason, finalDuration).openMenu(player);
                        }
                    }.runTask((Plugin) Hydrogen.getInstance());
                    return END_OF_CONVERSATION;
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + "Invalid duration.");
                return END_OF_CONVERSATION;
            }
        }).withEscapeSequence("/no").withLocalEcho(false).withTimeout(10).thatExcludesNonPlayersWithMessage("Go away evil console!");
        Conversation con = factory.buildConversation((Conversable)player);
        player.beginConversation(con);
    }
    
    private DyeColor getColor() {
        String lastColors = this.prefix.getFormattedPrefix();
        ChatColor color = ChatColor.getByChar(lastColors.charAt(lastColors.indexOf(167) + 1));
        switch (color) {
            case DARK_BLUE: {
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
            case BLUE: {
                return DyeColor.BLUE;
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
    
    public PrefixButton(String targetName, UUID targetUUID, Prefix prefix) {
        this.targetName = targetName;
        this.targetUUID = targetUUID;
        this.prefix = prefix;
    }
}
