package net.frozenorb.hydrogen.commands.prefix.menu;

import com.google.common.collect.Lists;
import net.frozenorb.hydrogen.server.Server;
import net.frozenorb.qlib.menu.Button;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class ScopeButton extends Button
{
    private ScopesMenu parent;
    private Server scope;
    
    public String getName(Player player) {
        boolean status = this.parent.getStatus().get(this.scope.getId());
        return (status ? ChatColor.GREEN : ChatColor.RED) + this.scope.getId();
    }
    
    public List<String> getDescription(Player player) {
        boolean status = this.parent.getStatus().get(this.scope.getId());
        List<String> description = Lists.newArrayList();
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 30));
        if (status) {
            description.add(ChatColor.BLUE + "Click to " + ChatColor.RED + "remove " + ChatColor.YELLOW + this.scope.getId() + ChatColor.BLUE + " from this grant's scopes.");
        }
        else {
            description.add(ChatColor.BLUE + "Click to " + ChatColor.GREEN + "add " + ChatColor.YELLOW + this.scope.getId() + ChatColor.BLUE + " to this grant's scopes.");
        }
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 30));
        return description;
    }
    
    public Material getMaterial(Player player) {
        return Material.WOOL;
    }
    
    public byte getDamageValue(Player player) {
        boolean status = this.parent.getStatus().get(this.scope.getId());
        return status ? DyeColor.LIME.getWoolData() : DyeColor.GRAY.getWoolData();
    }
    
    public void clicked(Player player, int i, ClickType clickType) {
        this.parent.getStatus().put(this.scope.getId(), !this.parent.getStatus().getOrDefault(this.scope.getId(), false));
        this.parent.setGlobal(false);
    }
    
    public ScopeButton(ScopesMenu parent, Server scope) {
        this.parent = parent;
        this.scope = scope;
    }
}
