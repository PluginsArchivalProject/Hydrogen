package net.frozenorb.hydrogen.punishment.menu;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;
import net.frozenorb.hydrogen.punishment.Punishment;
import lombok.RequiredArgsConstructor;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.minecraft.util.com.google.gson.reflect.TypeToken;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.Menu;
import net.frozenorb.qlib.qLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MainPunishmentMenu extends Menu {

    private final String targetUUID;
    private final String targetName;

    public String getTitle(Player player) {
        return ChatColor.BLUE + "Punishments - " + this.targetName;
    }

    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();

        if (player.hasPermission("minehq.punishments.view.blacklist")) {
            buttons.put(1, this.button(Punishment.PunishmentType.WARN));
            buttons.put(3, this.button(Punishment.PunishmentType.MUTE));
            buttons.put(5, this.button(Punishment.PunishmentType.BAN));
            buttons.put(7, this.button(Punishment.PunishmentType.BLACKLIST));
        } else {
            buttons.put(1, this.button(Punishment.PunishmentType.WARN));
            buttons.put(4, this.button(Punishment.PunishmentType.MUTE));
            buttons.put(7, this.button(Punishment.PunishmentType.BAN));
        }

        return buttons;
    }

    private Button button(Punishment.PunishmentType type) {
        return new Button() {
            public String getName(Player player) {
                return ChatColor.RED + type.getName() + "s";
            }

            public List<String> getDescription(Player player) {
                return null;
            }

            public Material getMaterial(Player player) {
                return Material.WOOL;
            }

            public byte getDamageValue(Player player) {
                switch (type) {
                    case WARN:
                        return DyeColor.YELLOW.getWoolData();
                    case MUTE:
                        return DyeColor.ORANGE.getWoolData();
                    case BAN:
                        return DyeColor.RED.getWoolData();
                    default:
                        return DyeColor.BLACK.getWoolData();
                }
            }

            public void clicked(Player player, int i, ClickType clickType) {
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Loading " + MainPunishmentMenu.this.targetName + "'s " + type.getName() + "s...");

                Bukkit.getScheduler().scheduleAsyncDelayedTask(Hydrogen.getInstance(), () -> {
                    RequestResponse response = RequestHandler.get("/punishments", ImmutableMap.of("user", MainPunishmentMenu.this.targetUUID));
                    if (response.wasSuccessful()) {
                        List<Punishment> allPunishments = qLib.PLAIN_GSON.fromJson(response.getResponse(), new TypeToken<List<Punishment>>() {}.getType());
                        LinkedHashMap<Punishment, String> punishments = new LinkedHashMap<>();

                        allPunishments.sort((first, second) -> Longs.compare(second.getAddedAt(), first.getAddedAt()));
                        allPunishments.stream().filter(punishment -> punishment.getType() == type)
                                .forEach(punishment -> punishments.put(punishment, punishment.resolveAddedBy()));

                        Bukkit.getScheduler().scheduleSyncDelayedTask(Hydrogen.getInstance(), () ->
                                new PunishmentMenu(targetUUID, targetName, type, punishments).openMenu(player));
                    } else player.sendMessage(ChatColor.RED + response.getErrorMessage());
                });
            }
        };
    }

}
