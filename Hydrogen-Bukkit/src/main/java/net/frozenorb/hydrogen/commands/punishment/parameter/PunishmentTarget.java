package net.frozenorb.hydrogen.commands.punishment.parameter;

import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.qlib.command.ParameterType;
import net.frozenorb.qlib.util.Callback;
import net.frozenorb.qlib.util.UUIDUtils;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.*;
import java.util.regex.Pattern;

public class PunishmentTarget {
    private static Pattern pattern = Pattern.compile("(?i)^[0-9a-f]{8}-?[0-9a-f]{4}-?[0-5][0-9a-f]{3}-?[089ab][0-9a-f]{3}-?[0-9a-f]{12}$");
    private static List<String> recentlyDisconnected = new ArrayList();
    private String name;

    private static boolean isValidUuid(String uuid) {
        return uuid != null && uuid.trim().length() > 31?pattern.matcher(uuid).matches():false;
    }

    public void resolveUUID(Callback<UUID> callback) {
        UUID uuid = UUIDUtils.uuid(this.name);
        if(uuid != null) {
            callback.callback(uuid);
        } else if(isValidUuid(this.name)) {
            callback.callback(UUID.fromString(this.name));
        } else {
            Iterator var3 = Bukkit.getOnlinePlayers().iterator();

            Player player;
            do {
                if(!var3.hasNext()) {
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(Hydrogen.getInstance(), () -> {
                        Builder builder = new Builder();
                        builder.get();
                        builder.url("https://api.mojang.com/users/profiles/minecraft/" + PunishmentTarget.this.name);

                        try {
                            Response e = Hydrogen.getInstance().getOkHttpClient().newCall(builder.build()).execute();
                            String body = e.body().string();
                            if(body.isEmpty()) {
                                callback.callback(null);
                                return;
                            }

                            JSONObject json = new JSONObject(new JSONTokener(body));
                            PunishmentTarget.this.name = json.getString("name");
                            callback.callback(UUID.fromString(PunishmentTarget.this.dash(json.getString("id"))));
                        } catch (Exception var6) {
                            var6.printStackTrace();
                            callback.callback(null);
                        }

                    });
                    return;
                }

                player = (Player)var3.next();
            } while(!player.getName().equalsIgnoreCase(this.name));

            this.name = player.getName();
            callback.callback(player.getUniqueId());
        }
    }

    private String dash(String uuid) {
        StringBuffer sb = new StringBuffer(uuid);
        sb.insert(8, "-");
        sb = new StringBuffer(sb.toString());
        sb.insert(13, "-");
        sb = new StringBuffer(sb.toString());
        sb.insert(18, "-");
        sb = new StringBuffer(sb.toString());
        sb.insert(23, "-");
        return sb.toString();
    }

    public static class Type implements ParameterType<PunishmentTarget>
    {
        public PunishmentTarget transform(CommandSender sender, String source) {
            if (source.equals("self")) {
                source = sender.getName();
            }
            return new PunishmentTarget(source);
        }

        public List<String> tabComplete(Player sender, Set<String> flags, String source) {
            List<String> completions = new ArrayList<String>();
            Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
            completions.addAll(PunishmentTarget.recentlyDisconnected);
            return completions;
        }
    }

    public PunishmentTarget(String name) {
        this.name = name;
    }

    public static List<String> getRecentlyDisconnected() {
        return recentlyDisconnected;
    }

    public String getName() {
        return this.name;
    }
}
