package cat.nyaa.ukit.xpstore;

public class XpStoreConfig {
    public int maxAmount = Integer.MAX_VALUE / 64;
    public boolean enableQuickTake = true;
    public int quickTakeMinimumAmount = 10;
    public int quickTakeArmTimeInMillisecond = 2000;
    public double quickTakeRatio = 0.01;
}
