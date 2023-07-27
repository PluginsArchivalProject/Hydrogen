package net.frozenorb.hydrogen.runnable;

import net.frozenorb.hydrogen.Hydrogen;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;

@RequiredArgsConstructor
public class MOTDRunnable implements Runnable {

    private final Hydrogen hydrogen;

    @Override
    public void run() {
        Jedis jedis = hydrogen.getJedisPool().getResource();

        String motd = jedis.get("BungeeCordMOTD");
        if (motd == null)
            return;

        hydrogen.setMotdLines(motd);
        jedis.close();
    }

}