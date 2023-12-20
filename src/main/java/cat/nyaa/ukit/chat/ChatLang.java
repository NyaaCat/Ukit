package cat.nyaa.ukit.chat;

import land.melon.lab.simplelanguageloader.components.Text;

public class ChatLang {
    public Text help = Text.of(
            "&7Usage of /ukit chat:",
            "&7    /ukit chat <perfix|suffix> <set|remove> [content...]",
            "&7    /ukit chat <signature> <on|off>"
    );
    public Text prohibitedWord = Text.of(
            "Unable to apply your settings due to prohibited word ({word})."
    );
    public Text signaturePreferenceSet = Text.of(
            "Your message signature preference has been updated to {flag}&7."
    );
    public TailLang prefixLang = new TailLang(
            Text.of("&cYou can't offer the cost of {exp} level exp and {money}{currencyUnit} to change your prefix"),
            Text.of("&c{text}&c is too long (max {max} characters) to apply your prefix setting"),
            Text.of("&7Success to apply {text}&7 as your prefix"),
            Text.of("&7Success to remove your prefix."),
            Text.of("&cThis feature is disabled")
    );
    public TailLang suffixLang = new TailLang(
            Text.of("&cYou can't offer the cost of {exp} level exp and {money}{currencyUnit} to change your suffix"),
            Text.of("&c{text} is too long (max {max} characters) to apply your suffix setting"),
            Text.of("&7Success to apply {text}&7 as your suffix"),
            Text.of("&7Success to remove your suffix."),
            Text.of("&cThis feature is disabled.")
    );
}
