package net.frozenorb.hydrogen;

import net.frozenorb.hydrogen.listener.PingListener;
import net.frozenorb.hydrogen.runnable.MOTDRunnable;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

@Getter @Setter
public class Hydrogen extends Plugin {

    private JedisPool jedisPool;
    private String motdLines = "Undefined\nStill Undefined..";

    @Override
    public void onEnable() {
        this.jedisPool = new JedisPool("127.0.0.1", 6379);

        getProxy().getPluginManager().registerListener(this, new PingListener(this));
        getProxy().getScheduler().schedule(this, new MOTDRunnable(this), 50L, TimeUnit.SECONDS);
    }

}