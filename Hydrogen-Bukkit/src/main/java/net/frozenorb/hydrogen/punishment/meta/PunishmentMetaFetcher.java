package net.frozenorb.hydrogen.punishment.meta;

import net.frozenorb.hydrogen.punishment.Punishment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.*;

import java.util.*;

@Getter
@RequiredArgsConstructor
public abstract class PunishmentMetaFetcher {

    private final JavaPlugin plugin;

    public abstract PunishmentMeta fetch(UUID target);

    public PunishmentMeta fetch(UUID target, Punishment.PunishmentType type) {
        return this.fetch(target);
    }

}