package net.frozenorb.hydrogen.profile;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.Settings;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.prefix.Prefix;
import net.frozenorb.hydrogen.prefix.PrefixHandler;
import net.frozenorb.hydrogen.punishment.Punishment;
import net.frozenorb.hydrogen.rank.Rank;
import net.frozenorb.hydrogen.rank.RankHandler;
import net.frozenorb.hydrogen.util.PermissionUtils;
import lombok.Getter;
import lombok.Setter;
import net.frozenorb.qlib.nametag.FrozenNametagHandler;
import net.frozenorb.qlib.qLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

@Getter
@Setter
public class Profile {

    private UUID player;
    private List<Rank> ranks;
    private Map<String, List<Rank>> scopeRanks;
    private boolean accessAllowed;
    private String accessDenialReason;
    private Map<String, Boolean> permissions;
    private boolean totpRequired;
    private boolean authenticated;
    private boolean ipWhitelisted;
    private Punishment mute;
    private ChatColor iconColor;
    private ChatColor nameColor;
    private Set<Prefix> authorizedPrefixes;
    private Prefix activePrefix;
    private Rank bestGeneralRank;
    private Rank bestDisplayRank;

    public Profile(UUID uuid, JSONObject json) {
        this.update(uuid, json);
    }

    public void checkTotpLock(String ip) {
        ProfileHandler profileHandler = Hydrogen.getInstance().getProfileHandler();
        if (!profileHandler.getTotpEnabled().contains(this.player) && !this.getBestGeneralRank().isStaffRank())
            this.totpRequired = false;

        RequestResponse response = RequestHandler.get("/users/" + this.player.toString() + "/requiresTotp", ImmutableMap.of("userIp", ip));
        if (response.wasSuccessful()) {
            this.totpRequired = response.asJSONObject().getBoolean("required");
            this.ipWhitelisted = response.asJSONObject().getString("message").equals("NOT_REQUIRED_IP_PRE_AUTHORIZED");
        }
    }

    private void updatePermissions(Player player) {
        Hydrogen.getInstance().getPermissionHandler().update(player, this.permissions);
    }

