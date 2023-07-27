package net.frozenorb.hydrogen.commands.prefix.setmenu;

import com.google.common.collect.Lists;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.prefix.Prefix;
import net.frozenorb.hydrogen.profile.Profile;
import net.frozenorb.qlib.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PrefixButton extends Button
{
    private String targetName;
    private UUID targetUUID;
    private Prefix prefix;
    private boolean active;
    private boolean authorized;
    
    public String getName(Player player) {
        return ChatColor.translateAlternateColorCodes('&', this.prefix.getButtonName());
    }
    
    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();
        if (this.active) {
            description.add(ChatColor.RED + "This is already your prefix.");
            description.add(ChatColor.RED + "Click to disable.");
        }
        else if (this.authorized) {
            description.add(ChatColor.GRAY + "Click to make this your prefix.");
        }
        else if (this.prefix.isPurchaseable()) {
            if (this.prefix.getButtonDescription().contains("%newline%")) {
                Arrays.stream(this.prefix.getButtonDescription().split("%newline%")).map(string -> ChatColor.translateAlternateColorCodes('&', string)).forEach(description::add);
            }
            else {
                description.add(ChatColor.translateAlternateColorCodes('&', this.prefix.getButtonDescription()));
            }
        }
        else {
            description.add(ChatColor.GRAY + "This prefix is unavailable and unpurchaseable.");
        }
        return description;
    }
    
    public Material getMaterial(Player player) {
        return Material.WOOL;
    }
    
    public byte getDamageValue(Player player) {
        return this.getColor().getWoolData();
    }
    
    public void clicked(Player player, int i, ClickType clickType) {
        if (this.active) {
            Profile playerProfile = Hydrogen.getInstance().getProfileHandler().getProfile(player.getUniqueId()).get();
            playerProfile.setActivePrefix(null);
            playerProfile.updatePlayer(player);
            player.sendMessage(ChatColor.GREEN + "Removed prefix.");
            return;
        }
        if (!this.authorized) {
            player.sendMessage(ChatColor.RED + "You can't use this prefix!");
            if (this.prefix.isPurchaseable()) {
                player.sendMessage(ChatColor.RED + "But it's available for a limited time at store.minehq.com!");
            }
            return;
        }
        Profile playerProfile = Hydrogen.getInstance().getProfileHandler().getProfile(player.getUniqueId()).get();
        playerProfile.setActivePrefix(this.prefix);
        playerProfile.updatePlayer(player);
        player.sendMessage(ChatColor.GREEN + "Updated prefix.");
    }
    
    private DyeColor getColor() {
        return this.active ? DyeColor.GREEN : (this.authorized ? DyeColor.RED : DyeColor.GRAY);
    }
    
    public PrefixButton(String targetName, UUID targetUUID, Prefix prefix, boolean active, boolean authorized) {
        this.targetName = targetName;
        this.targetUUID = targetUUID;
        this.prefix = prefix;
        this.active = active;
        this.authorized = authorized;
    }
}
