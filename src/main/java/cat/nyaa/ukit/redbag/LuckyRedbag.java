package cat.nyaa.ukit.redbag;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.Utils;
import land.melon.lab.simplelanguageloader.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class LuckyRedbag extends FixedRedbag {
    private final List<Double> amountTable;

    public LuckyRedbag(Player owner, double amount, int quality, SpigotLoader pluginInstance) {
        super(owner, amount, quality, pluginInstance);

        var factorTable = new ArrayList<Double>(quality);
        var sum = 0.0;
        for (int i = 0; i < quality; i++) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            var factor = random.nextDouble();
            sum += factor;
            factorTable.add(factor);
        }
        var finalSum = sum;
        amountTable = factorTable.stream().map(t -> amount * (t / finalSum)).toList();
    }

    @Override
    public Pair<Boolean, Double> actGrub(Player player) {
        var amount = amountTable.get(grabbedMap.size());
        var success = pluginInstance.economyProvider.depositPlayer(player.getUniqueId(), amount);
        if (success) {
            grabbedMap.put(player.getUniqueId(), amount);
        }
        return Pair.of(success, success ? amount : 0);
    }

    @Override
    public void announceFinished() {
        if (grabbedMap.isEmpty()) {
            return;
        }
        Pair<UUID, Double> luckyKingRecord = null;
        for (var k : grabbedMap.keySet()) {
            if (luckyKingRecord == null) {
                luckyKingRecord = Pair.of(k, grabbedMap.get(k));
                continue;
            }
            if (luckyKingRecord.value() < grabbedMap.get(k))
                luckyKingRecord = Pair.of(k, grabbedMap.get(k));
        }
        Utils.silentBroadcast(pluginInstance.language.redbagLang.luckRedbagDone.produce(
                Pair.of("owner", owner.getName()),
                Pair.of("type", getName()),
                Pair.of("luckiestOne", Bukkit.getOfflinePlayer(luckyKingRecord.key()).getName()),
                Pair.of("amount", luckyKingRecord.value()),
                Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural())
        ));
    }

    @Override
    public double getRemainingMoney() {
        var sum = 0.0D;
        for (int i = grabbedMap.size(); i < quantity; i++) {
            sum += amountTable.get(i);
        }
        return sum;
    }

    @Override
    public String getName() {
        return pluginInstance.language.redbagLang.luckyRedbagName.produce();
    }

    @Override
    public double getAmountTotal() {
        return amount;
    }
}
