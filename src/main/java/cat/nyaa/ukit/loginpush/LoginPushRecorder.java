package cat.nyaa.ukit.loginpush;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoginPushRecorder {
    private final Connection connection;

    private final File recordFile;

    private final String TABLE_NAME = "login_msg";

    public LoginPushRecorder(File recordFile) throws SQLException {
        this.recordFile = recordFile;
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + recordFile.getAbsolutePath());
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (msg_id INTEGER PRIMARY KEY AUTOINCREMENT, receiverUniqueID TEXT, timestamp INTEGER, raw_message TEXT)");
            //create an index on receiverUniqueID
            statement.execute("CREATE INDEX IF NOT EXISTS ReceiverUniqueIDIndex ON " + TABLE_NAME + " (receiverUniqueID)");
        }
    }

    public List<LoginPush> getLoginPush(UUID playerUniqueId) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE receiverUniqueID = ?")) {
            statement.setString(1, playerUniqueId.toString());
            var resultSet = statement.executeQuery();
            List<LoginPush> loginPushes = new ArrayList<>();
            while (resultSet.next()) {
                loginPushes.add(
                        new LoginPush(resultSet.getInt("msg_id"),
                                resultSet.getLong("timestamp"),
                                JSONComponentSerializer.json().deserialize(resultSet.getString("raw_message"))));
            }
            return loginPushes;
        }
    }

    public int countUnreadPush(UUID playerUniqueId) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE receiverUniqueID = ?")) {
            statement.setString(1, playerUniqueId.toString());
            var resultSet = statement.executeQuery();
            return resultSet.getInt(1);
        }
    }

    public void createLoginPush(UUID playerUniqueID, Component message) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (receiverUniqueID, timestamp, raw_message) VALUES (?, ?, ?)")) {
            statement.setString(1, playerUniqueID.toString());
            statement.setLong(2, System.currentTimeMillis());
            statement.setString(3, JSONComponentSerializer.json().serialize(message));
            statement.execute();
        }
    }

    public void deleteLoginPush(List<Integer> messageIds) throws SQLException {
        try (var statement = connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE msg_id = ?")) {
            for (Integer messageId : messageIds) {
                statement.setInt(1, messageId);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
