package net.frozenorb.hydrogen.profile;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import lombok.Getter;
import net.frozenorb.qlib.qLib;
import net.minecraft.util.com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;

import java.util.*;

@Getter
public class ProfileHandler {

    private Set<UUID> totpEnabled = new HashSet<>();
    private final Map<UUID, Profile> profiles = Maps.newConcurrentMap();

    public ProfileHandler() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(Hydrogen.getInstance(), this::refresh, 0L, 2400L);
    }

    private void refresh() {
        RequestResponse response = RequestHandler.get("/dumps/totp");
        if (response.wasSuccessful()) {
            this.totpEnabled = qLib.PLAIN_GSON.fromJson(response.getResponse(), new TypeToken<Set<UUID>>() {}.getType());
        } else Bukkit.getLogger().warning("ProfileHandler - Could not get totp-enabled users from API: " + response.getErrorMessage());
    }

    public void remove(UUID player) {
        this.profiles.remove(player);
    }

    public Optional<Profile> getProfile(UUID player) {
        return Optional.ofNullable(this.profiles.get(player));
    }

    public Profile loadProfile(UUID player, String name, String ip) {
        RequestResponse response = RequestHandler.post("/users/" + player.toString() + "/login", ImmutableMap.of("username", name, "userIp", ip));

        if (response.wasSuccessful()) {
            Profile profile = new Profile(player, response.asJSONObject());
            this.profiles.put(player, profile);
            return profile;
        }

        Bukkit.getLogger().warning("ProfileHandler - Could not load profile for " + player + ": " + response.getErrorMessage());
        return null;
    }

}