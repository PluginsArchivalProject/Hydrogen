package net.frozenorb.hydrogen.totp.prompt;

import com.google.common.collect.ImmutableMap;
import net.frozenorb.hydrogen.Hydrogen;
import net.frozenorb.hydrogen.Settings;
import net.frozenorb.hydrogen.connection.RequestHandler;
import net.frozenorb.hydrogen.connection.RequestResponse;
import net.frozenorb.hydrogen.totp.TotpMapCreator;
import net.minecraft.util.com.google.common.escape.Escaper;
import net.minecraft.util.com.google.common.net.UrlEscapers;
import net.minecraft.util.org.apache.commons.codec.binary.Base32;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class ScanMapPrompt extends StringPrompt {

    private static String totpImageUrlFormat = "https://www.google.com/chart?chs=130x130&chld=M%%7C0&cht=qr&chl=%s";
    private static TotpMapCreator totpMapCreator = new TotpMapCreator();
    private static Base32 base32Encoder = new Base32();
    private static SecureRandom secureRandom;
    int failures = 0;

    public String getPromptText(ConversationContext context) {
        Player player = (Player)context.getForWhom();
        if(this.failures == 0) {
            Bukkit.getScheduler().runTaskAsynchronously(Hydrogen.getInstance(), () -> {
                String totpSecret = generateTotpSecret();
                BufferedImage totpImage = createTotpImage(player, totpSecret);

                Bukkit.getScheduler().runTask(Hydrogen.getInstance(), () -> {
                    ItemStack map = totpMapCreator.createMap(player, totpImage);
                    context.setSessionData("totpSecret", totpSecret);
                    context.setSessionData("totpMap", map);
                    player.getInventory().addItem(map);
                });
            });
        }

        return ChatColor.RED + "On your 2FA device, scan the map given to you. Once you've scanned the map, type the code displayed on your device in chat.";
    }

    public Prompt acceptInput(ConversationContext context, String s) {
        Player player = (Player)context.getForWhom();
        ItemStack totpMap = (ItemStack)context.getSessionData("totpMap");
        String totpSecret = (String)context.getSessionData("totpSecret");

        player.getInventory().remove(totpMap);
        Settings settings = Hydrogen.getInstance().getSettings();

        int totpCode;
        try {
            totpCode = Integer.parseInt(s.replaceAll(" ", ""));
        } catch (NumberFormatException var8) {
            if(this.failures++ >= 3) {
                context.getForWhom().sendRawMessage(ChatColor.RED + "Cancelling 2FA setup due to too many incorrect codes.");
                context.getForWhom().sendRawMessage(ChatColor.RED + "Contact the " + settings.getNetworkName() + " staff team for any questions you have about 2FA.");
                return END_OF_CONVERSATION;
            }

            context.getForWhom().sendRawMessage("");
            context.getForWhom().sendRawMessage(ChatColor.RED + s + " isn't a valid totp code. Let's try that again.");
            return this;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Hydrogen.getInstance(), () -> {
            RequestResponse response = RequestHandler.post("/users/" + player.getUniqueId() + "/setupTotp", ImmutableMap.of("userIp", player.getAddress().getAddress().getHostAddress(), "secret", totpSecret, "totpCode", Integer.valueOf(totpCode)));
            if(response.wasSuccessful()) {
                player.sendMessage(ChatColor.GREEN + "2FA setup completed successfully.");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to setup 2FA. " + response.getErrorMessage());
            }
        });

        return END_OF_CONVERSATION;
    }

    private static String generateTotpSecret() {
        byte[] secretKey = new byte[10];
        secureRandom.nextBytes(secretKey);
        return base32Encoder.encodeToString(secretKey);
    }

    private static BufferedImage createTotpImage(Player player, String totpSecret) {
        Settings settings = Hydrogen.getInstance().getSettings();
        Escaper urlEscaper = UrlEscapers.urlFragmentEscaper();
        String totpUrl = "otpauth://totp/" + urlEscaper.escape(player.getName()) + "?secret=" + totpSecret + "&issuer=" + urlEscaper.escape(settings.getNetworkName() + " Network");
        String totpImageUrl = String.format("https://www.google.com/chart?chs=130x130&chld=M%%7C0&cht=qr&chl=%s", new Object[]{URLEncoder.encode(totpUrl)});

        try {
            return ImageIO.read(new URL(totpImageUrl));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (GeneralSecurityException ignored) {}
    }
}