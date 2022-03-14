package cat.nyaa.ukit.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public class Utils {
    public static int getReachDistance(Player player) {
        return player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE ? 5 : 6;
    }

    public static Block getBlockLookingAt(Player player) {
        return player.getTargetBlock(null, getReachDistance(player));
    }

    public static Entity getEntityLookingAt(Player player, Predicate<Entity> predicate) {
        var trace = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getLocation().getDirection(), getReachDistance(player), 0, predicate);
        if (trace == null) return null;
        return trace.getHitEntity();
    }
    public static void silentBroadcast(String text){
        Bukkit.getOnlinePlayers().forEach(
               p-> p.sendMessage(text)
        );
    }

    public static void silentBroadcast(BaseComponent baseComponent){
        silentBroadcast(new BaseComponent[]{baseComponent});
    }

    public static void silentBroadcast(BaseComponent[] baseComponents){
        Bukkit.getOnlinePlayers().forEach(
                p -> p.spigot().sendMessage(baseComponents)
        );
    }
}
