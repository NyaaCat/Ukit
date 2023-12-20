package cat.nyaa.ukit.chat;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import io.papermc.paper.event.player.AsyncChatEvent;
import land.melon.lab.simplelanguageloader.utils.ColorConverter;
import land.melon.lab.simplelanguageloader.utils.Pair;
import land.melon.lab.simplelanguageloader.utils.TextUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class ChatFunction implements SubCommandExecutor, SubTabCompleter, Listener {
    public final String CHAT_PERMISSION_NODE = "ukit.chat";
    private final SpigotLoader pluginInstance;
    private final List<String> subCommands = List.of("prefix", "suffix", "signature");
    private final List<String> prefixOptions = List.of("set", "remove");
    private final List<String> signaturePreferences = List.of("true", "false");

    private final NamespacedKey MSG_SIGNATURE_FLAG;
    private final boolean disabled;

    public ChatFunction(SpigotLoader pluginInstance) {
        this.pluginInstance = pluginInstance;
        MSG_SIGNATURE_FLAG = new NamespacedKey(pluginInstance, "MSG_SIGNATURE_FLAG");
        disabled = pluginInstance.chatProvider == null || pluginInstance.economyProvider == null;
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        //ukit chat <prefix|suffix> <set|remove> <content>
        //ukit chat signature <true|false>
        if (!(commandSender instanceof Player senderPlayer)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.playerOnlyCommand.produce());
            return true;
        }
        if (disabled) {
            senderPlayer.sendMessage(pluginInstance.language.commonLang.functionDisabled.produce());
            return true;
        } else if (!senderPlayer.hasPermission(CHAT_PERMISSION_NODE)) {
            senderPlayer.sendMessage(pluginInstance.language.commonLang.permissionDenied.produce());
            return true;
        } else if (args.length < 2 || !subCommands.contains(args[0].toLowerCase())) {
            return false;
        } else {
            if ("signature".equalsIgnoreCase(args[0])) {
                var preference = Boolean.valueOf(args[1]);
                senderPlayer.getPersistentDataContainer().set(MSG_SIGNATURE_FLAG, PersistentDataType.BOOLEAN, preference);
                senderPlayer.sendMessage(
                        pluginInstance.language.chatLang.signaturePreferenceSet.produce(
                                Pair.of("flag", preference ?
                                        pluginInstance.language.commonLang.textTrue.produce() : pluginInstance.language.commonLang.textFalse.produce())
                        )
                );
                return true;
            } //else prefix/suffix
            if (!prefixOptions.contains(args[1].toLowerCase()))
                return false;
            var isPrefix = args[0].equalsIgnoreCase("prefix");
            var settings = isPrefix ? pluginInstance.config.chatSettings.prefix : pluginInstance.config.chatSettings.suffix;
            var lang = isPrefix ? pluginInstance.language.chatLang.prefixLang : pluginInstance.language.chatLang.suffixLang;

            if (args[1].equalsIgnoreCase("set")) {
                if (args.length < 3) return false;
                var joinedText = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                if (!joinedText.startsWith("&r"))
                    joinedText = "&r" + joinedText;
                if (!joinedText.endsWith("&r")) joinedText = joinedText + "&r";
                joinedText = ColorConverter.translateToLegacyColorText(joinedText, '&');
                if (settings.enabled) {
                    if (senderPlayer.getLevel() < settings.expCostLvl || pluginInstance.economyProvider.getPlayerBalance(senderPlayer.getUniqueId()) < settings.moneyCost) {
                        senderPlayer.sendMessage(lang.cantOffered.produce(
                                Pair.of("exp", settings.expCostLvl),
                                Pair.of("money", settings.moneyCost),
                                Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural())
                        ));
                        return true;
                    }
                    if (TextUtils.countWord(joinedText, '§') > settings.maxLength) {
                        senderPlayer.sendMessage(lang.textTooLong.produce(
                                Pair.of("text", joinedText),
                                Pair.of("max", settings.maxLength)
                        ));
                        return true;
                    }
//                    //check words
//                    for (var word : settings.prohibitedWords) {
//                        if (joinedText.contains(word)) {
//                            senderPlayer.sendMessage(
//                                    pluginInstance.language.chatLang.prohibitedWord.produce(
//                                            Pair.of("word", word)
//                                    )
//                            );
//                            return true;
//                        }
//                    }
                    pluginInstance.economyProvider.withdrawPlayer(senderPlayer.getUniqueId(), settings.moneyCost);
                    pluginInstance.economyProvider.depositSystemVault(settings.moneyCost);
                    senderPlayer.setLevel(senderPlayer.getLevel() - settings.expCostLvl);
                    if (isPrefix) {
                        pluginInstance.chatProvider.setPlayerPrefix(null, senderPlayer, settings.customPattern.produce(Pair.of("text", joinedText)));
                    } else {
                        pluginInstance.chatProvider.setPlayerSuffix(null, senderPlayer, settings.customPattern.produce(Pair.of("text", joinedText)));
                    }
                    senderPlayer.sendMessage(lang.settingApplied.produce(
                            Pair.of("text", joinedText)
                    ));

                } else {
                    senderPlayer.sendMessage(lang.notEnabled.produce());
                }
            } else /*remove*/ {
                if (isPrefix) {
                    pluginInstance.chatProvider.setPlayerPrefix(null, senderPlayer, "");
                    senderPlayer.sendMessage(
                            lang.settingRemoved.produce()
                    );
                } else {
                    pluginInstance.chatProvider.setPlayerSuffix(null, senderPlayer, "");
                    senderPlayer.sendMessage(
                            lang.settingRemoved.produce()
                    );
                }
            }
            return true;
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        var dataContainer = event.getPlayer().getPersistentDataContainer();
        if (dataContainer.has(MSG_SIGNATURE_FLAG) && Boolean.FALSE.equals(dataContainer.get(MSG_SIGNATURE_FLAG, PersistentDataType.BOOLEAN))) {
            event.setCancelled(true);
            event.viewers().forEach(t -> {
                t.sendMessage(event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), event.message(), t));
            });
        }
        //skip if true
    }

    @Override
    public String getHelp() {
        return pluginInstance.language.chatLang.help.produce();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (disabled || sender instanceof ConsoleCommandSender) {
            return null;
        }
        if (args.length == 0) {
            return subCommands;
        } else if (args.length == 1) {
            return subCommands.stream().filter(t -> t.startsWith(args[0])).toList();
        } else if (args.length == 2) {
            return ("signature".equalsIgnoreCase(args[0]) ?
                    signaturePreferences : prefixOptions).stream().filter(t -> t.startsWith(args[1])).toList();
        } else if (args.length == 3) {
            if ("signature".equalsIgnoreCase(args[0])) return null;
            if (!subCommands.contains(args[0].toLowerCase()) || !args[1].equalsIgnoreCase("set"))
                return null;
            var isPrefix = args[0].equalsIgnoreCase("prefix");
            if (isPrefix) {
                var prefix = pluginInstance.chatProvider.getPlayerPrefix((Player) sender);
                if (prefix.isEmpty())
                    return null;
                else
                    return List.of(prefix.trim().replace("&", "§§").replace("§", "&"));
            } else /*suffix*/ {
                var suffix = pluginInstance.chatProvider.getPlayerSuffix((Player) sender);
                if (suffix.isEmpty())
                    return null;
                else
                    return List.of(suffix.trim().replace("&", "§§").replace("§", "&"));
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean checkPermission(CommandSender commandSender) {
        return commandSender.hasPermission(CHAT_PERMISSION_NODE);
    }
}
