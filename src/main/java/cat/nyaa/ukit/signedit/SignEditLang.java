package cat.nyaa.ukit.signedit;

import land.melon.lab.simplelanguageloader.components.Text;

public class SignEditLang {
    public Text help = Text.of(
            "&7Usage of &6/ukit signedit&7:",
            "&7    &6/ukit signedit<line> <text>&7: edit a line of a sign",
            "&7    Note: The text you enter cannot exceed {max} characters per line (except format codes)."
    );
    public Text notASign = Text.of("&cThe block which you are looking at is not a sign.");

    public Text cantModifyLockSign = Text.of("&cLock sign couldn't be modified.");

    public Text modifyCancelled = Text.of("&cThis sign couldn't be modified due to server side configuration.");
    public Text invalidLineNumber = Text.of("&c{input} is not a valid line number, it should be a number between 1 and 4.");
    public Text textTooLong = Text.of("&cThe text you entered is too long, it should be less than {max} characters (except format codes).");
}
