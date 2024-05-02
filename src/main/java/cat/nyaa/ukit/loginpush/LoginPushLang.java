package cat.nyaa.ukit.loginpush;

import land.melon.lab.simplelanguageloader.components.Text;

public class LoginPushLang {
    public Text news_help = Text.of("&7Usage:",
            "&7    /u news&7: fetch unread messages");
    public Text login_push_notice = Text.of("&7you received {number} messages during offline, use &b/u news &7to read");
    public Text no_new_push = Text.of("&8You have no new messages to read");
    public Text login_push_title = Text.of("&8Here are your unread messages: ");
    public Text login_push_end = Text.of("&8Message delivery complete");
    public Text push_timestamp_format = Text.of("yyyy-MM-dd HH:mm");
    public Text push_message_line1 = Text.of("  {message}");
    public Text push_message_line2 = Text.of("  &8&o↳ {time} , comes from {sender}");
}
