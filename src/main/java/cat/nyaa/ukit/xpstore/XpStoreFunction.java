package cat.nyaa.ukit.xpstore;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.ExperienceUtils;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import cat.nyaa.ukit.utils.Utils;
import land.melon.lab.simplelanguageloader.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class XpStoreFunction implements SubCommandExecutor, SubTabCompleter, Listener {
    private final SpigotLoader pluginInstance;
    private final NamespacedKey EXPAmountKey;
    private final NamespacedKey LoreLineIndexKey;
    private final String EXPBOTTLE_PERMISSION_NODE = "ukit.xpstore";
    private final Map<UUID, Integer> playerExpBottleMap = new HashMap<>();
    private final List<String> subCommands = List.of("store", "take", "set");

    public XpStoreFunction(SpigotLoader pluginInstance) {
        this.pluginInstance = pluginInstance;
        EXPAmountKey = new NamespacedKey(pluginInstance, "EXP_AMOUNT");
        LoreLineIndexKey = new NamespacedKey(pluginInstance, "LORE_LINE_INDEX");
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
            senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.notExpBottle.produce());
            return true;
        }
        var config = pluginInstance.config.xpStoreConfig;
        var itemSlot = itemInHandPair.key();
        var itemInHand = itemInHandPair.value();
        var amountItem = itemInHand.getAmount();
        int amountInput;
        try {
            if(args[1].toLowerCase().charAt(args[1].length() - 1) == 'l') {
                amountInput = ExperienceUtils.getExpForLevel(Integer.parseInt(args[1].substring(0, args[1].length() - 1)));
            } else {
                amountInput = Integer.parseInt(args[1]);
            }
            if (amountInput < 0 || amountInput > config.maxAmount)
                throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.notValidAmount.produce(Pair.of("input", args[1])));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "store" -> {
                int expTotal = amountItem * amountInput;
                if (ExperienceUtils.getExpPoints(senderPlayer) < expTotal) {
                    senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.notEnoughExp.produce(Pair.of("expTotal", expTotal), Pair.of("expPerBottle", amountInput), Pair.of("amount", amountItem)));
                    return true;
                }
                ItemStack itemSaved = addExpToItemStack(itemInHand, amountInput);
                Utils.setItemInHand(senderPlayer, Pair.of(itemSlot, itemSaved));
                ExperienceUtils.subtractPlayerExpPoints(senderPlayer, expTotal);
                senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.expSaved.produce(Pair.of("amount", expTotal)));
            }
            case "set" -> {
                int expMovedTotal = getMinimumDivisible(amountInput - ExperienceUtils.getExpPoints(senderPlayer), amountItem);
                int amountMovedAverage = expMovedTotal / amountItem;
                int amountRemaining = getExpContained(itemInHand) - amountMovedAverage;
                if(amountRemaining < 0) {
                    senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.notEnoughExpInBottle.produce(Pair.of("amount", amountMovedAverage)));
                    return true;
                }
                ItemStack itemSaved = addExpToItemStack(itemInHand, -amountMovedAverage);
                Utils.setItemInHand(senderPlayer, Pair.of(itemSlot, itemSaved));
                ExperienceUtils.addPlayerExpPoints(senderPlayer, expMovedTotal);
                if(expMovedTotal <= 0) {
                    senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.expSaved.produce(Pair.of("amount", -expMovedTotal)));
                } else {
                    senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.expTook.produce(Pair.of("amount", expMovedTotal)));
                }
            }
            case "take" -> {
                int expTotal = getMinimumDivisible(amountInput, amountItem);
                int amountAverage = expTotal / amountItem;
                int amountContained = getExpContained(itemInHand);
                if (amountContained < amountAverage) {
                    senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.notEnoughExpInBottle.produce(Pair.of("amount", amountAverage)));
                    return true;
                }
                ItemStack itemSaved = addExpToItemStack(itemInHand, -amountAverage);
                Utils.setItemInHand(senderPlayer, Pair.of(itemSlot, itemSaved));
                ExperienceUtils.addPlayerExpPoints(senderPlayer, expTotal);
                senderPlayer.sendMessage(pluginInstance.language.xpStoreLang.expTook.produce(Pair.of("amount", expTotal)));
            }
        }
        return true;
    }

    /**
     * $ \frac{1}{2} $
     * @param amountExpect
     * @param factor
     * @return
     */
    private int getMinimumDivisible(int amountExpect, int factor) {
        assert factor > 0;
        if (amountExpect % factor == 0) {
            return amountExpect;
        } else {
            if(amountExpect > 0) {
                return amountExpect - (amountExpect % factor) + factor;
            } else {
                return amountExpect + (amountExpect % factor) + factor;
            }
        }
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
            Pair<EquipmentSlot, ItemStack> item = Utils.getItemInHand(senderPlayer, Material.EXPERIENCE_BOTTLE);
            switch (args[0].toLowerCase()) {
                case "store"-> {
                    if (item == null) {
                        return List.of(pluginInstance.language.xpStoreLang.noExpBottleInHandTabNotice.produce());
                    } else {
                        return List.of(String.valueOf(ExperienceUtils.getExpPoints(senderPlayer) / item.value().getAmount()));
                    }
                }
                case "take" -> {
                    if (item == null) {
                        return List.of(pluginInstance.language.xpStoreLang.noExpBottleInHandTabNotice.produce());
                    } else {
                        return List.of(String.valueOf(getExpContained(item.value()) * item.value().getAmount()));
                    }
                }
                case "set" -> {
                    if (item == null) {
                        return List.of(pluginInstance.language.xpStoreLang.noExpBottleInHandTabNotice.produce());
                    } else {
                        return List.of("1L", "30L");
                    }
                }
                default -> {
                    return List.of();
                }
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

    private boolean isExpContainer(Entity entity) {
        return entity.getPersistentDataContainer().has(EXPAmountKey, PersistentDataType.INTEGER);
    }

    private int getExpContained(ItemStack itemStack) {
        if (!isExpContainer(itemStack)) {
            return 0;
        } else {
            return itemStack.getItemMeta().getPersistentDataContainer().get(EXPAmountKey, PersistentDataType.INTEGER);
        }
    }

    private int getExpContained(Entity entity) {
        if (!isExpContainer(entity))
            return 0;
        else
            return entity.getPersistentDataContainer().get(EXPAmountKey, PersistentDataType.INTEGER);
    }

    private ItemStack addExpToItemStack(ItemStack itemStack, int amount) {
        var itemMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        assert itemMeta != null;
        if (!isExpContainer(itemStack)) {
            itemMeta.getPersistentDataContainer().set(EXPAmountKey, PersistentDataType.INTEGER, amount);
        } else {
            var expAlready = itemMeta.getPersistentDataContainer().get(EXPAmountKey, PersistentDataType.INTEGER);
            itemMeta.getPersistentDataContainer().set(EXPAmountKey, PersistentDataType.INTEGER, expAlready + amount);
        }
        itemStack.setItemMeta(updateLore(itemMeta));
        return itemStack;
    }

    private void addExpToEntity(Entity entity, int amount) {
        if (!isExpContainer(entity)) {
            entity.getPersistentDataContainer().set(EXPAmountKey, PersistentDataType.INTEGER, amount);
        } else {
            entity.getPersistentDataContainer().set(EXPAmountKey, PersistentDataType.INTEGER,
                    entity.getPersistentDataContainer().get(EXPAmountKey, PersistentDataType.INTEGER) + amount
            );
        }
    }

    private ItemMeta updateLore(ItemMeta itemMeta) {
        var loreIndex = itemMeta.getPersistentDataContainer().get(LoreLineIndexKey, PersistentDataType.INTEGER);
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
        itemMeta.getPersistentDataContainer().set(LoreLineIndexKey, PersistentDataType.INTEGER, loreIndex);
        itemMeta.setLore(lore);
        return itemMeta;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractWithExpBottle(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (event.getItem() == null)
            return;
        playerExpBottleMap.put(event.getPlayer().getUniqueId(), getExpContained(event.getItem()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onThrewExpBottleLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooterPlayer))
            return;
        if (event.getEntity().getType() != EntityType.THROWN_EXP_BOTTLE)
            return;
        if (!playerExpBottleMap.containsKey(shooterPlayer.getUniqueId()))
            return;
        var amount = playerExpBottleMap.remove(shooterPlayer.getUniqueId());
        addExpToEntity(event.getEntity(), amount);
    }

    @EventHandler(ignoreCancelled = true)
    public void onExpBottleHitGround(ExpBottleEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player))
            return;
        var amount = getExpContained(event.getEntity());
        event.setExperience(event.getExperience() + amount);
    }
}
