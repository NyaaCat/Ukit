package cat.nyaa.ukit.api;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.loginpush.LoginPushRecorder;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.util.UUID;

public class UKitAPI {
    private final SpigotLoader pluginInstance;
    private final LoginPushRecorder loginPushRecorder;
    private static UKitAPI instance;

    public UKitAPI(SpigotLoader pluginInstance, LoginPushRecorder loginPushRecorder) {
        this.pluginInstance = pluginInstance;
        this.loginPushRecorder = loginPushRecorder;
        instance = this;
    }

    public static UKitAPI getAPIInstance() {
        return instance;
    }

    public void createLoginPush(UUID playerUniqueID, Component message, Component senderName) throws SQLException {
        loginPushRecorder.createLoginPush(playerUniqueID, message, senderName);
    }
}
