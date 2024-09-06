package cat.nyaa.ukit.utils;

import net.ess3.api.IEssentials;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.UUID;

public class EssentialsPluginUtils {

    static {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (plugin == null) {
            enabled = false;
        } else {
            essentials = (IEssentials) plugin;
            enabled = true;
        }
    }

    private static boolean enabled;
    private static IEssentials essentials;

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

    public static boolean isAFK(UUID uniqueID) {
        if (!enabled) {
            return false;
        }
        return essentials.getUser(uniqueID).isAfk();
    }

    public static Object nickWithHoverOrNormalName(UUID uniqueID) {
        return EssentialsPluginUtils.isEnabled() && EssentialsPluginUtils.hasNick(uniqueID) ?
                LegacyComponentSerializer.legacySection().deserialize(EssentialsPluginUtils.getPlayerNickName(uniqueID))
                        .hoverEvent(HoverEvent.showText(
                                Component.text(Objects.requireNonNullElse(Bukkit.getOfflinePlayer(uniqueID).getName(), "unknown"))
                        )) :
                Bukkit.getOfflinePlayer(uniqueID).getName();
    }

    public static boolean isEnabled() {
        return enabled;
    }

}
