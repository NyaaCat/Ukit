package cat.nyaa.ukit.mailer;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MailboxLocRecorder {
    private final ConcurrentMap<UUID, MailboxLoc> mailboxLocMap;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().disableHtmlEscaping().create();

    private final File recordFile;
    private final File recordBackupFile;

    public MailboxLocRecorder(File recordFile) throws IOException {
        if (recordFile.createNewFile() && recordFile.isFile())
            throw new IOException(recordFile.getAbsolutePath() + "should be a file, but found a directory.");
        var reader = new InputStreamReader(new GZIPInputStream(new FileInputStream(recordFile)));
        ConcurrentMap<UUID, MailboxLoc> map = gson.fromJson(reader, new TypeToken<ConcurrentHashMap<UUID, MailboxLoc>>() {
        }.getType());

        if (map == null)
            map = new ConcurrentHashMap<>();

        mailboxLocMap = map;
        this.recordFile = recordFile;
        this.recordBackupFile = new File(recordFile.getAbsolutePath() + ".backup");
    }

    public void saveLocMap() throws IOException {
        var writer = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(recordFile)));
        var ignored = recordBackupFile.delete();
        if (recordFile.exists())
            if (recordFile.renameTo(recordBackupFile))
                throw new IOException("failed to create record file backup!");
        gson.toJson(mailboxLocMap, writer);
    }

    public boolean hasMailboxSet(UUID playerUniqueID) {
        return mailboxLocMap.containsKey(playerUniqueID);
    }

    public MailboxLoc getMailboxLoc(UUID playerUniqueID) {
        return mailboxLocMap.get(playerUniqueID);
    }

    public void setMailboxLog(UUID playerUniqueID, MailboxLoc mailboxLoc) {
        mailboxLocMap.put(playerUniqueID, mailboxLoc);
    }

}
