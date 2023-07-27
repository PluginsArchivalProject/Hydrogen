package net.frozenorb.hydrogen.totp;

import net.frozenorb.hydrogen.Hydrogen;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

public class TotpMapCreator {

    public ItemStack createMap(Player player, BufferedImage bufferedImage) {
        MapView mapView = Hydrogen.getInstance().getServer().createMap(player.getWorld());

        mapView.getRenderers().forEach(mapView::removeRenderer);
        mapView.addRenderer(new TotpMapRenderer(player.getUniqueId(), bufferedImage));
        player.sendMap(mapView);

        return new ItemStack(Material.MAP, 0, mapView.getId());
    }

}
