package cat.nyaa.ukit.chat;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import land.melon.lab.simplelanguageloader.utils.ColorConverter;
import land.melon.lab.simplelanguageloader.utils.Pair;
import land.melon.lab.simplelanguageloader.utils.TextUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ChatFunction implements SubCommandExecutor, SubTabCompleter {
    public final String CHAT_PERMISSION_NODE = "ukit.chat";
    private final SpigotLoader pluginInstance;
    private final List<String> subCommands = List.of("prefix", "suffix");
    private final List<String> options = List.of("set", "remove");
    private final boolean disabled;

    public ChatFunction(SpigotLoader pluginInstance) {
        this.pluginInstance = pluginInstance;
        disabled = pluginInstance.chatProvider == null || pluginInstance.economyProvider == null;
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        //ukit chat <prefix|suffix> <set|remove> <content>
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
        } else if (args.length < 2 || !subCommands.contains(args[0].toLowerCase()) || !options.contains(args[1].toLowerCase())) {
            return false;
        } else {
            var isPrefix = args[0].equalsIgnoreCase("prefix");
            var settings = isPrefix ? pluginInstance.config.chatSettings.prefix : pluginInstance.config.chatSettings.suffix;
            var lang = isPrefix ? pluginInstance.language.chatLang.prefixLang : pluginInstance.language.chatLang.suffixLang;

            if (args[1].equalsIgnoreCase("set")) {
                if (args.length < 3) return false;
                var joinedText = ColorConverter.translateToLegacyColorText("&r" + String.join(" ", Arrays.copyOfRange(args, 2, args.length)) + "&r", '&');
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
                    pluginInstance.economyProvider.depositPlayer(senderPlayer.getUniqueId(), settings.moneyCost);
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

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
