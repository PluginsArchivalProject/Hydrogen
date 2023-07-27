package net.frozenorb.hydrogen.rank;

import com.google.common.collect.Lists;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.qlib.qLib;
import net.minecraft.util.com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RankHandler {

    private List<Rank> ranks = Lists.newArrayList();

    public RankHandler() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(Hydrogen.getInstance(), this::refresh, 0L, 6000L);
    }

    public void refresh() {
        RequestResponse response = RequestHandler.get("/ranks");
        if (response.wasSuccessful()) {
            this.ranks = qLib.PLAIN_GSON.fromJson(response.getResponse(), new TypeToken<List<Rank>>() {}.getType());
            this.ranks.sort(Rank.DISPLAY_WEIGHT_COMPARATOR);
        } else Bukkit.getLogger().warning("RankHandler - Could not retrieve ranks from API: " + response.getErrorMessage());
    }

    public Optional<Rank> getRank(String parse) {
        for (Rank rank : this.ranks) {
            if (rank.getId().equalsIgnoreCase(parse))
                return Optional.of(rank);

            if (rank.getDisplayName().equalsIgnoreCase(parse))
                return Optional.of(rank);
        }

        return Optional.empty();
    }

    public List<Rank> getRanks() {
        return new ArrayList<>(this.ranks);
    }

}