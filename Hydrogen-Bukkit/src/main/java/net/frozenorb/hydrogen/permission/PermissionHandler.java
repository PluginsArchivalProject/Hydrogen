package net.frozenorb.hydrogen.permission;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.rank.Rank;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Setter
public class PermissionHandler {

    private static Field permissionsField;
    private Map<Rank, Map<String, Boolean>> permissionCache = new ConcurrentHashMap<>();
    private Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();

    public Map<String, Boolean> getPermissions(Rank rank) {
        return this.permissionCache.get(rank);
    }

    public void update(Player player, Map<String, Boolean> permissions) {
        this.attachments.computeIfAbsent(player.getUniqueId(), i -> player.addAttachment(Hydrogen.getInstance()));
        PermissionAttachment attachment = this.attachments.get(player.getUniqueId());

        try {
            permissionsField.set(attachment, permissions);
            player.recalculatePermissions();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void removeAttachment(Player player) {
        this.attachments.remove(player.getUniqueId());
    }

    static {
        try {
            (permissionsField = PermissionAttachment.class.getDeclaredField("permissions")).setAccessible(true);
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        }
    }

}