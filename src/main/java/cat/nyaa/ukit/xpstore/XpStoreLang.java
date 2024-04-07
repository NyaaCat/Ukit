package cat.nyaa.ukit.xpstore;

import land.melon.lab.simplelanguageloader.components.Text;

public class XpStoreLang {
    public Text help = Text.of(
            "&7Usage:",
            "&7    /ukit xp store <amount>: add experience to experience bottles in your hand",
            "&7    /ukit xp take <amount>: take out experience from the experience bottles in your hand."
    );
    public Text loreTextPattern = Text.of("&#5cb2fbS&#66affbt&#70acfbo&#79a8fcr&#83a5fce&#8da2fcd &#979ffcE&#a19cfcx&#aa98fdp&#b495fd: &#be92fd{amount}");
    public Text noExpBottleInHandTabNotice = Text.of("Note: Neither Your Hands has Experience Bottle");
    public Text noItemInHand = Text.of("&cPlease hold an exp bottle in your hand before using this command");
    public Text notExpBottle = Text.of("&cCan't save exp into {item}&c! You can only save exp into exp bottles.");
    public Text notValidAmount = Text.of("&c{input} is not an valid amount.");
    public Text notEnoughExp = Text.of("&cNot enough experience. You need at least {expTotal} experiences to store {expPerBottle} to {amount} experience bottles in your hand.");
    public Text notEnoughExpInBottle = Text.of("&cThe experience bottle in your hand don't have {amount} experience to take out.");
    public Text expSaved = Text.of("&7You have saved {amount} experiences to exp bottle in your hand");
    public Text expTook = Text.of("&7You have took {amount} experiences total from the bottle in your hand.");
}
