package net.frozenorb.hydrogen.grant.menu;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.grant.Grant;
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
public class GrantButton extends Button {

    private final Grant grant;
    private final String addedByResolved;

    public String getName(final Player player) {
        return ChatColor.YELLOW + TimeUtils.formatIntoCalendarString(new Date(this.grant.getAddedAt()));
    }

    public List<String> getDescription(final Player player) {
        final List<String> description = Lists.newArrayList();
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
        final int seconds = (this.grant.getExpiresAt() > 0L) ? TimeUtils.getSecondsBetween(new Date(), new Date(this.grant.getExpiresAt())) : 0;
        final String by = (this.grant.getAddedBy() == null) ? "Console" : this.addedByResolved;

        description.add(ChatColor.YELLOW + "By: " + ChatColor.RED + by);
        description.add(ChatColor.YELLOW + "Reason: " + ChatColor.RED + this.grant.getReason());
        description.add(ChatColor.YELLOW + "Scopes: " + ChatColor.RED + (this.grant.getScopes().isEmpty() ? "Global" : this.grant.getScopes()));
        description.add(ChatColor.YELLOW + "Rank: " + ChatColor.RED + this.grant.getRank());

        if (this.grant.isActive()) {
            if (this.grant.getExpiresAt() != 0L) {
                description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
                description.add(ChatColor.YELLOW + "Time remaining: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds));
            } else {
                description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
                description.add(ChatColor.YELLOW + "This is a permanent grant.");
            }
            if (player.hasPermission("minehq.grant.remove." + this.grant.getRank())) {
                description.add("");
                description.add(ChatColor.RED.toString() + ChatColor.BOLD + "Click to remove");
                description.add(ChatColor.RED.toString() + ChatColor.BOLD + "this grant");
            }
        } else if (this.grant.isRemoved()) {
            final String removedBy = (this.grant.getRemovedBy() == null) ? "Console" : UUIDUtils.name(this.grant.getRemovedBy());
            description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
            description.add(ChatColor.RED + "Removed:");
            description.add(ChatColor.YELLOW + removedBy + ": " + ChatColor.RED + this.grant.getRemovalReason());
            description.add(ChatColor.RED + "at " + ChatColor.YELLOW + TimeUtils.formatIntoCalendarString(new Date(this.grant.getRemovedAt())));

            if (this.grant.getExpiresAt() != 0L) {
                description.add("");
                description.add(ChatColor.YELLOW + "Duration: " + TimeUtils.formatIntoDetailedString((int) ((this.grant.getExpiresAt() - this.grant.getAddedAt()) / 1000L) + 1));
            }
        } else if (this.grant.isExpired()) {
            description.add(ChatColor.YELLOW + "Duration: " + TimeUtils.formatIntoDetailedString((int) ((this.grant.getExpiresAt() - this.grant.getAddedAt()) / 1000L) + 1));
            description.add(ChatColor.GREEN + "Expired");
        }

        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
        return description;
    }

    public Material getMaterial(final Player player) {
        return Material.WOOL;
    }

    public byte getDamageValue(final Player player) {
        return this.grant.isActive() ? DyeColor.LIME.getWoolData() : DyeColor.RED.getWoolData();
    }

    public void clicked(final Player player, final int i, final ClickType clickType) {
        if (!player.hasPermission("minehq.grant.remove." + this.grant.getRank()) || !this.grant.isActive())
            return;

        player.closeInventory();
        final ConversationFactory factory = new ConversationFactory(Hydrogen.getInstance()).withModality(true)
                .withPrefix(new NullConversationPrefix()).withFirstPrompt(new StringPrompt() {
                    public String getPromptText(final ConversationContext context) {
                        return "§aType a reason to be used when removing this grant. Type §cno§a to quit.";
                    }

                    public Prompt acceptInput(final ConversationContext cc, final String s) {
                        if (s.equalsIgnoreCase("no")) {
                            cc.getForWhom().sendRawMessage(ChatColor.GREEN + "Grant removal aborted.");
                        } else {
                            Bukkit.getScheduler().runTaskAsynchronously(Hydrogen.getInstance(), () -> {
                                final RequestResponse response = RequestHandler.delete("/grants/" + GrantButton.this.grant.getId(), ImmutableMap.of("removedBy", player.getUniqueId(), "removedByIp", player.getAddress().getAddress().getHostAddress(), "reason", s));
                                if (response.wasSuccessful()) {
                                    player.sendMessage(ChatColor.GREEN + "Removed grant successfully.");
                                } else {
                                    player.sendMessage(ChatColor.RED + response.getErrorMessage());
                                }
                            });
                        }
                        return END_OF_CONVERSATION;
                    }
                }).withLocalEcho(false).withEscapeSequence("/no").withTimeout(60).thatExcludesNonPlayersWithMessage("Go away evil console!");

        final Conversation con = factory.buildConversation(player);
        player.beginConversation(con);
    }

}
