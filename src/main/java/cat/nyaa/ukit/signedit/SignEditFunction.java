package cat.nyaa.ukit.signedit;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import cat.nyaa.ukit.utils.Utils;
import land.melon.lab.simplelanguageloader.utils.ColorConverter;
import land.melon.lab.simplelanguageloader.utils.Pair;
import land.melon.lab.simplelanguageloader.utils.TextUtils;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SignEditFunction implements SubCommandExecutor, SubTabCompleter {
    private final SpigotLoader pluginInstance;
    private final String SIGNEDIT_PERMISSION_NODE = "ukit.signedit";
    private final List<String> subCommands = List.of("1", "2", "3", "4");

    public SignEditFunction(SpigotLoader pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.playerOnlyCommand.produce());
            return true;
        } else if (!commandSender.hasPermission(SIGNEDIT_PERMISSION_NODE)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.permissionDenied.produce());
            return true;
        } else if (args.length < 2) {
            return false;
        } else {
            Player player = (Player) commandSender;
            var targetBlock = Utils.getBlockLookingAt(player);
            if (targetBlock.getState() instanceof Sign sign) {
                int line;
                try {
                    line = Integer.parseInt(args[0]);
                    if (line < 1 || line > 4)
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(pluginInstance.language.signEditLang.invalidLineNumber.produce(
                            Pair.of("input", args[0])
                    ));
                    return true;
                }
                var finalLine = ColorConverter.convertToLegacyColorCode(ColorConverter.convertConvenientColorCode(String.join(" ", Arrays.copyOfRange(args, 1, args.length))), '&');
                if (TextUtils.countWord(finalLine, '§') > pluginInstance.config.signEditConfig.maxLengthPerLine) {
                    commandSender.sendMessage(pluginInstance.language.signEditLang.textTooLong.produce(
                            Pair.of("max", pluginInstance.config.signEditConfig.maxLengthPerLine)
                    ));
                } else {
                    sign.setLine(line - 1, finalLine);
                    sign.update();
                }
            } else {
                commandSender.sendMessage(pluginInstance.language.signEditLang.notASign.produce());
            }
            return true;
        }
    }

    @Override
    public String getHelp() {
        return pluginInstance.language.signEditLang.help.produce(
                Pair.of("max", pluginInstance.config.signEditConfig.maxLengthPerLine)
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
                return subCommands;
            } else if (args.length == 1) {
                return subCommands.stream().filter(s -> s.startsWith(args[0])).toList();
            } else {
                if (subCommands.contains(args[0])) {
                    var line = Integer.parseInt(args[0]);
                    var targetBlock = Utils.getBlockLookingAt(player);
                    if (targetBlock.getState() instanceof Sign sign) {
                        var suggestion = sign.getLine(line - 1).replaceAll("&", "§§").replaceAll("§", "&");
                        return suggestion.length() == 0 ? null : List.of(suggestion);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean checkPermission(CommandSender commandSender){
        return commandSender.hasPermission(SIGNEDIT_PERMISSION_NODE);
    }
}
