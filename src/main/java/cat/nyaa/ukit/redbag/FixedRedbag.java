package cat.nyaa.ukit.redbag;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.Utils;
import land.melon.lab.simplelanguageloader.utils.Pair;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FixedRedbag {
    final int quantity;
    final double amount;
    final Player owner;
    final SpigotLoader pluginInstance;
    final Map<UUID, Double> grabbedMap = new HashMap<>();
    boolean finished = false;

    public FixedRedbag(Player owner, double amount, int quantity, SpigotLoader pluginInstance) {
        this.owner = owner;
        this.quantity = quantity;
        this.amount = amount;
        this.pluginInstance = pluginInstance;
    }

    public boolean isGrabbed(Player player) {
        return grabbedMap.containsKey(player.getUniqueId());
    }

    public synchronized void grub(Player player) {
        if (grabbedMap.containsKey(player.getUniqueId())) {
            player.sendMessage(pluginInstance.language.redbagLang.alreadyGrabbed.produce());
        } else if (isFinished()) {
            player.sendMessage(pluginInstance.language.redbagLang.tooLate.produce());
        } else {
            var success = actGrub(player);
            if (success.key()) {
                player.sendMessage(
                        pluginInstance.language.redbagLang.grabbedFeedbackToGrabber.produce(
                                Pair.of("owner", owner.getName()),
                                Pair.of("amount", success.value()),
                                Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural()),
                                Pair.of("owner", owner.getName()),
                                Pair.of("type", getName())
                        )
                );
                owner.sendMessage(
                        pluginInstance.language.redbagLang.grabbedFeedbackToOwner.produce(
                                Pair.of("player", player.getName()),
                                Pair.of("amount", success.value()),
                                Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural()),
                                Pair.of("owner", owner.getName()),
                                Pair.of("type", getName())
                        )
                );
                if (grabbedMap.size() == quantity) {
                    finish();
                }
            } else {
                player.sendMessage(pluginInstance.language.redbagLang.failedToGrab.produce());
            }
        }
    }

    public Pair<Boolean, Double> actGrub(Player player) {
        var success = pluginInstance.economyProvider.depositPlayer(player.getUniqueId(), amount);
        if (success)
            grabbedMap.put(player.getUniqueId(), amount);
        return Pair.of(success, success ? amount : 0);
    }

    public Map<UUID, Double> getGrabbedMap() {
        return grabbedMap;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getAmountTotal() {
        return amount * quantity;
    }

    public String getName() {
        return pluginInstance.language.redbagLang.fixedRedbagName.produce();
    }

    public Player getOwner() {
        return owner;
    }

    public double getRemainingMoney() {
        return (quantity - grabbedMap.size()) * amount;
    }

    public boolean isFinished() {
        return finished;
    }

    public void announceFinished() {
        Utils.silentBroadcast(pluginInstance.language.redbagLang.fixedRedbagDone.produce(
                Pair.of("owner", owner.getName()),
                Pair.of("type", getName())
        ));
    }

    public synchronized void finish() {
        if (finished) {
            return;
        }
        finished = true;
        announceFinished();
        var refund = getRemainingMoney();
        if (refund != 0) {
            var success = pluginInstance.economyProvider.depositPlayer(owner.getUniqueId(), refund);
            if (success) {
                if (owner.isOnline()) {
                    owner.sendMessage(pluginInstance.language.redbagLang.refundFeedback.produce(
                            Pair.of("amount", refund),
                            Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural())
                    ));
                }
            } else {
                var time = Long.toHexString(System.currentTimeMillis());
                pluginInstance.getLogger().warning("(redbag) Failed to refund " + refund + " to " + owner.getName() + "(" + owner.getUniqueId() + "). Time: 0x" + time);
                if (owner.isOnline()) {
                    owner.sendMessage(pluginInstance.language.redbagLang.failedToRefund.produce(
                            Pair.of("amount", refund),
                            Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural()),
                            Pair.of("timestamp", time)
                    ));
                }
            }
        }
    }
}
