package net.frozenorb.hydrogen.totp;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class TotpMapRenderer extends MapRenderer {

    private final UUID targetPlayer;
    private BufferedImage image;

    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        if (player.getUniqueId().equals(this.targetPlayer)) {
            mapCanvas.drawImage(0, 0, this.image);
            this.image = null;
        }
    }

    public TotpMapRenderer(UUID targetPlayer, BufferedImage image) {
        this.targetPlayer = targetPlayer;
        this.image = image;
    }

}