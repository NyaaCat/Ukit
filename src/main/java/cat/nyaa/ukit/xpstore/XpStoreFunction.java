package cat.nyaa.ukit.xpstore;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.ExperienceUtils;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import cat.nyaa.ukit.utils.Utils;
import land.melon.lab.simplelanguageloader.utils.ItemUtils;
import land.melon.lab.simplelanguageloader.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class XpStoreFunction implements SubCommandExecutor, SubTabCompleter, Listener {
    private final SpigotLoader pluginInstance;
    private final NamespacedKey EXPAmountKey;
    private final NamespacedKey AmountLoreIndexKey;
    private final NamespacedKey QuickTakePreferenceKey;
    private final NamespacedKey QuickTakeLoreIndexKey;
    private final NamespacedKey ThrownExpAmountKey;
    private final String EXPBOTTLE_PERMISSION_NODE = "ukit.xpstore";
    private final Map<UUID, Long> quickTakeArmMap = new HashMap<>();
    private final List<String> subCommands = List.of("store", "take", "quicktake");

    public XpStoreFunction(SpigotLoader pluginInstance) {
        this.pluginInstance = pluginInstance;
        EXPAmountKey = new NamespacedKey(pluginInstance, "EXP_AMOUNT");
        AmountLoreIndexKey = new NamespacedKey(pluginInstance, "LORE_LINE_INDEX");
        QuickTakeLoreIndexKey = new NamespacedKey(pluginInstance, "QUICK_TAKE_LORE_INDEX");
        QuickTakePreferenceKey = new NamespacedKey(pluginInstance, "QUICK_TAKE_AMOUNT");
        ThrownExpAmountKey = new NamespacedKey(pluginInstance, "THROWN_EXP_AMOUNT");
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        //ukit xpstore store <amount>
        //ukit xpstore takeexp <amount>
        if (!(commandSender instanceof Player senderPlayer)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.playerOnlyCommand.produce());
            return true;
        }
        if (!checkPermission(commandSender)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.permissionDenied.produce());
            return true;
        }
        if (args.length < 2 || !subCommands.contains(args[0].toLowerCase())) {
            return false;
        }
        var itemInHandPair = Utils.getItemInHand(senderPlayer);
        if (itemInHandPair == null) {
            senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.noItemInHand.produce());
            return true;
        } else if (itemInHandPair.value().getType() != Material.EXPERIENCE_BOTTLE) {
            senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.notExpBottle.produceAsComponent(
                    Pair.of("item", ItemUtils.itemTextWithHover(itemInHandPair.value()))
            ));
            return true;
        }
        var config = pluginInstance.config.xpStoreConfig;
        var itemSlot = itemInHandPair.key();
        var itemInHand = itemInHandPair.value();
        var amountItem = itemInHand.getAmount();
        var expInItem = getExpContained(itemInHand);
        int amountInput;
        try {
            amountInput = Integer.parseInt(args[1]);
            if (amountInput < 0)
                throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.notValidAmount.produce(Pair.of("input", args[1])));
            return true;
        }
        if (args[0].equalsIgnoreCase("store")) {
            var expTotal = amountItem * amountInput;
            if (ExperienceUtils.getExpPoints(senderPlayer) < expTotal) {
                senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.notEnoughExp.produce(
                        Pair.of("expTotal", expTotal),
                        Pair.of("expPerBottle", amountInput),
                        Pair.of("amount", amountItem)
                ));
                return true;
            }
            if (amountInput + expInItem > config.maxAmount) {
                senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.maximumExceed.produce(
                        Pair.of("maximum", config.maxAmount)
                ));
                return true;
            }
            var itemSaved = addExpToItemStack(itemInHand, amountInput);
            Utils.setItemInHand(senderPlayer, Pair.of(itemSlot, itemSaved));
            ExperienceUtils.subtractExpPoints(senderPlayer, expTotal);
            senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.expSaved.produce(
                    Pair.of("amount", expTotal)
            ));
        } else if (args[0].equalsIgnoreCase("take")) {
            var expTotal = getMinimumDivisible(amountInput, amountItem);
            var amountAverage = expTotal / amountItem;
            var amountContained = getExpContained(itemInHand);
            if (amountContained < amountAverage) {
                senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.notEnoughExpInBottle.produce(
                        Pair.of("amount", amountAverage)
                ));
                return true;
            }
            var itemSaved = addExpToItemStack(itemInHand, -amountAverage);
            Utils.setItemInHand(senderPlayer, Pair.of(itemSlot, itemSaved));
            ExperienceUtils.addPlayerExperience(senderPlayer, expTotal, true);
            senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.expTook.produce(
                    Pair.of("amount", expTotal),
                    Pair.of("remaining", getExpContained(itemSaved))
            ));
        } else if (args[0].equalsIgnoreCase("quicktake")) {
            if (!isExpContainer(itemInHand)) {
                addExpToItemStack(itemInHand, 0);
            }
            var itemSaved = updateQuickTakePreference(itemInHand, amountInput);
            Utils.setItemInHand(senderPlayer, Pair.of(itemSlot, itemSaved));
            senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.quickTakePrefUpdated.produce(
                    Pair.of("amount", amountInput)
            ));
        }
        return true;
    }

    private int getMinimumDivisible(int amountExpect, int factor) {
        if (amountExpect % factor == 0)
            return amountExpect;
        else
            return amountExpect - (amountExpect % factor) + factor;
    }

    @Override
    public String getHelp() {
        return pluginInstance.language.xpStoreLang.help.produce();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player senderPlayer)) {
            return List.of();
        }
        if (args.length < 2) {
            return subCommands.stream().filter(t -> t.startsWith(args[0].toLowerCase())).toList();
        } else if (args.length == 2) {
            var item = Utils.getItemInHand(senderPlayer, Material.EXPERIENCE_BOTTLE);
            if (args[0].equalsIgnoreCase("store")) {
                if (item == null)
                    return List.of(pluginInstance.language.xpStoreLang.noExpBottleInHandTabNotice.produce());
                else
                    return List.of(String.valueOf(ExperienceUtils.getExpPoints(senderPlayer) / item.value().getAmount()));
            } else if (args[0].equalsIgnoreCase("take")) {
                if (item == null)
                    return List.of(pluginInstance.language.xpStoreLang.noExpBottleInHandTabNotice.produce());
                else
                    return List.of(String.valueOf(getExpContained(item.value()) * item.value().getAmount()));
            } else if (args[0].equalsIgnoreCase("quicktake")) {
                return List.of("<amount>");
            } else {
                return List.of();
            }
        } else {
            return List.of();
        }
    }

    @Override
    public boolean checkPermission(CommandSender commandSender) {
        return commandSender.hasPermission(EXPBOTTLE_PERMISSION_NODE);
    }

    private boolean isExpContainer(ItemStack itemStack) {
        if (!itemStack.hasItemMeta())
            return false;
        return itemStack.getItemMeta().getPersistentDataContainer().has(EXPAmountKey, PersistentDataType.INTEGER);
    }

    private int getExpContained(ItemStack itemStack) {
        if (!isExpContainer(itemStack)) {
            return 0;
        } else {
            return itemStack.getItemMeta().getPersistentDataContainer().get(EXPAmountKey, PersistentDataType.INTEGER);
        }
    }

    private ItemStack addExpToItemStack(ItemStack itemStack, int amount) {
        var itemMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        if (!isExpContainer(itemStack)) {
            itemMeta.getPersistentDataContainer().set(EXPAmountKey, PersistentDataType.INTEGER, amount);
        } else {
            var expAlready = itemMeta.getPersistentDataContainer().get(EXPAmountKey, PersistentDataType.INTEGER);
            itemMeta.getPersistentDataContainer().set(EXPAmountKey, PersistentDataType.INTEGER, expAlready + amount);
        }
        itemStack.setItemMeta(updateAmountLore(itemMeta));
        return itemStack;
    }

    private ItemMeta updateAmountLore(ItemMeta itemMeta) {
        var loreIndex = itemMeta.getPersistentDataContainer().get(AmountLoreIndexKey, PersistentDataType.INTEGER);
        var amount = itemMeta.getPersistentDataContainer().get(EXPAmountKey, PersistentDataType.INTEGER);
        var lore = itemMeta.getLore();
        if (lore == null)
            lore = new ArrayList<>();
        var expAmountText = pluginInstance.language.xpStoreLang.loreTextPattern.produce(Pair.of("amount", amount));

        if (loreIndex == null) {
            //first time startup
            lore.add(expAmountText);
            loreIndex = lore.size() - 1;
        } else {
            //already an exp bottle
            if (loreIndex > lore.size() - 1) {
                //index out of bound
                lore.add(expAmountText);
                loreIndex = lore.size() - 1;
            } else {
                //index in bound
                lore.set(loreIndex, expAmountText);
            }
        }
        itemMeta.getPersistentDataContainer().set(AmountLoreIndexKey, PersistentDataType.INTEGER, loreIndex);
        itemMeta.setLore(lore);
        return itemMeta;
    }

    private ItemMeta updateQuickTakePreferenceLore(ItemMeta itemMeta) {
        var loreIndex = itemMeta.getPersistentDataContainer().get(QuickTakeLoreIndexKey, PersistentDataType.INTEGER);
        var amount = itemMeta.getPersistentDataContainer().get(QuickTakePreferenceKey, PersistentDataType.INTEGER);
        var lore = itemMeta.getLore();
        if (lore == null)
            lore = new ArrayList<>();
        var expAmountText = pluginInstance.language.xpStoreLang.loreQuickTakePattern.produce(Pair.of("amount", amount));

        if (loreIndex == null) {
            //first time startup
            lore.add(expAmountText);
            loreIndex = lore.size() - 1;
        } else {
            //already an exp bottle
            if (loreIndex > lore.size() - 1) {
                //index out of bound
                lore.add(expAmountText);
                loreIndex = lore.size() - 1;
            } else {
                //index in bound
                lore.set(loreIndex, expAmountText);
            }
        }
        itemMeta.getPersistentDataContainer().set(QuickTakeLoreIndexKey, PersistentDataType.INTEGER, loreIndex);
        itemMeta.setLore(lore);
        return itemMeta;
    }


    private ItemStack updateQuickTakePreference(ItemStack itemStack, int amount) {
        // require isExpContainer
        var itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(QuickTakePreferenceKey, PersistentDataType.INTEGER, amount);
        itemStack.setItemMeta(updateQuickTakePreferenceLore(itemMeta));
        return itemStack;
    }

    private Integer getQuickTakePreference(ItemStack itemStack) {
        var meta = itemStack.getItemMeta();
        if (meta == null) return null;
        else
            return meta.getPersistentDataContainer().get(QuickTakePreferenceKey, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onExpBottleLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof ThrownExpBottle thrownExpBottle)) return;
        var item = thrownExpBottle.getItem();
        if (!isExpContainer(item)) return;
        int expAmount = getExpContained(item);
        if (expAmount <= 0) return;
        thrownExpBottle.getPersistentDataContainer().set(ThrownExpAmountKey, PersistentDataType.INTEGER, expAmount);
    }

    @EventHandler
    public void onExpBottleHit(ExpBottleEvent event) {
        ThrownExpBottle thrownExpBottle = event.getEntity();
        Integer expAmount = thrownExpBottle.getPersistentDataContainer().get(ThrownExpAmountKey, PersistentDataType.INTEGER);
        if (expAmount == null) {
            var item = thrownExpBottle.getItem();
            if (!isExpContainer(item)) return;
            expAmount = getExpContained(item);
        }
        if (expAmount <= 0) return;
        event.setExperience(0);
        ExperienceUtils.splashExp(expAmount, thrownExpBottle.getLocation());
        thrownExpBottle.getPersistentDataContainer().remove(ThrownExpAmountKey);
    }

    @EventHandler
    public void onRightClickBottle(PlayerInteractEvent event) {
        if (!pluginInstance.config.xpStoreConfig.enableQuickTake) return;
        if (event.getPlayer().isSneaking()) return;
        if (!List.of(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR).contains(event.getAction()))
            return;
        var item = event.getItem();
        if (item == null) return;
        if (!isExpContainer(item)) return;
        event.setCancelled(true);

        var itemAmount = item.getAmount();
        var amountContain = getExpContained(item);
        if (amountContain == 0) return;

        var timeNow = System.currentTimeMillis();
        var expireTime = quickTakeArmMap.getOrDefault(event.getPlayer().getUniqueId(), 0L);
        quickTakeArmMap.put(event.getPlayer().getUniqueId(), timeNow + pluginInstance.config.xpStoreConfig.quickTakeArmTimeInMillisecond);
        if (timeNow > expireTime) {
            event.getPlayer().sendActionBar(pluginInstance.language.xpStoreLang.quickTakeArmed.produce());
            return;
        }

        // amountContain * quickTakeRatio or quickTakeMinimumAmount if amountContain enough, or take all at once
        var amountPreference = Objects.requireNonNullElse(getQuickTakePreference(item), getDefaultMinimumTakeAmount(amountContain));
        var amountTake = Math.min(amountContain, amountPreference);
        if (amountTake == 0) return;
        addExpToItemStack(item, -amountTake);
        Utils.setItemInHand(event.getPlayer(), Pair.of(event.getHand(), item));
        var amountTotal = itemAmount * amountTake;
        ExperienceUtils.splashExp(amountTotal, event.getPlayer().getLocation());

        event.getPlayer().sendActionBar(pluginInstance.language.xpStoreLang.quickTakeNotice.produce(
                Pair.of("amount", amountTotal),
                Pair.of("remaining", getExpContained(item))
        ));
    }

    // clean up
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        quickTakeArmMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerSwitchItem(PlayerItemHeldEvent event) {
        quickTakeArmMap.remove(event.getPlayer().getUniqueId());
    }

    private int getDefaultMinimumTakeAmount(int amountContain) {
        // amountContain * quickTakeRatio or quickTakeMinimumAmount
        var quickTakeRatio = pluginInstance.config.xpStoreConfig.quickTakeRatio;
        var quickTakeMinimumAmount = pluginInstance.config.xpStoreConfig.quickTakeMinimumAmount;
        return Math.max((int) (amountContain * quickTakeRatio), quickTakeMinimumAmount);
    }
}
