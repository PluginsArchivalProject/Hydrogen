package net.frozenorb.hydrogen.punishment;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.profile.Profile;
import net.frozenorb.hydrogen.punishment.meta.PunishmentMetaFetcher;
import net.frozenorb.hydrogen.punishment.meta.defaults.HPunishmentMetaFetcher;
import net.frozenorb.qlib.qLib;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import okhttp3.Request.Builder;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.Map.Entry;

public class PunishmentHandler {

    private final List<Builder> requestQueue = Lists.newArrayList();
    private final List<PunishmentMetaFetcher> metaFetchers = Lists.newArrayList();

    public PunishmentHandler() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(Hydrogen.getInstance(), this::refresh, 0L, 6000L);
        this.registerMetaFetcher(new HPunishmentMetaFetcher());
    }

    private void refresh() {
        if (this.requestQueue.size() != 0) {
            try {
                int requestsSize = this.requestQueue.size();

                for (Builder builder : this.requestQueue) {
                    RequestResponse secondResponse = RequestHandler.send(builder);
                    if (!secondResponse.couldNotConnect())
                        this.requestQueue.remove(builder);
                }

                Bukkit.getLogger().info("PunishmentCache - Flushed " + requestsSize + " queued requests.");
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().warning("PunishmentCache - Could not flush requests: " + e.getMessage());
            }
        }
    }

    public RequestResponse punish(UUID player, UUID sender, Punishment.PunishmentType type, String publicReason, String privateReason, long expiresIn) {
        if ((type == Punishment.PunishmentType.BAN || type == Punishment.PunishmentType.BLACKLIST)
                && Bukkit.getPlayer(player) != null
                && Bukkit.getPlayer(player).hasMetadata("HAL-Recording"))
            return new RequestResponse(false, "This player cannot be banned at this time.", "", null);

        HashMap<String, Map<String, Object>> metaMap = Maps.newHashMap();

        for (PunishmentMetaFetcher response : this.metaFetchers)
            metaMap.put(response.getPlugin().getName(), this.fixMeta(response.fetch(player, type).getInfo()));

        HashMap<String, Object> body = Maps.newHashMap();
        body.put("user", player.toString());
        body.put("publicReason", publicReason);
        body.put("privateReason", privateReason);
        body.put("type", type.toString());

        if (expiresIn > 0L)
            body.put("expiresIn", expiresIn);

        if (Bukkit.getPlayer(sender) != null) {
            body.put("addedBy", sender.toString());
            body.put("addedByIp", Bukkit.getPlayer(sender).getAddress().getAddress().getHostAddress());
        }

        if (Bukkit.getPlayer(player) != null) {
            String response = Bukkit.getPlayer(player).getAddress().getAddress().getHostAddress();
            body.put("userIp", response);
        }

        body.put("metadata", metaMap);

        RequestResponse response = RequestHandler.post("/punishments", body);
        if (!response.wasSuccessful())
            Bukkit.getLogger().warning("PunishmentHandler - Could not punish " + player + " (" + type.getName() + "): " + response.getErrorMessage());

        if (type == Punishment.PunishmentType.MUTE) {
            Optional<Profile> profileOptional = Hydrogen.getInstance().getProfileHandler().getProfile(player);
            profileOptional.ifPresent(profile -> profile.setMute(qLib.PLAIN_GSON.fromJson(response.asJSONObject().toString(), Punishment.class)));
        }

        if (response.couldNotConnect())
            this.requestQueue.add(response.getRequestBuilder());

        return response;
    }

    public RequestResponse pardon(UUID player, UUID sender, Punishment.PunishmentType type, String reason) {
        HashMap<String, Object> queryArgs = Maps.newHashMap();
        queryArgs.put("type", type.toString());
        queryArgs.put("reason", reason);

        if (Bukkit.getPlayer(sender) != null) {
            queryArgs.put("removedBy", sender.toString());
            queryArgs.put("removedByIp", Bukkit.getPlayer(sender).getAddress().getAddress().getHostAddress());
        }

        RequestResponse response = RequestHandler.delete("/users/" + player.toString() + "/activePunishment", queryArgs);
        if (!response.wasSuccessful())
            Bukkit.getLogger().warning("PunishmentHandler - Could not pardon " + player + " (" + type.getName() + "): " + response.getErrorMessage());
        else if (type == Punishment.PunishmentType.MUTE) {
            Optional<Profile> profileOptional = Hydrogen.getInstance().getProfileHandler().getProfile(player);
            profileOptional.ifPresent(profile -> profile.setMute(null));
        }

        if (response.couldNotConnect())
            this.requestQueue.add(response.getRequestBuilder());

        return response;
    }

    public void registerMetaFetcher(PunishmentMetaFetcher fetcher) {
        this.metaFetchers.add(fetcher);
    }

    private Map<String, Object> fixMeta(Map<String, Object> oldMap) {
        HashMap<String, Object> newMap = Maps.newHashMap();
        if (oldMap == null)
            return newMap;

        for (Entry<String, Object> entry : oldMap.entrySet())
            newMap.put(entry.getKey(), this.normalise(entry.getValue()));

        return newMap;
    }

    private Object normalise(Object otherObject) {
        if (otherObject instanceof Collection) {
            List<Object> newMap = Lists.newArrayList();

            for (Object entry : (Collection<?>) otherObject)
                newMap.add(this.normalise(entry));

            return newMap;
        }

        if (!(otherObject instanceof Map))
            return String.valueOf(otherObject);

        Map<Object, Object> newMap = Maps.newHashMap();
        Map<Object, Object> otherMap = (Map<Object, Object>) otherObject;

        for (Entry<Object, Object> entry : otherMap.entrySet())
            newMap.put(this.normalise(entry.getKey()), this.normalise(entry.getValue()));

        return newMap;
    }
}
