package cat.nyaa.ukit.elytra;

import cat.nyaa.ukit.SpigotLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ElytraFunction implements Listener {
    private final SpigotLoader pluginInstance;
    private final Material holdingItemType;

    public ElytraFunction(SpigotLoader pluginInstance) {
        this.pluginInstance = pluginInstance;
        holdingItemType = Material.valueOf(pluginInstance.config.elytraConfig.holdingItem);
    }

    @EventHandler
    public void onPlayerGliding(PlayerMoveEvent event) {
        // skip if speed is high or player is not gliding
        if (!event.getPlayer().isGliding())
            return;

        if (event.getPlayer().getInventory().getItemInMainHand().getType() != holdingItemType)
            return;

        // generate a speed indication bar ("||" for each 0.1 speed, gold for below trigger speed
        var speed = event.getPlayer().getVelocity().length();
        var speedBar = Component.text();
        for (var i = 0; i < 25; i++) {
            speedBar.append(Component.text("||").color(speed * 10 < i ? TextColor.color(NamedTextColor.GRAY) : TextColor.color(i < pluginInstance.config.elytraConfig.triggerSpeed * 10 ? 0xe89f64 : 0x96db81)));
        }
        speedBar.append(Component.text(" " + String.format("%.2f", speed * 20 * 0.06) + " km/min").color(TextColor.color(NamedTextColor.WHITE)));
        event.getPlayer().sendActionBar(speedBar);

        // skip if speed exceed 0.8
        if (event.getPlayer().getVelocity().length() > pluginInstance.config.elytraConfig.triggerSpeed)
            return;

        // select firework in pack
        var fireworkSlot = selectFireworkSlot(event.getPlayer().getInventory());
        if (fireworkSlot == -1) return;
        var fireworkItem = subtractOneItem(event.getPlayer().getInventory(), fireworkSlot);
        if (fireworkItem == null) return;

        event.getPlayer().fireworkBoost(fireworkItem);
    }

    private int selectFireworkSlot(PlayerInventory inventory) {
        for (var i = 0; i < inventory.getSize(); i++) {
            var item = inventory.getItem(i);
            if (item == null) continue;
            if (item.getType() == Material.FIREWORK_ROCKET) {
                return i;
            }
        }
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