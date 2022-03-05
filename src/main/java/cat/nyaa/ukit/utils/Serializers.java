package cat.nyaa.ukit.utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public class Serializers {
    public static String itemStackToItemJson(ItemStack itemStack) {
        return "{\"type\":\"minecraft:" + itemStack.getType().name().toLowerCase() + "\",\"Damage\":0,\"Count\":1,\"tag\":{\"display\":{\"Name\":\"" + itemStack.getItemMeta().getDisplayName() + "\",\"Lore\":[\"" + itemStack.getItemMeta().getLore().toString().replace("[", "").replace("]", "") + "\"]}}}";
    }

    /**
     * https://github.com/NyaaCat/NyaaCore/blob/ad176c3fa5533808fcaa2a391bf3ed9ef5652471/src/main/java/cat/nyaa/nyaacore/Message.java
     * Convert a Player to its JSON representation to be shown in a hover event
     */
    public static String getPlayerJson(OfflinePlayer player) {
        return "{name:\"{\\\"text\\\":\\\"" + player.getName() + "\\\"}\",id:\"" + player.getUniqueId() + "\",type:\"minecraft:player\"}";
    }
}
