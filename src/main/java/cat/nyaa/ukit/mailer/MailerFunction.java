package cat.nyaa.ukit.mailer;

import cat.nyaa.ukit.MainLang;
import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import land.melon.lab.simplelanguageloader.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.block.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MailerFunction implements SubCommandExecutor, SubTabCompleter, Listener {

    public static final String MAILER_PERMISSION_NODE = "ukit.mailer";
    private final SpigotLoader pluginInstance;
    private final MailboxLocRecorder locRecorder;
    private final List<String> subCommands = List.of("mailbox", "sendto");
    private final Map<UUID, ScheduledTask> mailboxSettingHoldTaskMap = new ConcurrentHashMap<>();
    private final boolean disabled;

    public MailerFunction(SpigotLoader pluginInstance, File mailerRecordFile) throws SQLException, IOException {
        this.pluginInstance = pluginInstance;
        locRecorder = new MailboxLocRecorder(mailerRecordFile);
        disabled = pluginInstance.economyProvider == null;
    }

    private MainLang getLanguage() {
        return pluginInstance.language;
    }


    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player senderPlayer)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.playerOnlyCommand.produce());
            return true;
        }
        if (!commandSender.hasPermission(MAILER_PERMISSION_NODE)) {
            senderPlayer.sendMessage(pluginInstance.language.commonLang.permissionDenied.produce());
            return true;
        }
        if (disabled) {
            commandSender.sendMessage(getLanguage().commonLang.functionDisabled.produce());
            return true;
        }

        if (args.length < 2 || !subCommands.contains(args[0].toLowerCase())) {
            commandSender.sendMessage(
                    getLanguage().mailerLang.mailerHelp.produce()
            );
            return true;
        }

        // /u mailer mailbox <set|clear>
        if (args[0].equalsIgnoreCase("mailbox")) {
            if (args[1].equalsIgnoreCase("set")) {
                var task = Bukkit.getGlobalRegionScheduler().runDelayed(pluginInstance, (scheduledTask) -> {
                    mailboxSettingHoldTaskMap.remove(senderPlayer.getUniqueId());
                    senderPlayer.sendMessage(
                            getLanguage().mailerLang.setCancelled.produce()
                    );
                }, 20 * 10);
                mailboxSettingHoldTaskMap.put(senderPlayer.getUniqueId(), task);
                commandSender.sendMessage(
                        getLanguage().mailerLang.rightClickToSet.produce()
                );
            } else if (args[1].equalsIgnoreCase("clear")) {
                try {
                    locRecorder.deleteMailboxRecord(senderPlayer.getUniqueId());
                    commandSender.sendMessage(
                            getLanguage().mailerLang.mailboxCleared.produce()
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                    commandSender.sendMessage(
                            getLanguage().commonLang.sqlErrorOccurred.produce()
                    );
                }
            } else if (args[1].equalsIgnoreCase("info")) {
                try {
                    var mailboxLoc = locRecorder.getMailboxLoc(senderPlayer.getUniqueId());
                    if (mailboxLoc == null) {
                        commandSender.sendMessage(
                                getLanguage().mailerLang.mailboxNotSetYet.produce()
                        );
                    } else {
                        var world = Bukkit.getWorld(mailboxLoc.getWorldUUID());
                        var worldName = world == null ? "invalid" : world.getName();
                        commandSender.sendMessage(
                                getLanguage().mailerLang.mailboxInfo.produce(
                                        Pair.of("x", mailboxLoc.getBlockX()),
                                        Pair.of("y", mailboxLoc.getBlockY()),
                                        Pair.of("z", mailboxLoc.getBlockZ()),
                                        Pair.of("world", worldName)
                                )
                        );
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    commandSender.sendMessage(
                            getLanguage().commonLang.sqlErrorOccurred.produce()
                    );
                }
            } else {
                commandSender.sendMessage(
                        getLanguage().mailerLang.mailerHelp.produce()
                );
            }
        } else /*args[1].equalsIgnoreCase("sendto")*/ {
            var targetPlayer = Bukkit.getOfflinePlayerIfCached(args[1]);
            if (targetPlayer == null) {
                commandSender.sendMessage(
                        getLanguage().commonLang.playerNotFound.produce()
                );
                return true;
            }
            try {
                var mailboxLoc = locRecorder.getMailboxLoc(targetPlayer.getUniqueId());
                if (mailboxLoc == null) {
                    commandSender.sendMessage(
                            getLanguage().mailerLang.playerNotSetMailboxYet.produce(
                                    Pair.of("player", targetPlayer.getName())
                            )
                    );
                    return true;
                }
                var world = Bukkit.getWorld(mailboxLoc.getWorldUUID());
                if (world == null) {
                    commandSender.sendMessage(
                            getLanguage().mailerLang.mailboxNotAvail.produce(
                                    Pair.of("player", targetPlayer.getName())
                            )
                    );
                    return true;
                }
                var blockState = world.getBlockAt(mailboxLoc.getBlockX(), mailboxLoc.getBlockY(), mailboxLoc.getBlockZ()).getState();
                if (!isValidMailbox(blockState)) {
                    commandSender.sendMessage(
                            getLanguage().mailerLang.mailboxNotAvail.produce(
                                    Pair.of("player", targetPlayer.getName())
                            )
                    );
                    return true;
                }
                if (((Container) blockState).getInventory().firstEmpty() == -1) {
                    commandSender.sendMessage(
                            getLanguage().mailerLang.mailboxFull.produce(
                                    Pair.of("player", targetPlayer.getName())
                            )
                    );
                    return true;
                }
                var itemInHand = senderPlayer.getInventory().getItemInMainHand();
                if (itemInHand.getType().isAir()) {
                    commandSender.sendMessage(
                            getLanguage().mailerLang.noItemInHand.produce()
                    );
                    return true;
                }
                var cost = Tag.SHULKER_BOXES.isTagged(itemInHand.getType()) ?
                        pluginInstance.config.mailerConfig.mailShulkerBoxCost : pluginInstance.config.mailerConfig.mailItemCost;
                if (!pluginInstance.economyProvider.withdrawPlayer(senderPlayer.getUniqueId(), cost)) {
                    commandSender.sendMessage(
                            getLanguage().mailerLang.moneyNotEnough.produce(
                                    Pair.of("amount", cost),
                                    Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural())
                            )
                    );
                    return true;
                }
                var inventory = ((Container) blockState).getInventory();
                inventory.addItem(itemInHand);
                senderPlayer.getInventory().setItemInMainHand(null);
                pluginInstance.economyProvider.depositSystemVault(cost);

                commandSender.sendMessage(
                        getLanguage().mailerLang.itemSent.produce(
                                Pair.of("player", targetPlayer.getName()),
                                Pair.of("amount", cost),
                                Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural())));

            } catch (SQLException e) {
                e.printStackTrace();
                commandSender.sendMessage(
                        getLanguage().commonLang.sqlErrorOccurred.produce()
                );
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!mailboxSettingHoldTaskMap.containsKey(event.getPlayer().getUniqueId()))
            return;
        var block = event.getClickedBlock();
        if (block == null) return;
        if (!isValidMailbox(block.getState())) return;

        mailboxSettingHoldTaskMap.get(event.getPlayer().getUniqueId()).cancel();
        mailboxSettingHoldTaskMap.remove(event.getPlayer().getUniqueId());
        event.setCancelled(true);

        if (!pluginInstance.economyProvider.withdrawPlayer(event.getPlayer().getUniqueId(), pluginInstance.config.mailerConfig.setMailboxCost)) {
            event.getPlayer().sendMessage(
                    getLanguage().mailerLang.moneyNotEnough.produce(
                            Pair.of("amount", pluginInstance.config.mailerConfig.setMailboxCost),
                            Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural())
                    )
            );
            event.getPlayer().sendMessage(
                    getLanguage().mailerLang.setCancelled.produce()
            );
            return;
        }
        pluginInstance.economyProvider.depositSystemVault(pluginInstance.config.mailerConfig.setMailboxCost);

        try {
            locRecorder.setMailboxLoc(event.getPlayer().getUniqueId(), new MailboxLoc(block.getX(), block.getY(), block.getZ(), block.getWorld().getUID()));
            event.getPlayer().sendMessage(
                    getLanguage().mailerLang.mailboxSet.produce(
                            Pair.of("x", block.getX()),
                            Pair.of("y", block.getY()),
                            Pair.of("z", block.getZ()),
                            Pair.of("world", block.getWorld().getName()),
                            Pair.of("amount", pluginInstance.config.mailerConfig.setMailboxCost),
                            Pair.of("currencyUnit", pluginInstance.economyProvider.currencyNamePlural())
                    )
            );
        } catch (SQLException e) {
            e.printStackTrace();
            event.getPlayer().sendMessage(
                    getLanguage().commonLang.sqlErrorOccurred.produce()
            );
        }
    }


    @Override
    public String getHelp() {
        return getLanguage().mailerLang.mailerHelp.produce();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (disabled || !(sender instanceof Player)) {
            return null;
        }
        if (args.length <= 1) {
            return subCommands;
        } else {
            if (args[0].equalsIgnoreCase("mailbox")) {
                if (args.length == 2) {
                    return List.of("set", "clear", "info");
                } else {
                    return null;
                }
            } else if (args[0].equalsIgnoreCase("sendto")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            } else {
                return null;
            }
        }
    }

    @Override
    public boolean checkPermission(CommandSender commandSender) {
        return commandSender.hasPermission(MAILER_PERMISSION_NODE);
    }

    private boolean isValidMailbox(BlockState blockState) {
        return blockState instanceof Chest || blockState instanceof Barrel || blockState instanceof ShulkerBox;
    }
}
