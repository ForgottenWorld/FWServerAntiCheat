package it.forgottenworld.fwserveranticheat;

import it.forgottenworld.fwanticheat.Config;
import it.forgottenworld.fwserveranticheat.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FWServerAntiCheat extends JavaPlugin {

    private final Logger logger = getLogger();
    private final Set<Player> playerSet = new HashSet<>();
    private FileLogger fileLogger;

    @Override
    public void onEnable() {
        Config whitelistConfig = null;
        Config config = null;
        try {
            fileLogger = new FileLogger(new File(getDataFolder(), "logs.txt"));
            whitelistConfig = new Config("whitelist.yml", this);
            config = new Config("config.yml", this);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
        PacketHandler packetHandler = new PacketHandler(this, config, whitelistConfig);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "fw:anticheat", packetHandler);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this, config), this);
    }

    @Override
    public void onDisable(){
        fileLogger.close();
    }

    public Set<Player> getPlayerSet() {
        return playerSet;
    }

    public void log(Level level, String message) {
        logger.log(level, message);
        message = "[" + new Timestamp(System.currentTimeMillis()).toString() + "]" + message;
        fileLogger.log(message);
    }
}
