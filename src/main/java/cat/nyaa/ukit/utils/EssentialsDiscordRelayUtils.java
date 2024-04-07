package cat.nyaa.ukit.utils;

import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discord.MessageType;
import org.bukkit.Bukkit;

public class EssentialsDiscordRelayUtils {

    private static final DiscordService discordService;
    private static boolean enabled;

    static {
        enabled = Bukkit.getPluginManager().isPluginEnabled("EssentialsDiscord");
        if (enabled) {
            discordService = Bukkit.getServicesManager().load(DiscordService.class);
        } else {
            discordService = null;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void broadCastMessageToChat(String message) {
        if (enabled) {
            discordService.sendMessage(MessageType.DefaultTypes.CHAT, message, false);
        }
    }

}
