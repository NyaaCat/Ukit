package cat.nyaa.ukit.redbag;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import cat.nyaa.ukit.utils.Utils;
import land.melon.lab.simplelanguageloader.utils.Pair;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class RedbagFunction implements SubCommandExecutor, SubTabCompleter, Listener {
    private final SpigotLoader pluginInstance;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final List<String> subCommands = List.of("create", "confirm", "cancel");
    private final Map<String, FixedRedbag> redbagMap = new ConcurrentHashMap<>();
    private final Map<UUID, Pair<FixedRedbag, String>> waitingMap = new HashMap<>();
    private final Pattern formatCodePattern = Pattern.compile("&[0-9A-Fa-fLlMmNnOoRrXxKk]");
    private final String REDBAG_PERMISSION_NODE = "ukit.redbag";
    private final boolean isDisabled;

    public RedbagFunction(SpigotLoader pluginInstance) {
        this.pluginInstance = pluginInstance;
        isDisabled = pluginInstance.economyProvider == null;
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player senderPlayer)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.playerOnlyCommand.produce());
            return true;
        }
        if (!commandSender.hasPermission(REDBAG_PERMISSION_NODE)) {
            senderPlayer.sendMessage(pluginInstance.language.commonLang.permissionDenied.produce());
            return true;
        }
        if (isDisabled) {
            commandSender.sendMessage(pluginInstance.language.commonLang.functionDisabled.produce());
            return true;
        }
        if (args.length < 1 || !subCommands.contains(args[0].toLowerCase())) {
            commandSender.sendMessage(
                    pluginInstance.language.redbagLang.help.produce()
            );
            return true;
        }
        switch (args[0]) {
            case "create" -> {
                var options = List.of("fixed", "lucky");
                if (args.length < 4) {
                    pluginInstance.language.redbagLang.help.produce();
                    return true;
                }
                double amount;
                int quantity;
                if (!options.contains(args[1].toLowerCase())) {
                    pluginInstance.language.redbagLang.help.produce();
                    return true;
                }
                try {
                    amount = Double.parseDouble(args[2]);
                    if (amount == 0 || Double.isInfinite(amount) || Double.isNaN(amount))
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) {
                    senderPlayer.sendMessage(pluginInstance.language.redbagLang.invalidAmount.produce(Pair.of("input", args[2])));
                    return true;
                }
                try {
                    quantity = Integer.parseInt(args[3]);
                    if (quantity == 0)
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) {
                    senderPlayer.sendMessage(pluginInstance.language.redbagLang.invalidQuantity.produce(Pair.of("input", args[3])));
                    return true;
                }
                if (redbagMap.values().stream().anyMatch(t -> t.getOwner().getUniqueId() == senderPlayer.getUniqueId())) {
                    senderPlayer.sendMessage(pluginInstance.language.redbagLang.activeOneExist.produce());
                    return true;
                }
                FixedRedbag redbag;
                if (quantity > pluginInstance.config.redbagConfig.maximumQuantity || quantity < pluginInstance.config.redbagConfig.minimumQuantity) {
                    senderPlayer.sendMessage(pluginInstance.language.redbagLang.quantityNotInRange.produce(
                            Pair.of("minimum", pluginInstance.config.redbagConfig.minimumQuantity),
                            Pair.of("maximum", pluginInstance.config.redbagConfig.maximumQuantity),
                            Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural())
                    ));
                    return true;
                }
                if (args[1].equalsIgnoreCase("fixed")) {
                    if (amount < pluginInstance.config.redbagConfig.minimumAmountPerRedbag) {
                        senderPlayer.sendMessage(pluginInstance.language.redbagLang.amountTooLow.produce(
                                Pair.of("amount", pluginInstance.config.redbagConfig.minimumAmountPerRedbag),
                                Pair.of("currencyUnit",pluginInstance.economyProvider.currencyNamePlural())
                        ));
                        return true;
                    }
                    redbag = new FixedRedbag(senderPlayer, amount, quantity, pluginInstance);
                } else/*lucky*/ {
                    if (amount / quantity < pluginInstance.config.redbagConfig.minimumAmountPerRedbag) {
                        senderPlayer.sendMessage(pluginInstance.language.redbagLang.amountTooLow.produce(
                                Pair.of("amount", pluginInstance.config.redbagConfig.minimumAmountPerRedbag),
                                Pair.of("currencyUnit",pluginInstance.economyProvider.currencyNamePlural())
                        ));
                        return true;
                    }
                    redbag = new LuckyRedbag(senderPlayer, amount, quantity, pluginInstance);
                }
                String password;
                if (args.length >= 5) {
                    password = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                } else {
                    do
                        password = generateRandomPassword(senderPlayer.getUniqueId());
                    while (redbagMap.containsKey(password));
                }
                if (password.length() > pluginInstance.config.redbagConfig.maxPasswordLength) {
                    senderPlayer.sendMessage(pluginInstance.language.redbagLang.passwordTooLong.produce(
                            Pair.of("password", password),
                            Pair.of("maxLength", pluginInstance.config.redbagConfig.maxPasswordLength)
                    ));
                    return true;
                }
                if (password.startsWith("/")){
                    senderPlayer.sendMessage(pluginInstance.language.redbagLang.cantStartsWithSlash.produce());
                    return true;
                }
                if (formatCodePattern.matcher(password).find()) {
                    senderPlayer.sendMessage(pluginInstance.language.redbagLang.cantUseFormatCode.produce());
                    return true;
                }
                if (redbagMap.containsKey(password)) {
                    senderPlayer.sendMessage(pluginInstance.language.redbagLang.duplicatePassword.produce());
                    return true;
                }
                waitingMap.put(senderPlayer.getUniqueId(), Pair.of(redbag, password));
                var yesButton = new TextComponent(TextComponent.fromLegacyText(pluginInstance.language.redbagLang.previewYesButtonText.produce()));
                yesButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(pluginInstance.language.redbagLang.previewYesButtonHoverText.produce()))));
                yesButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ukit redbag confirm"));
                var noButton = new TextComponent(TextComponent.fromLegacyText(pluginInstance.language.redbagLang.previewNoButtonText.produce()));
                noButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(pluginInstance.language.redbagLang.previewNoButtonHoverText.produce()))));
                noButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ukit redbag cancel"));
                senderPlayer.spigot().sendMessage(
                        pluginInstance.language.redbagLang.redbagPreview.produceWithBaseComponent(
                                Pair.of("type", redbag.getName()),
                                Pair.of("amount", redbag.getAmountTotal()),
                                Pair.of("quantity", redbag.getQuantity()),
                                Pair.of("password", password),
                                Pair.of("yesButton", yesButton),
                                Pair.of("noButton", noButton)
                        )
                );
            }
            case "confirm" -> {
                if (!waitingMap.containsKey(senderPlayer.getUniqueId())) {
                    senderPlayer.sendMessage(pluginInstance.language.redbagLang.noWaitingRedbag.produce());
                    return true;
                }
                var record = waitingMap.get(senderPlayer.getUniqueId());
                var balance = pluginInstance.economyProvider.getPlayerBalance(senderPlayer.getUniqueId());
                if (record.key().getAmountTotal() > balance) {
                    senderPlayer.sendMessage(pluginInstance.language.redbagLang.cantOffer.produce(
                            Pair.of("amount", record.key().getAmountTotal()),
                            Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural())
                    ));
                    return true;
                }
                var success = pluginInstance.economyProvider.withdrawPlayer(senderPlayer.getUniqueId(), record.key().getAmountTotal());
                if (!success) {
                    senderPlayer.sendMessage(pluginInstance.language.redbagLang.payFailure.produce());
                    return true;
                }
                waitingMap.remove(senderPlayer.getUniqueId());
                redbagMap.put(record.value(), record.key());
                pluginInstance.getServer().getScheduler().runTaskLater(pluginInstance, () -> {
                    if (!record.key().isDisabled()) {
                        redbagMap.remove(record.value()).disable();
                    }
                }, pluginInstance.config.redbagConfig.redbagLifeInSecond * 20L);
                senderPlayer.sendMessage(pluginInstance.language.redbagLang.redbagCreatedFeedback.produce());
                var grabButton = new TextComponent(TextComponent.fromLegacyText(pluginInstance.language.redbagLang.grabButtonText.produce()));
                grabButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(pluginInstance.language.redbagLang.grabButtonHoverText.produce(
                        Pair.of("password", record.value())
                )))));
                grabButton.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, record.value()));
                Utils.silentBroadcast(pluginInstance.language.redbagLang.redbagBroadcast.produceWithBaseComponent(
                        Pair.of("player", senderPlayer.getName()),
                        Pair.of("type", record.key().getName()),
                        Pair.of("password", record.value()),
                        Pair.of("grabButton", grabButton)
                ));
            }
            case "cancel" -> {
                if (!waitingMap.containsKey(senderPlayer.getUniqueId())) {
                    senderPlayer.sendMessage(pluginInstance.language.redbagLang.noWaitingRedbag.produce());
                    return true;
                }
                waitingMap.remove(senderPlayer.getUniqueId());
                senderPlayer.sendMessage(pluginInstance.language.redbagLang.redbagCancelFeedback.produce());
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (redbagMap.containsKey(event.getMessage())) {
            var redbag = redbagMap.get(event.getMessage());
            if (!redbag.isGrabbed(event.getPlayer())) {
                pluginInstance.getServer().getScheduler().runTaskLater(pluginInstance, () -> {
                    redbag.grub(event.getPlayer());
                    if (redbag.isDisabled()) {
                        redbagMap.remove(event.getMessage());
                    }
                }, 1);
            }
        }
    }

    private String generateRandomPassword(UUID playerUniqueID) {
        var base = String.valueOf(100000 + random.nextInt(899999));
        var constant = String.valueOf(Math.abs(playerUniqueID.getLeastSignificantBits() % 100));
        return base + constant;
    }

    @Override
    public String getHelp() {
        return pluginInstance.language.redbagLang.help.produce();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player playerSender)) {
            return null;
        }
        var completeList = new ArrayList<String>();
        if (args.length <= 1) {
            completeList.add("create");
            if (waitingMap.containsKey(playerSender.getUniqueId())) {
                completeList.add("confirm");
                completeList.add("cancel");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            completeList.add("fixed");
            completeList.add("lucky");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create") && (args[1].equalsIgnoreCase("fixed") || args[1].equalsIgnoreCase("lucky"))) {
            completeList.add("<amount>");
        } else if (args.length == 4 && args[0].equalsIgnoreCase("create") && (args[1].equalsIgnoreCase("fixed") || args[1].equalsIgnoreCase("lucky"))) {
            completeList.add("<quantity>");
        } else if (args.length == 5 && args[0].equalsIgnoreCase("create") && (args[1].equalsIgnoreCase("fixed") || args[1].equalsIgnoreCase("lucky"))) {
            completeList.add("[password...]");
        }
        return completeList;
    }

    public void refundAll() {
        redbagMap.values().forEach(FixedRedbag::disable);
        redbagMap.clear();
    }

    @Override
    public boolean checkPermission(CommandSender commandSender){
        return commandSender.hasPermission(REDBAG_PERMISSION_NODE);
    }
}
