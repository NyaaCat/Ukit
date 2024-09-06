package cat.nyaa.ukit.loginpush;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import land.melon.lab.simplelanguageloader.utils.Pair;
import net.ess3.api.events.AfkStatusChangeEvent;
import net.kyori.adventure.text.Component;
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
        var dateFormat = new SimpleDateFormat(pluginInstance.language.loginPushLang.push_timestamp_format.produce());
        Bukkit.getAsyncScheduler().runNow(pluginInstance, (task) -> {
            try {
                var loginPushes = loginPushRecorder.getLoginPush(senderPlayer.getUniqueId());
                if (loginPushes.isEmpty()) {
                    senderPlayer.sendMessage(pluginInstance.language.loginPushLang.no_new_push.produce());
                    return;
                }
                senderPlayer.sendMessage(pluginInstance.language.loginPushLang.login_push_title.produce());
                loginPushes.forEach(loginPush -> {
                    var line1 = pluginInstance.language.loginPushLang.push_message_line1.produceAsComponent(
                            Pair.of("message", loginPush.content()),
                            Pair.of("time", dateFormat.format(loginPush.time())),
                            Pair.of("sender", loginPush.sender())
                    );
                    var line2 = pluginInstance.language.loginPushLang.push_message_line2.produceAsComponent(
                            Pair.of("message", loginPush.content()),
                            Pair.of("time", dateFormat.format(loginPush.time())),
                            Pair.of("sender", loginPush.sender())
                    );
                    senderPlayer.sendMessage(
                            Component.text().append(line1).appendNewline().append(line2));
                });
                loginPushRecorder.deleteLoginPush(loginPushes.stream().map(LoginPush::id).toList());
                senderPlayer.sendMessage(pluginInstance.language.loginPushLang.login_push_end.produce());
            } catch (SQLException e) {
                e.printStackTrace();
                commandSender.sendMessage(pluginInstance.language.commonLang.sqlErrorOccurred.produce());
            }
        });
        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getAsyncScheduler().runNow(pluginInstance,(task)->{
            countAndSendMessageIfHasMessage(event.getPlayer(), 20 * 3);
        });
    }

    @EventHandler
    public void onAFKStatusChange(AfkStatusChangeEvent event) {
        if (!event.getValue()) {
            countAndSendMessageIfHasMessage(event.getAffected().getBase(), 10);
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

    private void countAndSendMessageIfHasMessage(Player player, long delayInTick) {
        try {
            int unreadPush = loginPushRecorder.countUnreadPush(player.getUniqueId());
            if (unreadPush > 0) {
                Bukkit.getGlobalRegionScheduler().runDelayed(pluginInstance, (ignored) -> player.sendMessage(pluginInstance.language.loginPushLang.login_push_notice.produce(
                        Pair.of("number", unreadPush)
                )), delayInTick);
            }
        } catch (SQLException e) {
            pluginInstance.getLogger().warning(e.getMessage());
            player.sendMessage(pluginInstance.language.commonLang.sqlErrorOccurred.produce());
        }
    }
}
