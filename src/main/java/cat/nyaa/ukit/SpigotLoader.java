package cat.nyaa.ukit;

import cat.nyaa.ukit.lock.LockFunction;
import cat.nyaa.ukit.show.ShowFunction;
import cat.nyaa.ukit.signedit.SignEditFunction;
import cat.nyaa.ukit.sit.SitConfig;
import cat.nyaa.ukit.sit.SitFunction;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import com.google.gson.GsonBuilder;
import land.melon.lab.simplelanguageloader.SimpleLanguageLoader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
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
    public MainLang language;
    public MainConfig config;
    public File languageFile = new File(getDataFolder(), "language.json");
    public File configFile = new File(getDataFolder(), "config.json");
    private SitFunction sitFunction;
    private ShowFunction showFunction;
    private SignEditFunction signEditFunction;
    private LockFunction lockFunction;

    @Override
    public void onEnable() {
        IGNORE_RESULT(getDataFolder().mkdir());
        logger = this.getLogger();

        this.getServer().getPluginCommand("ukit").setExecutor(this);
        this.getServer().getPluginCommand("ukit").setTabCompleter(this);

        sitFunction = new SitFunction(this);
        showFunction = new ShowFunction(this);
        signEditFunction = new SignEditFunction(this);
        lockFunction = new LockFunction(this);

        this.getServer().getPluginManager().registerEvents(sitFunction, this);

        if (!reload()) {
            getLogger().severe("Failed to load configs, disabling...");
            getPluginLoader().disablePlugin(this);
        }
    }

    public boolean reload() {
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
                if (!sender.hasPermission("ukit.reload")) {
                    sender.sendMessage(language.commonLang.permissionDenied.produce());
                }
            }
            case SHOW -> invokeCommand(showFunction, sender, command, label, argTruncated);

            case SIT -> invokeCommand(sitFunction, sender, command, label, argTruncated);

            case SIGNEDIT -> invokeCommand(signEditFunction, sender, command, label, argTruncated);

            case LOCK -> invokeCommand(lockFunction, sender, command, label, argTruncated);

        }
        return true;
    }

    private void invokeCommand(SubCommandExecutor subCommandExecutor, CommandSender sender, Command command, String label, String[] args) {
        if (!subCommandExecutor.invokeCommand(sender, command, label, args)) {
            sender.sendMessage(subCommandExecutor.getHelp());
        }
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, @Nonnull String[] args) {
        var subCommandList = Arrays.stream(SubCommands.values()).map((v) -> v.name().toLowerCase()).toList();
        List<String> completeList = null;
        if (args.length == 0) {
            return subCommandList;
        } else if (args.length == 1) {
            completeList = new ArrayList<>();
            for (var subCommand : subCommandList) {
                if (subCommand.startsWith(args[0]) && !subCommand.equalsIgnoreCase(args[0])) {
                    completeList.add(subCommand);
                }
            }
            return completeList;
        } else {
            try {
                var argsTruncated = Arrays.copyOfRange(args, 1, args.length);
                switch (SubCommands.valueOf(args[0].toUpperCase())) {
                    case SHOW -> completeList = showFunction.tabComplete(sender, command, alias, argsTruncated);
                    case SIT -> completeList = sitFunction.tabComplete(sender, command, alias, argsTruncated);
                    case SIGNEDIT -> completeList = signEditFunction.tabComplete(sender, command, alias, argsTruncated);
                    case LOCK -> completeList = lockFunction.tabComplete(sender, command, alias, argsTruncated);
                }
            } catch (IllegalArgumentException ignore) {
            }
        }
        return completeList;
    }

    private void IGNORE_RESULT(Object o) {
    }

    enum SubCommands {
        RELOAD,
        SHOW,
        SIT,
        SIGNEDIT,
        LOCK
    }

}
