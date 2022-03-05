package cat.nyaa.ukit.utils;

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
}
