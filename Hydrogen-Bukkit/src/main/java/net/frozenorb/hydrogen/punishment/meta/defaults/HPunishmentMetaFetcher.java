package net.frozenorb.hydrogen.punishment.meta.defaults;

import com.google.common.collect.Maps;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.listener.BanMetaListener;
import net.frozenorb.hydrogen.punishment.meta.PunishmentMeta;
import net.frozenorb.hydrogen.punishment.meta.PunishmentMetaFetcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class HPunishmentMetaFetcher extends PunishmentMetaFetcher {

    public HPunishmentMetaFetcher() {
        super(Hydrogen.getInstance());
    }

    @Override
    public PunishmentMeta fetch(UUID target) {
        Player player = Bukkit.getPlayer(target);
        Map<String, Object> map = Maps.newHashMap();

        if (BanMetaListener.getMessages().containsKey(target))
            map.put("chatMessages", BanMetaListener.getMessages().get(target));

        if (BanMetaListener.getJoinTime().containsKey(target))
            map.put("secondsOnline", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - BanMetaListener.getJoinTime().get(target)));

        if (player != null) {
            map.put("server", Bukkit.getServerName());
            map.put("location", player.getLocation());
            map.put("ip", player.getAddress().getAddress().getHostAddress());
        }

        return PunishmentMeta.of(map);
    }

}
