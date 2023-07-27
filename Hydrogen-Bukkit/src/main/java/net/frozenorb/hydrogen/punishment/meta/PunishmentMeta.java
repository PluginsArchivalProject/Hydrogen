package net.frozenorb.hydrogen.punishment.meta;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class PunishmentMeta {

    private final Map<String, Object> info;

    public static PunishmentMeta of(Map<String, Object> info) {
        return new PunishmentMeta(info);
    }

}