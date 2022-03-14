package cat.nyaa.ukit.lock;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import cat.nyaa.ukit.utils.Utils;
import land.melon.lab.simplelanguageloader.nms.LocaleUtils;
import land.melon.lab.simplelanguageloader.utils.Pair;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LockFunction implements SubCommandExecutor, SubTabCompleter {
    private final SpigotLoader pluginInstance;
    private final String LOCK_PERMISSION_NORMAL_NODE = "ukit.lock";
    private final String LOCK_PERMISSION_PRIVILEGE_NODE = "ukit.lock.admin";
    private final NamespacedKey LOCK_METADATA_KEY;
    private final List<String> subCommands = List.of("info", "setup", "remove", "property");

    public LockFunction(SpigotLoader pluginInstance) {
        this.pluginInstance = pluginInstance;
        LOCK_METADATA_KEY = new NamespacedKey(pluginInstance, "LOCK_OWNER");
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.playerOnlyCommand.produce());
            return true;
        } else if (!commandSender.hasPermission(LOCK_PERMISSION_NORMAL_NODE) && !commandSender.hasPermission(LOCK_PERMISSION_PRIVILEGE_NODE)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.permissionDenied.produce());
            return true;
        } else if (args.length < 1 || !subCommands.contains(args[0].toLowerCase())) {
            return false;
        } else {
            Player player = (Player) commandSender;
            var lookingEntity = Utils.getEntityLookingAt(player, entity -> entity instanceof ItemFrame);
            if (lookingEntity == null) {
                commandSender.sendMessage(pluginInstance.language.lockLang.noEntityFound.produce());
                return true;
            } else if (lookingEntity.getType() != EntityType.ITEM_FRAME) {
                commandSender.sendMessage(pluginInstance.language.lockLang.notItemFrame.produce());
                return true;
            } else {
                var lookingFrame = (ItemFrame) lookingEntity;
                switch (args[0]) {
                    case "setup":
                        if (isLockedFrame(lookingFrame)) {
                            commandSender.sendMessage(pluginInstance.language.lockLang.alreadyLockedFrame.produce());
                        } else if (lookingFrame.getItem().getType().isAir()) {
                            commandSender.sendMessage(pluginInstance.language.lockLang.emptyFrameNotAllowed.produce());
                        } else {
                            setUpLockFrame(lookingFrame, player.getUniqueId());
                            commandSender.sendMessage(pluginInstance.language.lockLang.setupSuccessfully.produce());
                        }
                        return true;
                    case "remove":
                        if (!isLockedFrame(lookingFrame)) {
                            commandSender.sendMessage(pluginInstance.language.lockLang.notLockFrame.produce());
                        } else if (!getLockingOwner(lookingFrame).equals(player.getUniqueId()) && !commandSender.hasPermission(LOCK_PERMISSION_PRIVILEGE_NODE)) {
                            commandSender.sendMessage(pluginInstance.language.lockLang.cantOperateOthers.produce());
                        } else {
                            removeLockedFrame(lookingFrame);
                            commandSender.sendMessage(pluginInstance.language.lockLang.removeSuccessfully.produce());
                        }
                        return true;
                    case "info":
                        if (!isLockedFrame(lookingFrame)) {
                            commandSender.sendMessage(pluginInstance.language.lockLang.notLockFrame.produce());
                        } else {
                            commandSender.sendMessage(pluginInstance.language.lockLang.lockFrameInfo.produce(
                                    Pair.of("player", Bukkit.getOfflinePlayer(getLockingOwner(lookingFrame)).getName()),
                                    Pair.of("item", LocaleUtils.getTranslatableItemComponent(lookingFrame.getItem()))
                            ));
                        }
                        return true;
                    case "property":
                        if (args.length == 2) {
                            return false;
                        } else if (!isLockedFrame(lookingFrame)) {
                            commandSender.sendMessage(pluginInstance.language.lockLang.notLockFrame.produce());
                            return true;
                        } else if (!getLockingOwner(lookingFrame).equals(player.getUniqueId()) && !commandSender.hasPermission(LOCK_PERMISSION_PRIVILEGE_NODE)) {
                            commandSender.sendMessage(pluginInstance.language.lockLang.cantOperateOthers.produce());
                            return true;
                        } else {
                            if (args.length == 1) {
                                player.spigot().sendMessage(getLockFramePropertyMessage(lookingFrame));
                                return true;
                            } else {
                                try {
                                    switch (LockedFrameProperties.valueOf(args[1].toUpperCase())) {
                                        case TRANSPARENT -> lookingFrame.setVisible(!args[2].equalsIgnoreCase("enable"));
                                        case GROWING -> lookingFrame.setGlowing(args[2].equalsIgnoreCase("enable"));
                                    }
                                    player.spigot().sendMessage(getLockFramePropertyMessage(lookingFrame));
                                    return true;
                                } catch (IllegalArgumentException e) {
                                    commandSender.sendMessage(pluginInstance.language.lockLang.invalidProperty.produce(
                                            Pair.of("property", args[1])
                                    ));
                                    return false;
                                }
                            }
                        }
                    default:
                        return false;
                }
            }
        }
    }

    private BaseComponent[] getLockFramePropertyMessage(ItemFrame frame) {
        var transparent = !frame.isVisible();
        var growing = frame.isGlowing();
        var transparentButton = new TextComponent(TextComponent.fromLegacyText(transparent ? pluginInstance.language.commonLang.buttonOff.colored() : pluginInstance.language.commonLang.buttonOn.colored()));
        var transparentCmd = "/ukit lock property transparent " + (transparent ? "disable" : "enable");
        var growingCmd = "/ukit lock property growing " + (growing ? "disable" : "enable");
        transparentButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(transparentCmd)));
        transparentButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, transparentCmd));
        var growingButton = new TextComponent(TextComponent.fromLegacyText(growing ? pluginInstance.language.commonLang.buttonOff.colored() : pluginInstance.language.commonLang.buttonOn.colored()));
        growingButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(growingCmd)));
        growingButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, growingCmd));

        return pluginInstance.language.lockLang.lockFrameProperties.produceWithBaseComponent(
                Pair.of("transparent", transparent ? pluginInstance.language.commonLang.textTrue : pluginInstance.language.commonLang.textFalse),
                Pair.of("growing", growing ? pluginInstance.language.commonLang.textTrue : pluginInstance.language.commonLang.textFalse),
                Pair.of("toggleButtonTransparent", transparentButton),
                Pair.of("toggleButtonDisplayName", growingButton)
        );

    }

    private void setUpLockFrame(ItemFrame itemFrame, UUID owner) {
        itemFrame.setInvulnerable(true);
        itemFrame.setItemDropChance(0);
        itemFrame.getPersistentDataContainer().set(LOCK_METADATA_KEY, PersistentDataType.STRING, owner.toString());
        itemFrame.getWorld().dropItem(itemFrame.getLocation(), itemFrame.getItem());
    }

    private void removeLockedFrame(ItemFrame itemFrame) {
        itemFrame.setItem(null);
        itemFrame.setInvulnerable(false);
        itemFrame.setItemDropChance(1);
        itemFrame.setVisible(true);
        itemFrame.setGlowing(false);
        itemFrame.getPersistentDataContainer().remove(LOCK_METADATA_KEY);
    }

    private boolean isLockedFrame(Entity entity) {
        if (entity == null) return false;
        return entity.getPersistentDataContainer().has(LOCK_METADATA_KEY, PersistentDataType.STRING);
    }

    private UUID getLockingOwner(ItemFrame itemFrame) {
        try {
            return UUID.fromString(itemFrame.getPersistentDataContainer().get(LOCK_METADATA_KEY, PersistentDataType.STRING));
        } catch (NullPointerException | IllegalArgumentException i) {
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
    }

    @Override
    public String getHelp() {
        return pluginInstance.language.lockLang.help.colored();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        var properties = Arrays.stream(LockedFrameProperties.values()).map(t -> t.name().toLowerCase()).toList();
        if (args.length == 0) {
            return subCommands;
        } else if (args.length == 1) {
            return subCommands.stream().filter(t -> t.startsWith(args[0])).toList();
        } else {
            if (args[0].equalsIgnoreCase("property")) {
                if (args.length == 2) {
                    return properties.stream().filter(t -> t.startsWith(args[1])).toList();
                } else if (args.length == 3) {
                    return List.of("enable", "disable");
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    enum LockedFrameProperties {
        TRANSPARENT, GROWING
    }
}
