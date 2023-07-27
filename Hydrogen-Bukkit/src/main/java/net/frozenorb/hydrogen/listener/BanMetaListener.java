package net.frozenorb.hydrogen.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BanMetaListener implements Listener {

    @Getter
    private static final Map<UUID, Long> joinTime = Maps.newConcurrentMap();

    @Getter
    private static final Map<UUID, List<String>> messages = Maps.newConcurrentMap();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        messages.putIfAbsent(player.getUniqueId(), Lists.newArrayList());
        messages.get(player.getUniqueId()).add(event.getMessage());

        if (messages.get(player.getUniqueId()).size() > 30)
            messages.get(player.getUniqueId()).remove(0);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        joinTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        joinTime.remove(player.getUniqueId());
        messages.remove(player.getUniqueId());
    }

}