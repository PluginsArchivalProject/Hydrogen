package net.frozenorb.hydrogen.punishment.menu;

import com.google.common.collect.Lists;
import net.frozenorb.hydrogen.punishment.Punishment;
import lombok.RequiredArgsConstructor;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.util.TimeUtils;
import net.frozenorb.qlib.util.UUIDUtils;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class PunishmentButton extends Button {

    private final Punishment punishment;
    private final String addedByResolved;
    private final boolean showReason;

    public String getName(Player player) {
        return ChatColor.YELLOW + TimeUtils.formatIntoCalendarString(new Date(this.punishment.getAddedAt()));
    }

    public Material getMaterial(Player player) {
        return Material.WOOL;
    }

    public byte getDamageValue(Player player) {
        return this.punishment.isActive() ? DyeColor.RED.getWoolData() : DyeColor.LIME.getWoolData();
    }

    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
        String by = (this.punishment.getAddedBy() == null) ? "Console" : this.addedByResolved;
        int seconds = (this.punishment.getExpiresAt() > 0L) ? TimeUtils.getSecondsBetween(new Date(), new Date(this.punishment.getExpiresAt())) : 0;
        String actor = this.punishment.getActorType() + ChatColor.YELLOW + " : " + ChatColor.RED + this.punishment.getActorName();

        if (this.punishment.getActorType().equals("Website"))
            actor = this.punishment.getActorType();

        description.add(ChatColor.YELLOW + "By: " + ChatColor.RED + by);
        description.add(ChatColor.YELLOW + "Added on: " + ChatColor.RED + actor);
        if (this.showReason || this.punishment.getAddedBy() == null)
            description.add(ChatColor.YELLOW + "Reason: " + ChatColor.RED + this.punishment.getPublicReason());

        if (player.hasPermission("minehq.punishment.view.internal") || player.getUniqueId().equals(this.punishment.getAddedBy()))
            description.add(ChatColor.YELLOW + "Internal reason: " + ChatColor.RED + this.punishment.getPrivateReason());

        if (this.punishment.isActive()) {
            if (this.punishment.getExpiresAt() != 0L) {
                description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
                description.add(ChatColor.YELLOW + "Time remaining: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds));
            } else {
                description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
                description.add(ChatColor.YELLOW + "This is a permanent punishment.");
            }
        } else if (this.punishment.isRemoved()) {
            String removedBy = (this.punishment.getRemovedBy() == null) ? "Console" : UUIDUtils.name(this.punishment.getRemovedBy());
            description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
            description.add(ChatColor.RED + "Removed:");
            description.add(ChatColor.YELLOW + removedBy + ": " + ChatColor.RED + this.punishment.getRemovalReason());
            description.add(ChatColor.RED + "at " + ChatColor.YELLOW + TimeUtils.formatIntoCalendarString(new Date(this.punishment.getRemovedAt())));
            if (this.punishment.getExpiresAt() != 0L) {
                description.add("");
                description.add(ChatColor.YELLOW + "Duration: " + TimeUtils.formatIntoDetailedString((int) ((this.punishment.getExpiresAt() - this.punishment.getAddedAt()) / 1000L) + 1));
            }
        } else if (this.punishment.isExpired()) {
            description.add(ChatColor.YELLOW + "Duration: " + TimeUtils.formatIntoDetailedString((int) ((this.punishment.getExpiresAt() - this.punishment.getAddedAt()) / 1000L) + 1));
            description.add(ChatColor.GREEN + "Expired");
        }

        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
        return description;
    }

    public void clicked(Player player, int i, ClickType clickType) {
    }

}
