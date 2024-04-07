package cat.nyaa.ukit.signedit;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.LockettePluginUtils;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import cat.nyaa.ukit.utils.Utils;
import land.melon.lab.simplelanguageloader.utils.ColorConverter;
import land.melon.lab.simplelanguageloader.utils.Pair;
import land.melon.lab.simplelanguageloader.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SignEditFunction implements SubCommandExecutor, SubTabCompleter {
    private final SpigotLoader pluginInstance;
    private final String SIGNEDIT_PERMISSION_NODE = "ukit.signedit";
    private final List<String> subCommands = List.of("1", "2", "3", "4");


    public SignEditFunction(SpigotLoader pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player senderPlayer)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.playerOnlyCommand.produce());
            return true;
        } else if (!senderPlayer.hasPermission(SIGNEDIT_PERMISSION_NODE)) {
            senderPlayer.sendMessage(pluginInstance.language.commonLang.permissionDenied.produce());
            return true;
        } else if (args.length < 2) {
            return false;
        } else {
            var targetBlock = Utils.getBlockLookingAt(senderPlayer);
            if (targetBlock == null) {
                commandSender.sendMessage(pluginInstance.language.signEditLang.notASign.produce());
                return true;
            }
            if (targetBlock.getState() instanceof Sign sign) {
                // fail-safe
                if (LockettePluginUtils.isLockSign(targetBlock)) {
                    commandSender.sendMessage(pluginInstance.language.signEditLang.cantModifyLockSign.produce());
                    return true;
                }
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
                    for (String word : pluginInstance.config.signEditConfig.wordsNotAllowed) {
                        if (finalLine.toLowerCase(Locale.ROOT).contains(word)) {
                            senderPlayer.sendMessage(pluginInstance.language.signEditLang.containsWordNotAllowed.produce(
                                    Pair.of("word", word.replace("§", "&"))
                            ));
                            return true;
                        }
                    }
                    var signSide = Utils.getSignSideLookingAt(senderPlayer, sign);
                    var lineContentBeforeChange = signSide.getLine(line - 1);
                    signSide.setLine(line - 1, finalLine);
                    sign.update();
                    var blockPlaceEvent = new BlockPlaceEvent(targetBlock, targetBlock.getState(),
                            targetBlock.getBlockData() instanceof Directional ?
                                    targetBlock.getRelative(((Directional) targetBlock.getBlockData()).getFacing().getOppositeFace()) :
                                    targetBlock.getRelative(BlockFace.DOWN),
                            new ItemStack(targetBlock.getType(), 1),
                            senderPlayer,
                            !inInSpawnProtection(targetBlock.getLocation()) || senderPlayer.isOp(),
                            EquipmentSlot.HAND);
                    Bukkit.getPluginManager().callEvent(blockPlaceEvent);
                    if (blockPlaceEvent.isCancelled()) {
                        signSide.setLine(line - 1, lineContentBeforeChange);
                        sign.update();
                        senderPlayer.sendMessage(pluginInstance.language.signEditLang.modifyCancelled.produce());
                        return true;
                    }

                }
            } else {
                commandSender.sendMessage(pluginInstance.language.signEditLang.notASign.produce());
            }
            return true;
        }
    }


    private boolean inInSpawnProtection(Location location) {
        return location.toVector().subtract(location.getWorld().getSpawnLocation().toVector()).length() <= pluginInstance.getServer().getSpawnRadius();
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

                    if (targetBlock == null)
                        return List.of();

                    if (targetBlock.getState() instanceof Sign sign) {
                        if (LockettePluginUtils.isLockSign(targetBlock)) {
                            return List.of();
                        }
                        var signSide = Utils.getSignSideLookingAt(player, sign);

                        var suggestion = signSide.getLine(line - 1).replaceAll("&", "§§").replaceAll("§", "&");
                        return suggestion.length() == 0 ? List.of() : List.of(suggestion);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean checkPermission(CommandSender commandSender) {
        return commandSender.hasPermission(SIGNEDIT_PERMISSION_NODE);
    }
}
