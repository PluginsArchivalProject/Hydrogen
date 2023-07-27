package net.frozenorb.hydrogen.server.heartbeat;

import com.google.common.collect.ImmutableMap;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.profile.Profile;
import net.frozenorb.hydrogen.rank.Rank;
import net.frozenorb.hydrogen.util.PermissionUtils;
import lombok.Getter;
import net.frozenorb.qlib.qLib;
import net.frozenorb.qlib.util.TPSUtils;
import net.minecraft.util.com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HeartbeatTask extends BukkitRunnable {

    private boolean first;

    @Getter
    private static Queue<Map<String, Object>> eventQueue = new ConcurrentLinkedQueue<>();

    public HeartbeatTask() {
        this.first = true;
    }

    public void run() {
        Map<String, Object> onlinePlayers = new HashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", player.getName());
            data.put("userIp", player.getAddress().getAddress().getHostAddress());
            onlinePlayers.put(player.getUniqueId().toString(), data);
        }

        List<Map<String, Object>> events = new LinkedList<>();
        Map<String, Object> event;
        while ((event = HeartbeatTask.eventQueue.poll()) != null)
            events.add(event);

        RequestResponse response = RequestHandler.post("/servers/heartbeat", ImmutableMap.of("players", onlinePlayers, "lastTps", TPSUtils.getTPS(), "events", events, "permissionsNeeded", this.first));
        if (response.wasSuccessful()) {
            JSONObject json = response.asJSONObject();
            JSONObject playersJson = json.getJSONObject("players");

            for (String key : playersJson.keySet()) {
                UUID uuid = UUID.fromString(key);
                JSONObject info = playersJson.getJSONObject(key);
                Optional<Profile> profileOptional = Hydrogen.getInstance().getProfileHandler().getProfile(uuid);

                Profile profile;
                Player player = Bukkit.getPlayer(uuid);
                if (profileOptional.isPresent()) {
                    profile = profileOptional.get();
                    profile.update(uuid, info);
                } else {
                    profile = new Profile(uuid, info);
                    Hydrogen.getInstance().getProfileHandler().getProfiles().put(uuid, profile);
                }

                if (player == null)
                    continue;

                profile.updatePlayer(player);
            }

            if (json.has("permissions")) {
                Map<String, List<String>> ret = qLib.PLAIN_GSON.fromJson(json.get("permissions").toString(), new TypeToken<Map<String, List<String>>>() {}.getType());
                ConcurrentHashMap<Rank, Map<String, Boolean>> newCache = new ConcurrentHashMap<>();

                for (Map.Entry<String, List<String>> entry : ret.entrySet())
                    newCache.put(Hydrogen.getInstance().getRankHandler().getRank(entry.getKey()).get(), PermissionUtils.convertFromList(entry.getValue()));

                Hydrogen.getInstance().getPermissionHandler().setPermissionCache(newCache);
            }
        } else Bukkit.getLogger().warning("Heartbeat - Could not POST server heartbeat: " + response.getErrorMessage());

        this.first = true;
    }

}
