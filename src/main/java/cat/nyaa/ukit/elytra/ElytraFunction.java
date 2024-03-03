package cat.nyaa.ukit.elytra;

import cat.nyaa.ukit.SpigotLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElytraFunction implements Listener {
    private final SpigotLoader pluginInstance;
    private final Material holdingItemType;

    private final Map<UUID, Integer> fireworkSlotCache = new HashMap<>();

    private final Map<UUID, Firework> fireworkBind = new HashMap<>();

    public ElytraFunction(SpigotLoader pluginInstance) {
        this.pluginInstance = pluginInstance;
        holdingItemType = Material.valueOf(pluginInstance.config.elytraConfig.holdingItem);
    }

    @EventHandler
    public void onPlayerGliding(PlayerMoveEvent event) {
        var player = event.getPlayer();

        // skip if player is not gliding
        if (!player.isGliding())
            return;

        if (player.getInventory().getItemInMainHand().getType() != holdingItemType)
            return;

        // generate a speed indication bar ("||" for each 0.1 speed, gold for below trigger speed
        var speed = player.getVelocity().length();
        var speedBar = Component.text();
        for (var i = 0; i < 25; i++) {
            speedBar.append(Component.text("||").color(speed * 10 < i ? TextColor.color(NamedTextColor.GRAY) : TextColor.color(i < pluginInstance.config.elytraConfig.triggerSpeed * 10 ? 0xe89f64 : 0x96db81)));
        }
        speedBar.append(Component.text(" " + String.format("%.2f", speed * 20 * 60 / 1000) + " km/min").color(TextColor.color(NamedTextColor.WHITE)));
        player.sendActionBar(speedBar);

        // skip if speed exceed 0.8
        if (player.getVelocity().length() > pluginInstance.config.elytraConfig.triggerSpeed)
            return;

        // skip if player is snaking
        if (player.isSneaking())
            return;

        // skip if player is speeding up
        if (fireworkBind.containsKey(player.getUniqueId())) {
            if (fireworkBind.get(player.getUniqueId()).isDetonated()) {
                fireworkBind.remove(player.getUniqueId());
            } else {
                return;
            }
        }

        // select firework in pack
        var fireworkSlot = selectFireworkSlot(player);
        if (fireworkSlot == -1) return;
        var fireworkItem = subtractOneItem(player.getInventory(), fireworkSlot);
        if (fireworkItem == null) return;

        var firework = player.fireworkBoost(fireworkItem);
        fireworkBind.put(player.getUniqueId(), firework);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // clear cache for offline player
        fireworkSlotCache.remove(event.getPlayer().getUniqueId());
        fireworkBind.remove(event.getPlayer().getUniqueId());
    }

    private int selectFireworkSlot(Player player) {
        var inventory = player.getInventory();

        // trying to use a location cache
        var cacheLoc = fireworkSlotCache.getOrDefault(player.getUniqueId(), -1);
        if (cacheLoc != -1) {
            var item = inventory.getItem(cacheLoc);
            if (item != null && item.getType() == Material.FIREWORK_ROCKET) {
                return cacheLoc;
            }
        }

        for (var i = 0; i < inventory.getSize(); i++) {
            var item = inventory.getItem(i);
            if (item == null) continue;
            if (item.getType() == Material.FIREWORK_ROCKET) {
                fireworkSlotCache.put(player.getUniqueId(), i);
                return i;
            }
        }

        fireworkSlotCache.remove(player.getUniqueId());
        return -1;
    }

    private ItemStack subtractOneItem(PlayerInventory inventory, int slot) {
        var item = inventory.getItem(slot);
        if (item == null)
            return null;
        if (item.getAmount() == 1) {
            inventory.setItem(slot, new ItemStack(Material.AIR));
        } else {
            item.setAmount(item.getAmount() - 1);
        }
        return item.asOne();
    }
}