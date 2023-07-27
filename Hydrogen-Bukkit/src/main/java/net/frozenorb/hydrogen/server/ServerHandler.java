package net.frozenorb.hydrogen.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.server.heartbeat.HeartbeatTask;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.connection.RequestHandler;
import lombok.Getter;
import net.frozenorb.qlib.qLib;
import net.minecraft.util.com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
public class ServerHandler {

    private Set<ChatFilterEntry> chatFilter;
    private Set<ServerGroup> serverGroups = Sets.newHashSet();
    private Set<Server> servers = Sets.newHashSet();

    public ServerHandler() {
        new HeartbeatTask().runTaskTimerAsynchronously(Hydrogen.getInstance(), 5L, 600L);
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(Hydrogen.getInstance(), this::refresh, 0L, 6000L);
    }

    private void refresh() {
        RequestResponse response = RequestHandler.get("/serverGroups");
        if (response.wasSuccessful()) {
            this.serverGroups = qLib.PLAIN_GSON.fromJson(response.getResponse(), new TypeToken<Set<ServerGroup>>() {}.getType());
        } else Bukkit.getLogger().warning("ServerHandler - Could not get server groups from API: " + response.getErrorMessage());

        response = RequestHandler.get("/servers");
        if (response.wasSuccessful()) {
            this.servers = qLib.PLAIN_GSON.fromJson(response.getResponse(), new TypeToken<Set<Server>>() {}.getType());
        } else Bukkit.getLogger().warning("ServerHandler - Could not get servers from API: " + response.getErrorMessage());

        response = RequestHandler.get("/chatFilter");
        if (response.wasSuccessful()) {
            this.chatFilter = qLib.PLAIN_GSON.fromJson(response.getResponse(), new TypeToken<Set<ChatFilterEntry>>() {}.getType());
        } else Bukkit.getLogger().warning("ServerHandler - Could not get chat filter list from API: " + response.getErrorMessage());

        response = RequestHandler.get("/whoami");
        if (response.wasSuccessful()) {
            String id = response.asJSONObject().getString("name");
            this.getServer(id).ifPresent(server -> {
                try {
                    ((CraftServer) Bukkit.getServer()).setServerGroup(server.getServerGroup());
                } catch (NoSuchMethodError ignored) {}
            });
        } else Bukkit.getLogger().warning("ServerHandler - Could not load our identity from API: " + response.getErrorMessage());
    }

    public Optional<ServerGroup> getServerGroup(String parse) {
        for (ServerGroup group : this.serverGroups) {
            if (group.getId().equalsIgnoreCase(parse))
                return Optional.of(group);
        }
        return Optional.empty();
    }

    public Optional<Server> getServer(String parse) {
        for (Server server : this.servers) {
            if (server.getId().equalsIgnoreCase(parse) || server.getDisplayName().equalsIgnoreCase(parse))
                return Optional.of(server);
        }
        return Optional.empty();
    }

    public Optional<Server> find(UUID player) {
        RequestResponse response = RequestHandler.get("/users/" + player.toString());
        if (!response.wasSuccessful())
            return Optional.empty();

        JSONObject json = response.asJSONObject();
        if (json.has("online") && !json.getBoolean("online"))
            return Optional.empty();

        String server = json.has("lastSeenOn") ? json.getString("lastSeenOn") : null;
        if (server == null)
            return Optional.empty();

        for (Server serverObject : this.servers) {
            if (serverObject.getId().equalsIgnoreCase(server))
                return Optional.of(serverObject);
        }
        return Optional.empty();
    }

    public void leave(UUID player) {
        HeartbeatTask.getEventQueue().offer(ImmutableMap.of("type", "leave", "user", player));
    }

    public List<Server> getServers() {
        return Lists.newArrayList(this.servers);
    }

}
