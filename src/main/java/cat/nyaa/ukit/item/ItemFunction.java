package cat.nyaa.ukit.item;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import land.melon.lab.simplelanguageloader.nms.ItemUtils;
import land.melon.lab.simplelanguageloader.utils.ColorConverter;
import land.melon.lab.simplelanguageloader.utils.Pair;
import land.melon.lab.simplelanguageloader.utils.TextUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ItemFunction implements SubCommandExecutor, SubTabCompleter {
    private final String ITEM_RENAME_PERMISSION_NODE = "ukit.item.rename";
    private final boolean enabled;
    private final List<String> subCommands = List.of("rename");

    public ItemFunction(SpigotLoader pluginInstance) {
        this.pluginInstance = pluginInstance;
        enabled = pluginInstance.economyProvider != null;
    }

    private final SpigotLoader pluginInstance;

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        //ukit item rename <name...>
        if (!enabled) {
            commandSender.sendMessage(pluginInstance.language.commonLang.functionDisabled.produce());
            return true;
        }
        if (!(commandSender instanceof Player senderPlayer)) {
            commandSender.sendMessage(
                    pluginInstance.language.commonLang.playerOnlyCommand.produce()
            );
            return true;
        }
        if (!checkPermission(senderPlayer)) {
            senderPlayer.sendMessage(pluginInstance.language.commonLang.permissionDenied.produce());
            return true;
        }
        if (args.length < 1) {
            return false;
        }
        if(!subCommands.contains(args[0].toLowerCase())){
            return false;
        }
        if (args[0].equalsIgnoreCase("rename")) {
            var config = pluginInstance.config.itemConfig;
            var displayName = ColorConverter.translateToLegacyColorText(String.join(" ", Arrays.copyOfRange(args, 1, args.length)), '&');
            var wordCount = TextUtils.countWord(displayName, '§');
            if (wordCount > config.maxLength) {
                senderPlayer.sendMessage(pluginInstance.language.itemLang.nameTooLong.produce(Pair.of("max", config.maxLength)));
                return true;
            }
            var isOffhand = false;
            var itemInHand = senderPlayer.getInventory().getItemInMainHand();
            if (itemInHand.getType().isAir()) {
                isOffhand = true;
                itemInHand = senderPlayer.getInventory().getItemInOffHand();
            }
            if (itemInHand.getType().isAir()) {
                senderPlayer.sendMessage(pluginInstance.language.itemLang.noItemInHand.produce());
                return true;
            }
            var costTotal = config.multiplyAmount ? config.costMoney * itemInHand.getAmount() : config.costMoney;
            if (!pluginInstance.economyProvider.withdrawPlayer(senderPlayer.getUniqueId(), costTotal)) {
                senderPlayer.sendMessage(pluginInstance.language.itemLang.cantOffer.produce(
                        Pair.of("amount", costTotal),
                        Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural())
                ));
                return true;
            }
            pluginInstance.economyProvider.depositSystemVault(costTotal);
            var itemMeta = itemInHand.getItemMeta();
            itemMeta.setDisplayName(displayName);
            itemInHand.setItemMeta(itemMeta);
            if (isOffhand) {
                senderPlayer.getInventory().setItemInOffHand(itemInHand);
            } else {
                senderPlayer.getInventory().setItemInMainHand(itemInHand);
            }
            senderPlayer.spigot().sendMessage(pluginInstance.language.itemLang.success.produceWithBaseComponent(
                    Pair.of("name", ItemUtils.itemTextWithHover(itemInHand)),
                    Pair.of("amount", costTotal),
                    Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural())
            ));
        }
        return true;
    }

    @Override
    public String getHelp() {
        return pluginInstance.language.itemLang.help.produce();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player senderPlayer)) {
            return null;
        }
        if (args.length < 2) {
            return subCommands.stream().filter(t -> t.startsWith(args[0])).toList();
        } else {
            return Stream.of(getItemNameInHand(senderPlayer)).filter(t -> t.startsWith(args[1])).toList();
        }
    }

    private String getItemNameInHand(Player senderPlayer) {
        var defaultText = "<name...>";
        var item = senderPlayer.getInventory().getItemInMainHand();
        if (item.getType().isAir())
            item = senderPlayer.getInventory().getItemInOffHand();
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName().replace("§","&");
        } else {
            return defaultText;
        }
    }

    @Override
    public boolean checkPermission(CommandSender commandSender) {
        return commandSender.hasPermission(ITEM_RENAME_PERMISSION_NODE);
    }
}