    public void updatePlayer(Player player) {
        Hydrogen plugin = Hydrogen.getInstance();
        Settings settings = plugin.getSettings();

        if (!this.accessAllowed) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (this.accessDenialReason != null && this.accessDenialReason.contains("VPNs are not allowed")) {
                    player.kickPlayer(this.accessDenialReason);
                    return;
                }

                if (settings.isBannedJoinsEnabled()) {
                    String message = ChatColor.RED + "Banned players are only allowed to use /register.";
                    player.setMetadata("Locked", new FixedMetadataValue(plugin, message));
                } else player.kickPlayer(this.accessDenialReason);
            });

            return;
        }

        Rank bestDisplayRank = this.getBestDisplayRank();
        String gameColor = bestDisplayRank.getGameColor();

        if (bestDisplayRank.getId().equals("velt-plus"))
            gameColor = (nameColor == null ? ChatColor.WHITE : nameColor).toString() + ChatColor.BOLD;

        player.setDisplayName(gameColor + player.getName() + ChatColor.RESET);
        String gamePrefix = bestDisplayRank.getGamePrefix();

        if (bestDisplayRank.getId().equals("velt-plus"))
            gamePrefix = (iconColor == null ? ChatColor.GRAY : iconColor) + "\u2738" + gameColor + ChatColor.BOLD;

        if (this.activePrefix != null && !settings.isClean())
            gamePrefix = gamePrefix + ChatColor.translateAlternateColorCodes('&', this.activePrefix.getPrefix());

        player.setMetadata("HydrogenPrefix", new FixedMetadataValue(plugin, gamePrefix));
        FrozenNametagHandler.reloadPlayer(player);

        boolean totpRequired = false;
        boolean userTotpRequired = this.totpRequired;
        boolean staffTotpRequired = settings.isForceStaffTotp() && this.getBestGeneralRank().isStaffRank();

        String totpMessage = null;
        if ((userTotpRequired || staffTotpRequired) && !this.authenticated && !this.ipWhitelisted)
            totpRequired = true;

        if (totpRequired && !player.hasMetadata("ForceAuth")) {
            if (!userTotpRequired && staffTotpRequired) {
                totpMessage = ChatColor.RED + "Please set up your two-factor authentication using /2fasetup.";
            } else
                totpMessage = ChatColor.RED + "Please provide your two-factor code. Type \"/auth <code>\" to authenticate.";

            player.setMetadata("Locked", new FixedMetadataValue(plugin, totpMessage));
        } else player.removeMetadata("Locked", plugin);


        this.updatePermissions(player);
        String finalTotpMessage = totpMessage;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (finalTotpMessage != null)
                player.sendMessage(finalTotpMessage);
        }, 10L);
    }

    public void setActivePrefix(Prefix prefix) {
        this.activePrefix = prefix;
        Bukkit.getScheduler().runTaskAsynchronously(Hydrogen.getInstance(), () -> {
            Map<String, Object> meta = Maps.newHashMap();
            meta.put("prefix", prefix == null ? null : prefix.getId());

            RequestResponse response = RequestHandler.post("/users/" + Profile.this.player + "/prefix", meta);
            if (!response.wasSuccessful())
                Bukkit.getLogger().info(response.asJSONObject().toString());
        });
    }

    public void authenticated() {
        this.authenticated = true;
    }

    public boolean isMuted() {
        return this.mute != null;
    }

    public void update(UUID uuid, JSONObject json) {
        RankHandler rankHandler = Hydrogen.getInstance().getRankHandler();
        PrefixHandler prefixHandler = Hydrogen.getInstance().getPrefixHandler();

        this.player = uuid;
        this.ranks = new ArrayList<>();

        JSONArray ranks = json.getJSONArray("ranks");
        for (int bestGeneralRank = 0; bestGeneralRank < ranks.length(); ++bestGeneralRank) {
            Optional<Rank> bestDisplayRank = rankHandler.getRank(ranks.getString(bestGeneralRank));
            bestDisplayRank.ifPresent(this.ranks::add);
        }

        Rank rank = null;
        Iterator<Rank> permissionsIterator = this.ranks.iterator();

        while (true) {
            Rank permissions;
            do {
                if (!permissionsIterator.hasNext()) {
                    this.bestGeneralRank = rank;

                    Rank currentRank = null;
                    Iterator<Rank> prefixIterator = this.ranks.iterator();

                    while (true) {
                        Rank prefixes;
                        do {
                            if (!prefixIterator.hasNext()) {
                                this.bestDisplayRank = currentRank;
                                this.scopeRanks = new HashMap<>();
                                if (json.has("scopeRanks")) {
                                    json.getJSONObject("scopeRanks").toMap().forEach((scope, rawRanks) -> {
                                        List<Rank> parsedRanks = new ArrayList<>();

                                        for (String rawRank : (List<String>) rawRanks)
                                            rankHandler.getRank(rawRank).ifPresent(parsedRanks::add);

                                        this.scopeRanks.put(scope, parsedRanks);
                                    });
                                }

                                this.mute = json.has("mute") ? qLib.PLAIN_GSON.fromJson(json.get("mute").toString(), Punishment.class) : null;

                                if (json.has("access")) {
                                    this.accessAllowed = json.getJSONObject("access").optBoolean("allowed", true);
                                    this.accessDenialReason = json.getJSONObject("access").optString("message", "");
                                } else {
                                    this.accessAllowed = true;
                                    this.accessDenialReason = "";
                                }

                                Map<String, Boolean> permissionMap = Maps.newHashMap();

                                Rank authorizedPrefixes;
                                for (Iterator<Rank> rankIterator = this.ranks.iterator(); rankIterator.hasNext(); permissionMap = PermissionUtils.mergePermissions(permissionMap, Hydrogen.getInstance().getPermissionHandler().getPermissions(authorizedPrefixes)))
                                    authorizedPrefixes = rankIterator.next();

                                this.permissions = permissionMap;

                                this.iconColor = json.has("iconColor") ? ChatColor.valueOf(json.getString("iconColor")) : null;
                                this.nameColor = json.has("nameColor") ? ChatColor.valueOf(json.getString("nameColor")) : null;

                                if (json.has("prefixes")) {
                                    JSONArray prefixArray = json.getJSONArray("prefixes");
                                    Set<Prefix> toAdd = Sets.newHashSet();

                                    for (int i = 0; i < prefixArray.length(); ++i) {
                                        Optional<Prefix> prefix = prefixHandler.getPrefix(prefixArray.getString(i));
                                        prefix.ifPresent(toAdd::add);
                                    }

                                    this.authorizedPrefixes = toAdd;
                                } else this.authorizedPrefixes = ImmutableSet.of();

                                if (!json.has("activePrefix"))
                                    return;

                                prefixHandler.getPrefix(json.getString("activePrefix")).ifPresent(prefix -> {
                                    if (this.authorizedPrefixes.stream().anyMatch(ap -> ap.getId().equals(prefix.getId())))
                                        this.activePrefix = prefix;
                                });
                                return;
                            }

                            prefixes = prefixIterator.next();
                        } while (currentRank != null && prefixes.getDisplayWeight() < currentRank.getDisplayWeight());

                        currentRank = prefixes;
                    }
                }

                permissions = permissionsIterator.next();
            } while (rank != null && permissions.getGeneralWeight() < rank.getGeneralWeight());

            rank = permissions;
        }
    }

}