package net.frozenorb.hydrogen.commands;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.profile.Profile;
import net.frozenorb.hydrogen.rank.Rank;
import lombok.RequiredArgsConstructor;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.Menu;
import net.frozenorb.qlib.menu.menus.ConfirmMenu;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ColorsCommand {

    private static final Button BLACK_GLASS_PANE = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 15, " ");
    private static final ColorsCommand INSTANCE = new ColorsCommand();
    private static final byte WHITE = 0;
    private static final byte ORANGE = 1;
    private static final byte LIGHT_BLUE = 3;
    private static final byte YELLOW = 4;
    private static final byte LIME = 5;
    private static final byte PINK = 6;
    private static final byte GRAY = 7;
    private static final byte LIGHT_GRAY = 8;
    private static final byte CYAN = 9;
    private static final byte PURPLE = 10;
    private static final byte BLUE = 11;
    private static final byte GREEN = 13;
    private static final byte RED = 14;

    @Command(names = {"setcolors", "colors", "colours", "setcolours", "color", "colour", "setcolor", "setcolour"}, permission = "hydrogen.colors")
    public static void colors(Player sender) {
        INSTANCE.new IconColorMenu().openMenu(sender);
    }

    private Map<Integer, Button> fillMapWithPanes(Map<Integer, Button> map) {
        for (int i = 0; i < 36; ++i)
            map.putIfAbsent(i, BLACK_GLASS_PANE);

        return map;
    }

    private Button getIconButton(Player player, ChatColor chatColor, byte woolColor) {
        return new Button() {
            public String getName(Player player) {
                return chatColor.toString() + WordUtils.capitalizeFully(chatColor.name().toLowerCase().replace('_', ' '));
            }

            public List<String> getDescription(Player player) {
                return Lists.newArrayList();
            }

            public Material getMaterial(Player player) {
                return Material.WOOL;
            }

            public void clicked(Player player, int slot, ClickType clickType) {
                new NameColorMenu(chatColor).openMenu(player);
            }

            public byte getDamageValue(Player player) {
                return woolColor;
            }
        };
    }

    private Button getNameButton(Player player, ChatColor iconColor, ChatColor nameColor, byte woolColor) {
        return new Button() {
            public String getName(Player player) {
                return nameColor.toString() + WordUtils.capitalizeFully(nameColor.name().toLowerCase().replace('_', ' '));
            }

            public List<String> getDescription(Player player) {
                return Lists.newArrayList();
            }

            public Material getMaterial(Player player) {
                return Material.WOOL;
            }

            public void clicked(Player player, int slot, ClickType clickType) {
                new ConfirmMenu("Confirm", data -> {
                    if (data) {
                        setColors(player, iconColor, nameColor);
                        player.sendMessage(ChatColor.GREEN + "Updated icon & name colors.");
                    } else player.sendMessage(ChatColor.RED + "Icon & name color change aborted.");
                }).openMenu(player);
            }

            public byte getDamageValue(Player player) {
                return woolColor;
            }
        };
    }

    private void setColors(Player player, ChatColor iconColor, ChatColor nameColor) {
        Hydrogen plugin = Hydrogen.getInstance();
        Optional<Profile> profileOptional = plugin.getProfileHandler().getProfile(player.getUniqueId());

        if (!profileOptional.isPresent())
            return;

        Profile profile = profileOptional.get();
        Rank bestDisplayRank = profile.getBestDisplayRank();
        String gameColor = bestDisplayRank.getGameColor();

        profile.setIconColor(iconColor);
        profile.setNameColor(nameColor);

        if (bestDisplayRank.getId().equals("velt-plus"))
            gameColor = Objects.firstNonNull(nameColor, ChatColor.WHITE).toString() + ChatColor.BOLD;

        player.setDisplayName(gameColor + player.getName() + ChatColor.RESET);
        String gamePrefix = bestDisplayRank.getGamePrefix();
        if (bestDisplayRank.getId().equals("velt-plus"))
            gamePrefix = Objects.firstNonNull(iconColor, ChatColor.GRAY).toString() + "\u2738" + gameColor + ChatColor.BOLD;

        if (profile.getActivePrefix() != null && !plugin.getSettings().isClean())
            gamePrefix += ChatColor.translateAlternateColorCodes('&', profile.getActivePrefix().getPrefix());

        player.setMetadata("HydrogenPrefix", new FixedMetadataValue(plugin, gamePrefix));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> RequestHandler.post("/users/" + player.getUniqueId().toString() + "/colors", ImmutableMap.of("iconColor", iconColor.name(), "nameColor", nameColor.name())));
    }

    public class IconColorMenu extends Menu {
        public String getTitle(Player player) {
            return "Choose your icon color";
        }

        public Map<Integer, Button> getButtons(Player player) {
            Map<Integer, Button> buttons = Maps.newHashMap();
            buttons.put(10, getIconButton(player, ChatColor.WHITE, WHITE));
            buttons.put(11, getIconButton(player, ChatColor.GOLD, ORANGE));
            buttons.put(12, getIconButton(player, ChatColor.LIGHT_PURPLE, PINK));
            buttons.put(13, getIconButton(player, ChatColor.AQUA, LIGHT_BLUE));
            buttons.put(14, getIconButton(player, ChatColor.YELLOW, YELLOW));
            buttons.put(15, getIconButton(player, ChatColor.GREEN, LIME));
            buttons.put(16, getIconButton(player, ChatColor.DARK_PURPLE, PURPLE));
            buttons.put(19, getIconButton(player, ChatColor.DARK_GRAY, GRAY));
            buttons.put(20, getIconButton(player, ChatColor.GRAY, LIGHT_GRAY));
            buttons.put(21, getIconButton(player, ChatColor.DARK_AQUA, CYAN));
            buttons.put(22, getIconButton(player, ChatColor.DARK_BLUE, BLUE));
            buttons.put(23, getIconButton(player, ChatColor.RED, RED));
            buttons.put(24, getIconButton(player, ChatColor.DARK_RED, RED));
            buttons.put(25, getIconButton(player, ChatColor.DARK_GREEN, GREEN));
            return fillMapWithPanes(buttons);
        }

        public int size(Map<Integer, Button> buttons) {
            return 36;
        }
    }

    @RequiredArgsConstructor
    private class NameColorMenu extends Menu {
        private final ChatColor iconColor;

        public String getTitle(Player player) {
            return "Choose your name color";
        }

        public Map<Integer, Button> getButtons(Player player) {
            Map<Integer, Button> buttons = Maps.newHashMap();
            buttons.put(10, getNameButton(player, this.iconColor, ChatColor.WHITE, WHITE));
            buttons.put(11, getNameButton(player, this.iconColor, ChatColor.GOLD, ORANGE));
            buttons.put(12, getNameButton(player, this.iconColor, ChatColor.LIGHT_PURPLE, PINK));
            buttons.put(13, getNameButton(player, this.iconColor, ChatColor.AQUA, LIGHT_BLUE));
            buttons.put(14, getNameButton(player, this.iconColor, ChatColor.YELLOW, YELLOW));
            buttons.put(15, getNameButton(player, this.iconColor, ChatColor.GREEN, LIME));
            buttons.put(16, getNameButton(player, this.iconColor, ChatColor.DARK_PURPLE, PURPLE));
            buttons.put(19, getNameButton(player, this.iconColor, ChatColor.DARK_GRAY, GRAY));
            buttons.put(20, getNameButton(player, this.iconColor, ChatColor.GRAY, LIGHT_GRAY));
            buttons.put(21, getNameButton(player, this.iconColor, ChatColor.DARK_AQUA, CYAN));
            buttons.put(22, getNameButton(player, this.iconColor, ChatColor.DARK_BLUE, BLUE));
            buttons.put(23, getNameButton(player, this.iconColor, ChatColor.RED, RED));
            buttons.put(24, getNameButton(player, this.iconColor, ChatColor.DARK_RED, RED));
            buttons.put(25, getNameButton(player, this.iconColor, ChatColor.DARK_GREEN, GREEN));
            return fillMapWithPanes(buttons);
        }

        public int size(Map<Integer, Button> buttons) {
            return 36;
        }
    }
}
