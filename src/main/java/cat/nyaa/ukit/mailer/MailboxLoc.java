package cat.nyaa.ukit.mailer;

public class MailboxLoc {
    private int x;
    private int y;
    private int z;
    private String world;

    public MailboxLoc(int x, int y, int z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public MailboxLoc(){}

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getWorld() {
        return world;
    }
}
