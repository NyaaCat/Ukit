package cat.nyaa.ukit.mail;

import java.util.UUID;

public class MailboxLoc {
    private int blockX;
    private int blockY;
    private int blockZ;
    private UUID worldUUID;

    public MailboxLoc(int blockX, int blockY, int blockZ, UUID worldUUID) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.worldUUID = worldUUID;
    }

    public MailboxLoc() {
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public UUID getWorldUUID() {
        return worldUUID;
    }
}
