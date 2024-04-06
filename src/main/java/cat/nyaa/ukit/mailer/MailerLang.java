package cat.nyaa.ukit.mailer;

import land.melon.lab.simplelanguageloader.components.Text;

public class MailerLang {
    public Text mailerHelp = Text.of(
            "&7Usage:",
            "&7    /u mailer mailbox <set|clear|info>&7: set/clear your mailbox",
            "&7    /u mailer sendto <player>&7: send the item in your main hand to the mailbox of a player"
    );
    public Text rightClickToSet = Text.of("&7Right click a chest to set your mailbox");
    public Text mailboxSet = Text.of("&7Mailbox location set to {x}&7, {y}&7, {z}&7, {world}");
    public Text mailboxCleared = Text.of("&7Your mailbox setting cleared");
    public Text mailboxInfo = Text.of("&7Your mailbox is located at x:{x}&7, y:{y}&7, z:{z}&7, {world}");
    public Text mailboxNotSetYet = Text.of("&7Your mailbox is not set yet!");
    public Text setCancelled = Text.of("&7Cancelled to set a mailbox");
    public Text itemSent = Text.of("&7Item sent to {player}&7's mailbox, costs {amount}{currencyUnit}&7");
    public Text noItemInHand = Text.of("&7You have no item in your main hand!");
    public Text moneyNotEnough = Text.of("&7You need at least {amount}{currencyUnit}&7 to send this item");
    public Text playerNotSetMailboxYet = Text.of("&7{player} has not set a mailbox yet");
    public Text mailboxNotAvail = Text.of("&7{player}'s mailbox is not available for now");
    public Text mailboxFull = Text.of("&7{player}'s mailbox is full and can't accept new items");
}
