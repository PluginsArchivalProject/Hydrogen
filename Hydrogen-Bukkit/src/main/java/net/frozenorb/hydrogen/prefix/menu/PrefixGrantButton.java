package net.frozenorb.hydrogen.prefix.menu;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.prefix.PrefixGrant;
import lombok.RequiredArgsConstructor;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.util.TimeUtils;
import net.frozenorb.qlib.util.UUIDUtils;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class PrefixGrantButton extends Button {

    private final PrefixGrant prefixGrant;
    private final String addedByResolved;

    public String getName(Player player) {
        return ChatColor.YELLOW + TimeUtils.formatIntoCalendarString(new Date(this.prefixGrant.getAddedAt()));
    }

    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
        int seconds = (this.prefixGrant.getExpiresAt() > 0L) ? TimeUtils.getSecondsBetween(new Date(), new Date(this.prefixGrant.getExpiresAt())) : 0;
        String by = (this.prefixGrant.getAddedBy() == null) ? "Console" : this.addedByResolved;

        description.add(ChatColor.YELLOW + "By: " + ChatColor.RED + by);
        description.add(ChatColor.YELLOW + "Reason: " + ChatColor.RED + this.prefixGrant.getReason());
        description.add(ChatColor.YELLOW + "Scopes: " + ChatColor.RED + (this.prefixGrant.getScopes().isEmpty() ? "Global" : this.prefixGrant.getScopes()));
        description.add(ChatColor.YELLOW + "Prefix: " + ChatColor.RED + this.prefixGrant.getPrefix());

        if (this.prefixGrant.isActive()) {
            if (this.prefixGrant.getExpiresAt() != 0L) {
                description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
                description.add(ChatColor.YELLOW + "Time remaining: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds));
            } else {
                description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
                description.add(ChatColor.YELLOW + "This is a permanent prefix grant.");
            }
            if (player.hasPermission("minehq.prefixgrant.remove." + this.prefixGrant.getPrefix())) {
                description.add("");
                description.add(ChatColor.RED.toString() + ChatColor.BOLD + "Click to remove");
                description.add(ChatColor.RED.toString() + ChatColor.BOLD + "this prefix grant");
            }
        } else if (this.prefixGrant.isRemoved()) {
            String removedBy = (this.prefixGrant.getRemovedBy() == null) ? "Console" : UUIDUtils.name(this.prefixGrant.getRemovedBy());
            description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
            description.add(ChatColor.RED + "Removed:");
            description.add(ChatColor.YELLOW + removedBy + ": " + ChatColor.RED + this.prefixGrant.getRemovalReason());
            description.add(ChatColor.RED + "at " + ChatColor.YELLOW + TimeUtils.formatIntoCalendarString(new Date(this.prefixGrant.getRemovedAt())));

            if (this.prefixGrant.getExpiresAt() != 0L) {
                description.add("");
                description.add(ChatColor.YELLOW + "Duration: " + TimeUtils.formatIntoDetailedString((int) ((this.prefixGrant.getExpiresAt() - this.prefixGrant.getAddedAt()) / 1000L) + 1));
            }
        } else if (this.prefixGrant.isExpired()) {
            description.add(ChatColor.YELLOW + "Duration: " + TimeUtils.formatIntoDetailedString((int) ((this.prefixGrant.getExpiresAt() - this.prefixGrant.getAddedAt()) / 1000L) + 1));
            description.add(ChatColor.GREEN + "Expired");
        }

        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
        return description;
    }

    public Material getMaterial(Player player) {
        return Material.WOOL;
    }

    public byte getDamageValue(Player player) {
        return this.prefixGrant.isActive() ? DyeColor.LIME.getWoolData() : DyeColor.RED.getWoolData();
    }

    public void clicked(Player player, int i, ClickType clickType) {
        if (!player.hasPermission("minehq.prefixGrant.remove." + this.prefixGrant.getPrefix()) || !this.prefixGrant.isActive())
            return;

        player.closeInventory();
        ConversationFactory factory = new ConversationFactory(Hydrogen.getInstance())
                .withModality(true)
                .withPrefix(new NullConversationPrefix())
                .withFirstPrompt(new StringPrompt() {
                    public String getPromptText(ConversationContext context) {
                        return "§aType a reason to be used when removing this prefix grant. Type §cno§a to quit.";
                    }

                    public Prompt acceptInput(ConversationContext cc, String s) {
                        if (s.equalsIgnoreCase("no")) {
                            cc.getForWhom().sendRawMessage(ChatColor.GREEN + "Prefix grant removal aborted.");
                        } else {
                            Bukkit.getScheduler().runTaskAsynchronously(Hydrogen.getInstance(), () -> {
                                RequestResponse response = RequestHandler.delete("/prefixes/" + PrefixGrantButton.this.prefixGrant.getId(), ImmutableMap.of("removedBy", player.getUniqueId(), "removedByIp", player.getAddress().getAddress().getHostAddress(), "reason", s));
                                if (response.wasSuccessful()) {
                                    player.sendMessage(ChatColor.GREEN + "Removed prefix grant successfully.");
                                } else player.sendMessage(ChatColor.RED + response.getErrorMessage());
                            });
                        }
                        return END_OF_CONVERSATION;
                    }
                }).withLocalEcho(false).withEscapeSequence("/no").withTimeout(60).thatExcludesNonPlayersWithMessage("Go away evil console!");

        player.beginConversation(factory.buildConversation(player));
    }

}
