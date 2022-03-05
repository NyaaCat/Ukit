package cat.nyaa.ukit.lock;

import land.melon.lab.simplelanguageloader.components.Text;

public class LockLang {
    public Text help = Text.of(
            "&7Usage of /ukit lock:",
            "&7    /ukit lock <setup|remove|property>",
            "&7        /ukit lock setup: turn the item frame you are looking at to a locked one.",
            "&7        /ukit lock remove: restore the locked item frame to normal one.",
            "&7        /ukit lock property: show a interact property menu to change the properties of a locked frame."
    );
    public Text noEntityFound = Text.of("&cPlease looking at an item frame before perform actions.");
    public Text notItemFrame = Text.of("&cThe entity you are looking at is not an item frame.");
    public Text alreadyLockedFrame = Text.of("&cThe entity you are looking at is already a locked frame.");
    public Text setupSuccessfully = Text.of("&7Successfully setup a lock frame.");
    public Text emptyFrameNotAllowed = Text.of("&cYou can't setup an empty item frame as lock frame.");
    public Text removeSuccessfully = Text.of("&7Successfully removed lock frame.");
    public Text cantOperateOthers = Text.of("&cIs is not your lock frame.");
    public Text notLockFrame = Text.of("&cThis item frame is not an lock frame.");
    public Text invalidProperty = Text.of("&c{property} is not a valid property.");
    public Text lockFrameInfo = Text.of(
            "&7Lock Frame Info:",
            "&7    Owner: &6{owner}",
            "&7    Item: &6{item}"
    );
    public Text lockFrameProperties = Text.of(
            "&7Properties:",
            "&7    Transparent&7: {transparent} {toggleButtonTransparent}",
            "&7    Growing&7: {growing} {toggleButtonDisplayName}"
    );

}
