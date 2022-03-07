package cat.nyaa.ukit;

import land.melon.lab.simplelanguageloader.components.Text;

public class CommonLang {
    public Text playerOnlyCommand = Text.of("This command can only be used by players.");
    public Text permissionDenied = Text.of("You don't have permission to use this command.");
    public Text helpMessage = Text.of(
            "&7Usage of /ukit:",
            "&7    /ukit lock: custom item frame lock",
            "&7    /ukit show: broadcast your item in hand",
            "&7    /ukit signedit: edit the sign you are looking at",
            "&7    /ukit sit: enable/disable sit function. You could sit on some blocks when you right click them after you enabled sitting function.."
    );
    public Text functionDisabled = Text.of("&cThis function is not enabled due to server side configuration.");
    public Text textTrue = Text.of("&atrue");
    public Text textFalse = Text.of("&cfalse");
    public Text buttonOn = Text.of("&7[&aon&7]");
    public Text buttonOff = Text.of("&7[&coff&7]");
}
