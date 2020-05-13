package it.forgottenworld.fwserveranticheat.listener;

import it.forgottenworld.fwanticheat.Config;
import it.forgottenworld.fwserveranticheat.FWServerAntiCheat;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ConcurrentModificationException;
import java.util.logging.Level;

public class PlayerListener implements Listener {

    private final FWServerAntiCheat instance;
    private final Config config;

    public PlayerListener(FWServerAntiCheat instance, Config config) {
        this.instance = instance;
        this.config = config;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        final String kickMessage = "Impossibile verificare la presenza dell'anticheat";
        instance.getPlayerSet().add(event.getPlayer());
        Bukkit.getScheduler().runTaskLater(instance,() -> {
            try{
                if(instance.getPlayerSet().contains(event.getPlayer())){
                    instance.getPlayerSet().remove(event.getPlayer());
                    event.getPlayer().kickPlayer(kickMessage);
                    instance.log(Level.WARNING, " " + event.getPlayer().getName() + " kicked for: " + kickMessage);
                }
            }catch (ConcurrentModificationException e){
                event.getPlayer().kickPlayer(kickMessage);
                instance.log(Level.WARNING, " " + event.getPlayer().getName() + " kicked for: " + kickMessage);
            }
        },config.getConfig().getLong("delay-before-kick")*20L);
    }

}
