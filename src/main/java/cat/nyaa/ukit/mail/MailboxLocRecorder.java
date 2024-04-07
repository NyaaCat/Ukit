package cat.nyaa.ukit.mail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

public class MailboxLocRecorder {
    private final Connection connection;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().disableHtmlEscaping().create();

    private final File recordFile;

    private final String TABLE_NAME = "mailbox_loc";


    public MailboxLocRecorder(File recordFile) throws IOException, SQLException {
        if (!recordFile.createNewFile() && !recordFile.isFile())
            throw new IOException(recordFile.getAbsolutePath() + " should be a file, but found a directory.");
        this.recordFile = recordFile;
        connection = DriverManager.getConnection("jdbc:sqlite:" + recordFile.getAbsolutePath());
        connection.createStatement().execute("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (uuid TEXT PRIMARY KEY, world_x INTEGER, world_y INTEGER, world_z INTEGER, world_uuid TEXT)");
    }

    public MailboxLoc getMailboxLoc(UUID playerUniqueID) throws SQLException {
        var statement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE uuid = ?");
        statement.setString(1, playerUniqueID.toString());
        var resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return new MailboxLoc(resultSet.getInt("world_x"), resultSet.getInt("world_y"), resultSet.getInt("world_z"), UUID.fromString(resultSet.getString("world_uuid")));
        } else {
            return null;
        }
    }

    public void setMailboxLoc(UUID playerUniqueID, MailboxLoc mailboxLoc) throws SQLException {
        var statement = connection.prepareStatement("INSERT OR REPLACE INTO " + TABLE_NAME + " (uuid, world_x, world_y, world_z, world_uuid) VALUES (?, ?, ?, ?, ?)");
        statement.setString(1, playerUniqueID.toString());
        statement.setInt(2, mailboxLoc.getBlockX());
        statement.setInt(3, mailboxLoc.getBlockY());
        statement.setInt(4, mailboxLoc.getBlockZ());
        statement.setString(5, mailboxLoc.getWorldUUID().toString());
        statement.execute();
    }

    public void deleteMailboxRecord(UUID playerUniqueID) throws SQLException {
        var statement = connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE uuid = ?");
        statement.setString(1, playerUniqueID.toString());
        statement.execute();
    }

}
