package net.frozenorb.hydrogen.commands.grant.menu;

import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.commands.punishment.parameter.PunishmentTarget;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.profile.Profile;
import net.frozenorb.hydrogen.rank.Rank;
import net.frozenorb.hydrogen.server.ServerGroup;
import lombok.AllArgsConstructor;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.util.TimeUtils;
import com.google.common.collect.Lists;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class GrantButton extends Button {

    private Rank rank;
    private String targetName;
    private UUID targetUUID;
    private String reason;
    private ScopesMenu parent;
    private List<ServerGroup> scopes;
    private int duration;

    public String getName(Player player) {
        return ChatColor.GREEN + "Confirm and Grant";
    }

    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 30));
        description.add(ChatColor.BLUE + "Click to add the " + ChatColor.WHITE + this.rank.getFormattedName()
                + ChatColor.BLUE + " to " + ChatColor.WHITE + this.targetName + ChatColor.BLUE + ".");

        if (this.parent.isGlobal()) {
            description.add(ChatColor.BLUE + "This grant will be " + ChatColor.WHITE + "Global" + ChatColor.BLUE + ".");
        } else {
            List<String> scopes = this.scopes.stream().map(ServerGroup::getId).collect(Collectors.toList());
            description.add(ChatColor.BLUE + "This grant will apply on: " + ChatColor.WHITE + scopes);
        }

        description.add(ChatColor.BLUE + "Reasoning: " + ChatColor.WHITE + this.reason);
        description.add(ChatColor.BLUE + "Duration: " + ChatColor.WHITE + ((this.duration > 0) ? TimeUtils.formatIntoDetailedString(this.duration) : "Permanent"));
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 30));
        return description;
    }

    public Material getMaterial(Player player) {
        return Material.DIAMOND_SWORD;
    }

    public byte getDamageValue(Player player) {
        return 0;
    }

    public void clicked(Player player, int i, ClickType clickType) {
        this.grant(this.targetUUID, this.targetName, this.reason, this.scopes, this.rank, this.duration, player);
        player.closeInventory();
    }

    private void grant(UUID user, String targetName, String reason, List<ServerGroup> scopes, Rank rank, int expiresIn, Player sender) {
        Optional<Profile> profileOptional = Hydrogen.getInstance().getProfileHandler().getProfile(user);
        List<String> finalScopes = Lists.newArrayList();
        finalScopes.addAll(scopes.stream().map(ServerGroup::getId).collect(Collectors.toList()));
        Map<String, Object> body = new HashMap<>();

        body.put("user", user);
        body.put("reason", reason);
        body.put("scopes", finalScopes.toArray(new String[]{}));
        body.put("rank", rank.getId());
        if (expiresIn > 0) {
            body.put("expiresIn", expiresIn);
        }
        body.put("addedBy", sender.getUniqueId().toString());
        body.put("addedByIp", sender.getAddress().getAddress().getHostAddress());

        RequestResponse response = RequestHandler.post("/grants", body);
        if (profileOptional.isPresent()) {
            Hydrogen plugin = Hydrogen.getInstance();

            plugin.getProfileHandler().remove(user);
            plugin.getPermissionHandler().removeAttachment(Bukkit.getPlayer(user));
            plugin.getServerHandler().leave(user);
            plugin.getProfileHandler().loadProfile(user, targetName, Bukkit.getPlayer(user).getAddress().toString());
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    PunishmentTarget.getRecentlyDisconnected().remove(targetName), 600L);

            PunishmentTarget.getRecentlyDisconnected().add(targetName);
        }

        if (response.wasSuccessful()) {
            sender.sendMessage(ChatColor.GREEN + "Successfully granted " + ChatColor.WHITE + targetName + ChatColor.GREEN + " the " + ChatColor.WHITE + rank.getFormattedName() + ChatColor.GREEN + " rank.");
            this.parent.setComplete(true);
        } else sender.sendMessage(ChatColor.RED + response.getErrorMessage());

    }

}
