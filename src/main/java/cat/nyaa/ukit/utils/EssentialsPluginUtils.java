package cat.nyaa.ukit.utils;

import net.ess3.api.IEssentials;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class EssentialsPluginUtils {

    static {
        setup();
    }

    private static boolean enabled;
    private static IEssentials essentials;


    public static void setup() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (plugin == null) {
            enabled = false;
        } else {
            essentials = (IEssentials) plugin;
            enabled = true;
        }
    }

    public static String getPlayerNickName(UUID uniqueID) {
        if (!enabled) {
            return null;
        }
        var user = essentials.getUser(uniqueID);
        return user.getNickname();
    }

    public static boolean hasNick(UUID uniqueID) {
        if (!enabled) {
            return false;
        }
        return essentials.getUser(uniqueID).getNickname() != null;
    }

    public static boolean isEnabled() {
        return enabled;
    }


}
