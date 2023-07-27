package net.frozenorb.hydrogen.listener;

import net.frozenorb.hydrogen.Hydrogen;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

@RequiredArgsConstructor
public class PingListener implements Listener {

    private final Hydrogen hydrogen;

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        ServerPing response = event.getResponse();

        if (hydrogen.getMotdLines() != null)
            response.setDescription(hydrogen.getMotdLines());

        event.setResponse(response);
    }

}