package cat.nyaa.ukit;

import cat.nyaa.ecore.EconomyCore;
import cat.nyaa.ukit.chat.ChatFunction;
import cat.nyaa.ukit.item.ItemFunction;
import cat.nyaa.ukit.lock.LockFunction;
import cat.nyaa.ukit.redbag.RedbagFunction;
import cat.nyaa.ukit.show.ShowFunction;
import cat.nyaa.ukit.signedit.SignEditFunction;
import cat.nyaa.ukit.sit.SitConfig;
import cat.nyaa.ukit.sit.SitFunction;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.xpstore.XpStoreFunction;
import com.google.gson.GsonBuilder;
import land.melon.lab.simplelanguageloader.SimpleLanguageLoader;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class SpigotLoader extends JavaPlugin implements TabExecutor {
    public static Logger logger = null;
    private final SimpleLanguageLoader configLoader = new SimpleLanguageLoader(
            new GsonBuilder().registerTypeAdapter(SitConfig.class, SitConfig.serializer), true);
    private final String RELOAD_PERMISSION_NODE = "ukit.reload";
    public MainLang language;
    public MainConfig config;
    public File languageFile = new File(getDataFolder(), "language.json");
    public File configFile = new File(getDataFolder(), "config.json");
    public Chat chatProvider = null;
    public EconomyCore economyProvider = null;
    private SitFunction sitFunction;
    private ShowFunction showFunction;
    private SignEditFunction signEditFunction;
    private LockFunction lockFunction;
    private ChatFunction chatFunction;
    private RedbagFunction redbagFunction;
    private ItemFunction itemFunction;
    private XpStoreFunction xpStoreFunction;

    @Override
    public void onEnable() {
        IGNORE_RESULT(getDataFolder().mkdir());
        logger = this.getLogger();
        if (!reload()) {
            getLogger().severe("Failed to load configs, disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getServer().getPluginCommand("ukit").setExecutor(this);
        this.getServer().getPluginCommand("ukit").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        getServer().getGlobalRegionScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
        if (redbagFunction != null) {
            redbagFunction.refundAll();
        }
    }

    public boolean reload() {
        //reset
        onDisable();

        //setup chat & economy
        IGNORE_RESULT(setupEconomy() && setupChat());

        //setup components
        sitFunction = new SitFunction(this);
        showFunction = new ShowFunction(this);
        signEditFunction = new SignEditFunction(this);
        lockFunction = new LockFunction(this);
        chatFunction = new ChatFunction(this);
        redbagFunction = new RedbagFunction(this);
        itemFunction = new ItemFunction(this);
        xpStoreFunction = new XpStoreFunction(this);


        //event handlers
        getServer().getPluginManager().registerEvents(sitFunction, this);
        getServer().getPluginManager().registerEvents(redbagFunction, this);
        getServer().getPluginManager().registerEvents(xpStoreFunction, this);
        getServer().getPluginManager().registerEvents(chatFunction, this);
        //reload config
        try {
            config = configLoader.loadOrInitialize(configFile, MainConfig.class, MainConfig::new);
            language = configLoader.loadOrInitialize(languageFile, MainLang.class, MainLang::new);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(language.commonLang.helpMessage.produce());
            return true;
        }
        SubCommands subCommands;
        try {
            subCommands = SubCommands.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(language.commonLang.helpMessage.produce());
            return true;
        }
        var argTruncated = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
        switch (subCommands) {
            case RELOAD -> {
                if (!sender.hasPermission(RELOAD_PERMISSION_NODE)) {
                    sender.sendMessage(language.commonLang.permissionDenied.produce());
                } else {
                    reload();
                    sender.sendMessage(language.commonLang.reloadSuccess.produce());
                }
            }
            case SHOW ->
                    invokeCommand(showFunction, sender, command, label, argTruncated);

            case SIT ->
                    invokeCommand(sitFunction, sender, command, label, argTruncated);

            case SIGNEDIT ->
                    invokeCommand(signEditFunction, sender, command, label, argTruncated);

            case LOCK ->
                    invokeCommand(lockFunction, sender, command, label, argTruncated);

            case CHAT ->
                    invokeCommand(chatFunction, sender, command, label, argTruncated);

            case REDBAG ->
                    invokeCommand(redbagFunction, sender, command, label, argTruncated);

            case ITEM ->
                    invokeCommand(itemFunction, sender, command, label, argTruncated);

            case XP ->
                    invokeCommand(xpStoreFunction, sender, command, label, argTruncated);

        }
        return true;
    }

    private void invokeCommand(SubCommandExecutor subCommandExecutor, CommandSender sender, Command command, String label, String[] args) {
        if (!subCommandExecutor.invokeCommand(sender, command, label, args)) {
            sender.sendMessage(subCommandExecutor.getHelp());
        }
    }

    private boolean hasPermission(SubCommands subCommand, CommandSender commandSender) {
        return switch (subCommand) {
            case REDBAG ->
                    redbagFunction != null && redbagFunction.checkPermission(commandSender);
            case SIT ->
                    sitFunction != null && sitFunction.checkPermission(commandSender);
            case CHAT ->
                    chatFunction != null && chatFunction.checkPermission(commandSender);
            case LOCK ->
                    lockFunction != null && lockFunction.checkPermission(commandSender);
            case SHOW ->
                    showFunction != null && showFunction.checkPermission(commandSender);
            case SIGNEDIT ->
                    signEditFunction != null && signEditFunction.checkPermission(commandSender);
            case ITEM ->
                    itemFunction != null && itemFunction.checkPermission(commandSender);
            case XP ->
                    xpStoreFunction != null && xpStoreFunction.checkPermission(commandSender);
            case RELOAD -> commandSender.hasPermission(RELOAD_PERMISSION_NODE);
        };
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, @Nonnull String[] args) {
        var subCommandList = Arrays.stream(SubCommands.values()).filter(t -> hasPermission(t, sender)).map((v) -> v.name().toLowerCase()).toList();
        List<String> completeList = new ArrayList<>();
        if (args.length < 2) {
            return subCommandList.stream().filter(t -> t.startsWith(args[0].toLowerCase())).toList();
        } else {
            try {
                var argsTruncated = Arrays.copyOfRange(args, 1, args.length);
                var subCommand = SubCommands.valueOf(args[0].toUpperCase());
                if (!hasPermission(subCommand, sender)) return completeList;
                switch (subCommand) {
                    case SHOW ->
                            completeList = showFunction.tabComplete(sender, command, alias, argsTruncated);
                    case SIT ->
                            completeList = sitFunction.tabComplete(sender, command, alias, argsTruncated);
                    case SIGNEDIT ->
                            completeList = signEditFunction.tabComplete(sender, command, alias, argsTruncated);
                    case LOCK ->
                            completeList = lockFunction.tabComplete(sender, command, alias, argsTruncated);
                    case CHAT ->
                            completeList = chatFunction.tabComplete(sender, command, alias, argsTruncated);
                    case REDBAG ->
                            completeList = redbagFunction.tabComplete(sender, command, alias, argsTruncated);
                    case ITEM ->
                            completeList = itemFunction.tabComplete(sender, command, alias, argsTruncated);
                    case XP ->
                            completeList = xpStoreFunction.tabComplete(sender, command, alias, argsTruncated);
                    case RELOAD -> {
                    }
                }
            } catch (IllegalArgumentException ignore) {
            }
        }
        return completeList;
    }

    private boolean setupEconomy() {
        var rsp = Bukkit.getServicesManager().getRegistration(EconomyCore.class);
        if (rsp != null) {
            economyProvider = rsp.getProvider();
        }
        return economyProvider != null;
    }

    private boolean setupChat() {
        var rsp = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (rsp != null) {
            chatProvider = rsp.getProvider();
        }
        return chatProvider != null;
    }

    private void IGNORE_RESULT(Object o) {
    }

    enum SubCommands {
        RELOAD,
        SHOW,
        SIT,
        SIGNEDIT,
        LOCK,
        CHAT,
        REDBAG,
        ITEM,
        XP
    }
}
