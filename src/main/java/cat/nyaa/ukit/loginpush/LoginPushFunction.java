package cat.nyaa.ukit.loginpush;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import land.melon.lab.simplelanguageloader.utils.Pair;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class LoginPushFunction implements Listener, SubCommandExecutor, SubTabCompleter {
    private final String LOGINPUSH_PERMISSION_NODE = "ukit.news";
    private final SpigotLoader pluginInstance;
    private final File pushSaveFile;
    private final LoginPushRecorder loginPushRecorder;

    public LoginPushFunction(SpigotLoader pluginInstance, File pushSaveFile) throws SQLException {
        this.pluginInstance = pluginInstance;
        this.pushSaveFile = pushSaveFile;
        loginPushRecorder = new LoginPushRecorder(pushSaveFile);
    }


    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player senderPlayer)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.playerOnlyCommand.produce());
            return true;
        }
        if (!commandSender.hasPermission(LOGINPUSH_PERMISSION_NODE)) {
            senderPlayer.sendMessage(pluginInstance.language.commonLang.permissionDenied.produce());
            return true;
        }
        senderPlayer.sendMessage(pluginInstance.language.loginPushLang.login_push_title.produce());
        var dateFormat = new SimpleDateFormat(pluginInstance.language.loginPushLang.push_timestamp_format.produce());
        try {
            loginPushRecorder.getLoginPush(senderPlayer.getUniqueId()).forEach(loginPush -> {
                senderPlayer.sendMessage(
                        loginPush.content().appendNewline().append(
                                LegacyComponentSerializer.legacySection().deserialize(dateFormat.format(loginPush.time())
                                )
                        ));
            });
        } catch (SQLException e) {
            commandSender.sendMessage(pluginInstance.language.commonLang.sqlErrorOccurred.produce());
            return true;
        }
        senderPlayer.sendMessage(pluginInstance.language.loginPushLang.login_push_end.produce());
        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            int unreadPush = loginPushRecorder.countUnreadPush(event.getPlayer().getUniqueId());
            if (unreadPush > 0) {
                Bukkit.getGlobalRegionScheduler().runDelayed(pluginInstance, (ignored) -> event.getPlayer().sendMessage(pluginInstance.language.loginPushLang.login_push_notice.produce(
                        Pair.of("number", unreadPush)
                )), 20 * 3);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            event.getPlayer().sendMessage(pluginInstance.language.commonLang.sqlErrorOccurred.produce());
        }
    }

    @Override
    public String getHelp() {
        return pluginInstance.language.loginPushLang.news_help.produce();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }

    @Override
    public boolean checkPermission(CommandSender commandSender) {
        return commandSender.hasPermission(LOGINPUSH_PERMISSION_NODE);
    }

    public LoginPushRecorder getLoginPushRecorder() {
        return loginPushRecorder;
    }
}
