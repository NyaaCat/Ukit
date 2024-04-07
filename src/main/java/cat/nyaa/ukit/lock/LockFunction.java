package cat.nyaa.ukit.lock;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.EssentialsPluginUtils;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import cat.nyaa.ukit.utils.Utils;
import land.melon.lab.simplelanguageloader.utils.ItemUtils;
import land.melon.lab.simplelanguageloader.utils.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LockFunction implements SubCommandExecutor, SubTabCompleter, Listener {
    private final SpigotLoader pluginInstance;
    private final String LOCK_PERMISSION_NORMAL_NODE = "ukit.lock";
    private final String LOCK_PERMISSION_PRIVILEGE_NODE = "ukit.lock.admin";
    private final NamespacedKey LOCK_METADATA_KEY;
    private final NamespacedKey LEGACY_METADATA_UID = new NamespacedKey("nyaautils", "exhibitionkeyname");
    private final NamespacedKey LEGACY_METADATA_DESC = new NamespacedKey("nyaautils", "exhibitionkeyuid");
    private final NamespacedKey LEGACY_METADATA_NAME = new NamespacedKey("nyaautils", "exhibitionkeydescription");
    private final UUID ZERO_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
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
            } else if (!(lookingEntity instanceof ItemFrame lookingFrame)) {
                commandSender.sendMessage(pluginInstance.language.lockLang.notItemFrame.produce());
                return true;
            } else {
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
                        if (isLegacyLockedFrame(lookingFrame)) {
                            if (!player.getUniqueId().equals(getLegacyLockFrameOwner(lookingFrame)) && !commandSender.hasPermission(LOCK_PERMISSION_PRIVILEGE_NODE)) {
                                commandSender.sendMessage(pluginInstance.language.lockLang.cantOperateOthers.produce());
                            } else {
                                removeLegacyLockedFrame(lookingFrame);
                                commandSender.sendMessage(pluginInstance.language.lockLang.removeSuccessfully.produce());
                            }
                        }
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
                        if(isLegacyLockedFrame(lookingFrame)){
                            commandSender.sendMessage(getLockFrameInfoMessage(getLegacyLockFrameOwner(lookingFrame), lookingFrame.getItem()));
                            return true;
                        }
                        if (!isLockedFrame(lookingFrame)) {
                            commandSender.sendMessage(pluginInstance.language.lockLang.notLockFrame.produce());
                        } else {
                            commandSender.sendMessage(getLockFrameInfoMessage(getLockingOwner(lookingFrame), lookingFrame.getItem()));
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
                                player.sendMessage(getLockFramePropertyMessage(lookingFrame));
                                return true;
                            } else {
                                try {
                                    switch (LockedFrameProperties.valueOf(args[1].toUpperCase())) {
                                        case TRANSPARENT ->
                                                lookingFrame.setVisible(!args[2].equalsIgnoreCase("enable"));
                                        case GLOWING ->
                                                lookingFrame.setGlowing(args[2].equalsIgnoreCase("enable"));
                                    }
                                    player.sendMessage(getLockFramePropertyMessage(lookingFrame));
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

    private Component getLockFramePropertyMessage(ItemFrame frame) {
        var transparent = !frame.isVisible();
        var transparentCmd = "/ukit lock property transparent " + (transparent ? "disable" : "enable");
        var transparentButton = LegacyComponentSerializer.legacySection().deserialize(transparent ? pluginInstance.language.commonLang.buttonOff.colored() : pluginInstance.language.commonLang.buttonOn.colored())
                .hoverEvent(HoverEvent.showText(Component.text(transparentCmd)))
                .clickEvent(ClickEvent.runCommand(transparentCmd));

        var glowing = frame.isGlowing();
        var glowingCmd = "/ukit lock property glowing " + (glowing ? "disable" : "enable");
        var glowingButton = LegacyComponentSerializer.legacySection().deserialize(glowing ? pluginInstance.language.commonLang.buttonOff.colored() : pluginInstance.language.commonLang.buttonOn.colored())
                .hoverEvent(HoverEvent.showText(Component.text(glowingCmd)))
                .clickEvent(ClickEvent.runCommand(glowingCmd));

        return pluginInstance.language.lockLang.lockFrameProperties.produceAsComponent(
                Pair.of("transparent", transparent ? pluginInstance.language.commonLang.textTrue : pluginInstance.language.commonLang.textFalse),
                Pair.of("growing" /*for typo in previous version*/, glowing ? pluginInstance.language.commonLang.textTrue : pluginInstance.language.commonLang.textFalse),
                Pair.of("glowing", glowing ? pluginInstance.language.commonLang.textTrue : pluginInstance.language.commonLang.textFalse),
                Pair.of("toggleButtonTransparent", transparentButton),
                Pair.of("toggleButtonDisplayName", glowingButton)
        );
    }

    private Component getLockFrameInfoMessage(UUID owner, ItemStack itemStack) {
        return pluginInstance.language.lockLang.lockFrameInfo.produceAsComponent(
                Pair.of("owner", EssentialsPluginUtils.nickWithHoverOrNormalName(owner) /*Bukkit.getOfflinePlayer(owner).getName()*/),
                Pair.of("item", ItemUtils.itemTextWithHover(itemStack))
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

    private boolean isLegacyLockedFrame(Entity entity) {
        if (entity == null) return false;
        if (entity.getType() != EntityType.ITEM_FRAME) return false;
        var itemFrame = (ItemFrame) entity;
        var item = itemFrame.getItem();
        if (!item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();

        return pdc.has(LEGACY_METADATA_UID) && itemFrame.isFixed();
    }

    private void removeLegacyLockedFrame(ItemFrame itemFrame) {
        itemFrame.setItem(null);
        itemFrame.setFixed(false);
    }

    private UUID getLegacyLockFrameOwner(ItemFrame itemFrame) {
        if (itemFrame == null)
            return ZERO_UUID;
        var item = itemFrame.getItem();
        if (!item.hasItemMeta())
            return ZERO_UUID;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        var uuidString = pdc.get(LEGACY_METADATA_UID, PersistentDataType.STRING);
        if (uuidString != null)
            return UUID.fromString(uuidString);
        else
            return ZERO_UUID;
    }

    private UUID getLockingOwner(ItemFrame itemFrame) {
        var uuidString = itemFrame.getPersistentDataContainer().get(LOCK_METADATA_KEY, PersistentDataType.STRING);
        if (uuidString != null)
            return UUID.fromString(uuidString);
        else
            return ZERO_UUID;
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


    @EventHandler
    public void onRightClickItemFrame(PlayerInteractEntityEvent event) {
        // right click on book: open book
        // right click on normal item: show an info message
        // right click with sneak: disregard
        if (event.getPlayer().isSneaking()) return;
        var entity = event.getRightClicked();

        if (isLegacyLockedFrame(entity)) {
            event.setCancelled(true);
            var owner = getLegacyLockFrameOwner((ItemFrame) entity);
            var item = ((ItemFrame) entity).getItem();

            if (item.getType() == Material.WRITTEN_BOOK) {
                event.getPlayer().openBook(item);
                return;
            }

            event.getPlayer().sendMessage(getLockFrameInfoMessage(owner, item));
            return;
        }

        if (isLockedFrame(entity)) {
            event.setCancelled(true);
            var owner = getLockingOwner((ItemFrame) entity);
            var item = ((ItemFrame) entity).getItem();

            if (item.getType() == Material.WRITTEN_BOOK) {
                event.getPlayer().openBook(item);
                return;
            }

            event.getPlayer().sendMessage(getLockFrameInfoMessage(owner, item));
        }

    }

    @Override
    public boolean checkPermission(CommandSender commandSender) {
        return commandSender.hasPermission(LOCK_PERMISSION_NORMAL_NODE) || commandSender.hasPermission(LOCK_PERMISSION_PRIVILEGE_NODE);
    }

    enum LockedFrameProperties {
        TRANSPARENT, GLOWING
    }
}
