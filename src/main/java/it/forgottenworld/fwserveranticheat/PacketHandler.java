package it.forgottenworld.fwserveranticheat;

import it.forgottenworld.fwanticheat.ClientInfoPacket;
import it.forgottenworld.fwanticheat.Config;
import it.forgottenworld.fwanticheat.InspectionResult;
import it.forgottenworld.fwanticheat.SerializationUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class PacketHandler implements PluginMessageListener {

    private final Config whitelistConfig;
    private final Config config;
    private final FWServerAntiCheat instance;

    public PacketHandler(FWServerAntiCheat instance, Config config, Config whitelistConfig) {
        this.instance = instance;
        this.config = config;
        this.whitelistConfig = whitelistConfig;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        try{
            ClientInfoPacket clientInfoPacket = parsePacket(message);
            instance.getPlayerSet().remove(player);
            if(clientInfoPacket.getModsInspectionResult() != InspectionResult.NORMAL || clientInfoPacket.getTextureInspectionResult() != InspectionResult.NORMAL){
                player.kickPlayer(evaluateInspectionResult(clientInfoPacket.getModsInspectionResult()) + "\n" + evaluateInspectionResult(clientInfoPacket.getTextureInspectionResult()));
                instance.log(Level.WARNING, " " + player.getName() + " kicked for reason: " + clientInfoPacket.getModsInspectionMessage());
            }else{
                String checksumEvaluationResult = evaluateChecksums(clientInfoPacket.getModsChecksumMap(), clientInfoPacket.getTextureChecksumMap());
                if(!checksumEvaluationResult.equals("")){
                    player.kickPlayer(checksumEvaluationResult);
                    instance.log(Level.WARNING, " " + player.getName() + " kicked for reason: \n" + checksumEvaluationResult);
                }
            }
        }catch (IOException | ClassNotFoundException | ConcurrentModificationException e ){
            e.printStackTrace();
            player.kickPlayer(ChatColor.DARK_RED + "Impossibile verificare la presenza dell'anticheat\nProva a riloggare\nSe l'errore persiste contatta lo Staff");
            instance.log(Level.SEVERE, " " + player.getName() + " kicked for: " + e.getMessage());
        }
    }

    private ClientInfoPacket parsePacket(byte[] message) throws IOException, ClassNotFoundException {
        ClientInfoPacket clientInfoPacket;
        byte[] packet = Arrays.copyOfRange(message,1,message.length);
        clientInfoPacket = (ClientInfoPacket) SerializationUtils.deserialize(packet);
        return clientInfoPacket;
    }

    private String evaluateInspectionResult(InspectionResult result){
        switch (result){
            case FILE_NOT_FOUND:
                return "File della mod eliminato";
            case IO_EXCEPTION:
                return "Impossibile reperire le informazioni sulle mod";
            case HASH_FUNCTION_NOT_FOUND:
                return "Algoritmo di hash non trovato";
            case INVALID_TEXTURE_FORMAT:
                return "Formato di una texture non valido";
            default:
                return "";
        }
    }

    private String evaluateChecksums(Map<String, String> modsChecksum, Map<String, String> textureChecksum){
        StringBuilder stringBuilder = new StringBuilder();
        ConfigurationSection whitelistedMods = whitelistConfig.getConfig().getConfigurationSection("whitelisted-mods");
        for(Map.Entry<String, String> entry : modsChecksum.entrySet()){
            assert whitelistedMods != null;
            String modName = entry.getKey();
            if(modName.equals("minecraft") && !config.getConfig().getBoolean("inspect-minecraft")){
                continue;
            }
            if(modName.equals("forge") && !config.getConfig().getBoolean("inspect-forge")){
                continue;
            }
            if(whitelistedMods.contains(entry.getKey())){
                if(!Objects.equals(whitelistedMods.getString(entry.getKey()), entry.getValue())){
                    stringBuilder.append("Mod alterata: ").append(entry.getKey()).append("\n");
                }
            }else{
                stringBuilder.append("Mod non consentita: ").append(entry.getKey()).append("\n");
            }
        }
        ConfigurationSection whitelistedTextures = whitelistConfig.getConfig().getConfigurationSection("whitelisted-textures");
        for(Map.Entry<String, String> entry : textureChecksum.entrySet()){
            assert whitelistedTextures != null;
            if(whitelistedTextures.contains(entry.getKey())){
                if(!Objects.equals(whitelistedTextures.getString(entry.getKey()), entry.getValue())){
                    stringBuilder.append("Texture alterata: ").append(entry.getKey()).append("\n");
                }
            }else{
                stringBuilder.append("Texture non consentita: ").append(entry.getKey()).append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
