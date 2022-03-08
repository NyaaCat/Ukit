package cat.nyaa.ukit.signedit;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import cat.nyaa.ukit.utils.Utils;
import land.melon.lab.simplelanguageloader.utils.ColorConverter;
import land.melon.lab.simplelanguageloader.utils.Pair;
import land.melon.lab.simplelanguageloader.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

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
        }  else if (args.length < 2) {
            return false;
        }
        return replaceNbt(commandSender, args);
    }

    private boolean checkAndUpdateSign(CommandSender commandSender, String[] args, Consumer<Pair<Integer, String>> consumer) {
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
            consumer.accept(new Pair<>(line, finalLine));
        }
        return false;
    }

    public boolean replaceNbt(CommandSender commandSender, String[] args){
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.playerOnlyCommand.produce());
            return true;
        } else if (!commandSender.hasPermission(SIGNEDIT_PERMISSION_NODE)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.permissionDenied.produce());
            return true;
        }
        Player player = ((Player) commandSender);
        ItemStack item = player.getInventory().getItemInMainHand().clone();
        if (!isSign(item.getType())) {
            commandSender.sendMessage(pluginInstance.language.signEditLang.itemNotASign.produce());
            return false;
        }
        if (args.length < 2) {
            printSignContent(player, msgFromItemStack(item));
            return false;
        }
        checkAndUpdateSign(commandSender, args, (pair) -> {
            // replace line
            List<String> strings = msgFromItemStack(item);
            strings.set(pair.key(), pair.value());
            // replace item
            player.getInventory().setItemInMainHand(toItemStack(item, strings));
        });
        return true;
    }

    public ItemStack toItemStack(ItemStack item, List<String> lines) {
        if (item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) item.getItemMeta();
            if (blockStateMeta.getBlockState() instanceof Sign) {
                Sign sign = ((Sign) blockStateMeta.getBlockState());
                for (int i = 0; i < 4; i++) {
                    sign.setLine(i, lines.get(i));
                }
                blockStateMeta.setBlockState(sign);
            }
            item.setItemMeta(blockStateMeta);
        }
        return item;
    }

    private List<String> msgFromItemStack(ItemStack item) {
        List<String> result = new ArrayList<>(4);
        if (item.hasItemMeta() && item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) item.getItemMeta();
            if (blockStateMeta.hasBlockState() && blockStateMeta.getBlockState() instanceof Sign) {
                Sign sign = ((Sign) blockStateMeta.getBlockState());
                for (int i = 0; i < 4; i++) {
                    result.add(i, sign.getLine(i));
                }
            }
        }
        return result;
    }

    public void printSignContent(Player player, List<String> content) {
        player.sendMessage(pluginInstance.language.signEditLang.signInfoMsg.produce());
        for (int i = 0; i < 4; i++) {
            // todo implement this
//            Message msg = new Message(I18n.format("user.signedit.content_line", i, content.getLine(i)));
//            msg.inner.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/nu se sign "+i+" "+content.getLine(i).replace('§','&')));
//            msg.send(player, Message.MessageType.CHAT);
            player.sendMessage(pluginInstance.language.signEditLang.signInfo.produce(new Pair<>("msg", content.get(i))));
        }
    }

    private boolean isSign(Material type) {
        return Tag.SIGNS.isTagged(type);
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
}
