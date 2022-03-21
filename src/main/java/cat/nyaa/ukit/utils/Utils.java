package cat.nyaa.ukit.utils;

import land.melon.lab.simplelanguageloader.utils.Pair;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

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

    public static void silentBroadcast(String text) {
        Bukkit.getOnlinePlayers().forEach(
                p -> p.sendMessage(text)
        );
    }

    public static void silentBroadcast(BaseComponent baseComponent) {
        silentBroadcast(new BaseComponent[]{baseComponent});
    }

    public static void silentBroadcast(BaseComponent[] baseComponents) {
        Bukkit.getOnlinePlayers().forEach(
                p -> p.spigot().sendMessage(baseComponents)
        );
    }

    public static Pair<EquipmentSlot, ItemStack> getItemInHand(Player player) {
        var item = player.getInventory().getItemInMainHand();
        var isOffhand = false;
        if (item.getType().isAir()) {
            item = player.getInventory().getItemInOffHand();
            isOffhand = true;
        }
        if (item.getType().isAir())
            return null;
        else return Pair.of(isOffhand ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND, item);
    }

    public static Pair<EquipmentSlot, ItemStack> getItemInHand(Player player, Material type) {
        var item = player.getInventory().getItemInMainHand();
        var isOffhand = false;
        if (item.getType() != type) {
            item = player.getInventory().getItemInOffHand();
            isOffhand = true;
        }
        if (item.getType() != type)
            return null;
        else return Pair.of(isOffhand ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND, item);
    }

    public static void setItemInHand(Player player, Pair<EquipmentSlot, ItemStack> itemStackPair) {
        player.getInventory().setItem(itemStackPair.key(), itemStackPair.value());
    }
}
