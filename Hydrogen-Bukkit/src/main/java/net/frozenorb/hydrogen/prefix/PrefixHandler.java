package net.frozenorb.hydrogen.prefix;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.qlib.qLib;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.util.com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PrefixHandler {

    private List<Prefix> prefixes = Lists.newArrayList();
    private Map<String, Prefix> prefixCache;

    public PrefixHandler() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(Hydrogen.getInstance(), this::refresh, 0L, 1200L);
    }

    public void refresh() {
        RequestResponse response = RequestHandler.get("/prefixes");
        if (response.wasSuccessful()) {
            this.prefixes = qLib.PLAIN_GSON.fromJson(response.getResponse(), new TypeToken<List<Prefix>>() {}.getType());

            Map<String, Prefix> newPrefixCache = Maps.newHashMap();
            for (Prefix prefix : this.prefixes)
                newPrefixCache.put(prefix.getId(), prefix);

            this.prefixCache = newPrefixCache;
        } else Bukkit.getLogger().warning("PrefixHandler - Could not retrieve prefixes from API: " + response.getErrorMessage());
    }

    public Optional<Prefix> getPrefix(String parse) {
        return Optional.ofNullable(this.prefixCache.get(parse));
    }

    public List<Prefix> getPrefixes() {
        return new ArrayList<>(this.prefixes);
    }

}