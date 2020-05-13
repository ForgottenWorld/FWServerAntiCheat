package it.forgottenworld.fwserveranticheat;

import it.forgottenworld.fwanticheat.Config;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FWServerAntiCheat extends JavaPlugin {

    private final Logger logger = getLogger();

    @Override
    public void onEnable() {
        Config config = null;
        try {
            config = new Config("whitelist.yml", this);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
        PacketHandler packetHandler = new PacketHandler(config);
        Bukkit.getMessenger().registerIncomingPluginChannel(this,"fw:anticheat", packetHandler);
    }

}
