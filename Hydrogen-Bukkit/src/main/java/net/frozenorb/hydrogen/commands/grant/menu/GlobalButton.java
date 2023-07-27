package net.frozenorb.hydrogen.commands.grant.menu;

import lombok.RequiredArgsConstructor;
import net.frozenorb.qlib.menu.*;
import org.bukkit.entity.*;
import com.google.common.collect.*;
import org.bukkit.*;
import org.bukkit.event.inventory.*;

import java.util.*;

@RequiredArgsConstructor
public class GlobalButton extends Button {

    private final ScopesMenu parent;

    public String getName(Player player) {
        return ChatColor.BLUE + "Global";
    }

    public List<String> getDescription(Player player) {
        return ImmutableList.of();
    }

    public Material getMaterial(Player player) {
        return Material.WOOL;
    }

    public byte getDamageValue(Player player) {
        return this.parent.isGlobal() ? DyeColor.LIME.getWoolData() : DyeColor.GRAY.getWoolData();
    }

    public void clicked(Player player, int i, ClickType clickType) {
        for (String key : this.parent.getStatus().keySet())
            this.parent.getStatus().put(key, false);

        this.parent.setGlobal(!this.parent.isGlobal());
    }

}
