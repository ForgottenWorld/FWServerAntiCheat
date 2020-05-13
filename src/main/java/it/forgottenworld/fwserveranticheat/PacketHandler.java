package it.forgottenworld.fwserveranticheat;

import it.forgottenworld.fwanticheat.ClientInfoPacket;
import it.forgottenworld.fwanticheat.Config;
import it.forgottenworld.fwanticheat.InspectionResult;
import it.forgottenworld.fwanticheat.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PacketHandler implements PluginMessageListener {

    private final Logger logger = Bukkit.getLogger();
    private final Config config;

    public PacketHandler(Config config) {
        this.config = config;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        try{
            ClientInfoPacket clientInfoPacket = parsePacket(message);
            if(clientInfoPacket.getModsInspectionResult() != InspectionResult.NORMAL || clientInfoPacket.getTextureInspectionResult() != InspectionResult.NORMAL){
                player.kickPlayer(evaluateInspectionResult(clientInfoPacket.getModsInspectionResult()) + "\n" + evaluateInspectionResult(clientInfoPacket.getTextureInspectionResult()));
                logger.log(Level.WARNING, player.getName() + " kicked for reason: " + clientInfoPacket.getModsInspectionMessage());
            }else{
                String checksumEvaluationResult = evaluateChecksums(clientInfoPacket.getModsChecksumMap(), clientInfoPacket.getTextureChecksumMap());
                if(!checksumEvaluationResult.equals("")){
                    player.kickPlayer(checksumEvaluationResult);
                    logger.log(Level.WARNING, player.getName() + " kicket for reason: \n" + checksumEvaluationResult);
                }
            }
        }catch (IOException | ClassNotFoundException e ){
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage());
            player.kickPlayer(ChatColor.DARK_RED + "Impossibile verificare la presenza dell'anticheat\nProva a riloggare\nSe l'errore persiste contatta lo Staff");
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
        ConfigurationSection whitelistedMods = config.getConfig().getConfigurationSection("whitelisted-mods");
        for(Map.Entry<String, String> entry : modsChecksum.entrySet()){
            logger.info(entry.getValue());
            assert whitelistedMods != null;
            if(whitelistedMods.contains(entry.getKey())){
                if(!Objects.equals(whitelistedMods.getString(entry.getKey()), entry.getValue())){
                    stringBuilder.append("Mod alterata: ").append(entry.getKey()).append("\n");
                }
            }else{
                stringBuilder.append("Mod non consentita: ").append(entry.getKey()).append("\n");
            }
        }
        ConfigurationSection whitelistedTextures = config.getConfig().getConfigurationSection("whitelisted-textures");
        for(Map.Entry<String, String> entry : textureChecksum.entrySet()){
            logger.info(entry.getValue());
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
