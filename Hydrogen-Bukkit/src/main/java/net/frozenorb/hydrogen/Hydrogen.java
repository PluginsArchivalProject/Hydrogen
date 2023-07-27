package net.frozenorb.hydrogen;

import net.frozenorb.hydrogen.permission.PermissionHandler;
import net.frozenorb.hydrogen.prefix.PrefixHandler;
import net.frozenorb.hydrogen.profile.ProfileHandler;
import net.frozenorb.hydrogen.rank.RankHandler;
import net.frozenorb.hydrogen.server.ServerHandler;
import lombok.Getter;
import net.frozenorb.hydrogen.commands.punishment.parameter.PunishmentTarget;
import net.frozenorb.hydrogen.nametag.HNametagProvider;
import net.frozenorb.hydrogen.punishment.PunishmentHandler;
import net.frozenorb.qlib.command.FrozenCommandHandler;
import net.frozenorb.qlib.nametag.FrozenNametagHandler;
import net.frozenorb.qlib.util.ClassUtils;
import okhttp3.OkHttpClient;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

@Getter
public class Hydrogen extends JavaPlugin {

    @Getter
    private static Hydrogen instance;

    private OkHttpClient okHttpClient;
    private RankHandler rankHandler;
    private ServerHandler serverHandler;
    private ProfileHandler profileHandler;
    private PermissionHandler permissionHandler;
    private PunishmentHandler punishmentHandler;
    private PrefixHandler prefixHandler;
    private Settings settings;
    
    public void onEnable() {
        instance = this;

        this.settings = new Settings(this);
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(2L, TimeUnit.SECONDS)
                .writeTimeout(2L, TimeUnit.SECONDS)
                .readTimeout(2L, TimeUnit.SECONDS).build();

        this.setupHandlers();
        this.setupCommands();
        this.setupListeners();
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "MHQ-Queue");

        FrozenNametagHandler.registerProvider(new HNametagProvider());
    }
    
    public void onDisable() {
        instance = null;
    }
    
    private void setupHandlers() {
        this.rankHandler = new RankHandler();
        this.serverHandler = new ServerHandler();
        this.profileHandler = new ProfileHandler();
        this.permissionHandler = new PermissionHandler();
        this.punishmentHandler = new PunishmentHandler();
        this.prefixHandler = new PrefixHandler();
    }
    
    private void setupCommands() {
        FrozenCommandHandler.registerPackage(this, "net.frozenorb.hydrogen.commands");
        FrozenCommandHandler.registerParameterType(PunishmentTarget.class, new PunishmentTarget.Type());
    }
    
    private void setupListeners() {
        ClassUtils.getClassesInPackage(this, "net.frozenorb.hydrogen.listener").stream().filter(Listener.class::isAssignableFrom).forEach(clazz -> {
            try {
                Bukkit.getPluginManager().registerEvents((Listener) clazz.newInstance(), this);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

}
