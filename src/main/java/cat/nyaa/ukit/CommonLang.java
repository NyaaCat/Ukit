package cat.nyaa.ukit;

import land.melon.lab.simplelanguageloader.components.Text;

public class CommonLang {
    public Text playerOnlyCommand = Text.of("&7You could only use this command as a player.");
    public Text permissionDenied = Text.of("&cSorry, but you don't have permission to use this command.");
    public Text reloadSuccess = Text.of("&7UKit reloaded.");
    public Text helpMessage = Text.of(
            "&7Usage of /ukit:",
            "&7    /ukit lock: custom item frame lock",
            "&7    /ukit show: broadcast your item in hand",
            "&7    /ukit signedit: edit the sign you are looking at",
            "&7    /ukit sit: enable/disable sit function. You could sit on some blocks when you right click them after you enabled sitting function.",
            "&7    /ukit redbag: send a redbag to all the players online"
    );
    public Text functionDisabled = Text.of("&cThis function is not enabled due to server side configuration.");
    public Text textTrue = Text.of("&atrue");
    public Text textFalse = Text.of("&cfalse");
    public Text buttonOn = Text.of("&7[&aon&7]");
    public Text buttonOff = Text.of("&7[&coff&7]");
}
