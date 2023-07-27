package net.frozenorb.hydrogen;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;

@Getter @Setter
public class Settings {

    private final Hydrogen plugin;
    private String networkName;
    private String networkWebsite;
    private String apiHost;
    private String apiKey;
    private boolean forceStaffTotp;
    private boolean bannedJoinsEnabled;
    private boolean clean = false;

    public Settings(Hydrogen plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();

        FileConfiguration config = this.plugin.getConfig();
        this.networkName = config.getString("text.network-name");
        this.networkWebsite = config.getString("text.network-website");
        this.apiHost = config.getString("APIHost");
        this.apiKey = config.getString("APIKey");

        if (!this.getServerName().equals(this.apiKey))
            Bukkit.getLogger().info("Hydrogen API key & server name mismatch! Using server name.");

        this.forceStaffTotp = config.getBoolean("2FARequired");
        this.bannedJoinsEnabled = config.getBoolean("BannedJoinsEnabled");
    }

    public String getServerName() {
        return this.plugin.getServer().getServerName();
    }

    public void save() {
        FileConfiguration config = this.plugin.getConfig();
        config.set("APIHost", this.apiHost);
        config.set("APIKey", this.apiKey);
        config.set("2FARequired", this.forceStaffTotp);
        config.set("BannedJoinsEnabled", this.bannedJoinsEnabled);
        this.plugin.saveConfig();
    }

}