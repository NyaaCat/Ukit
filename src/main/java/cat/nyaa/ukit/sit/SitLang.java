package cat.nyaa.ukit.sit;

import land.melon.lab.simplelanguageloader.components.Text;

public class SitLang {
    public Text help = Text.of(
            "&7Usage of &6/ukit sit&7:",
            "&7    &6/ukit sit [enable|disable|toggle]: enable/disable/toggle sit mode.",
            "&7    Note: empty parameter (/ukit sit) will toggle sit mode by default."
    );
    public Text invalidLocation = new Text("&7Sorry, but you can't sit here.");
    public Text sitEnabled = new Text("&7Sitting function enabled!");
    public Text sitDisabled = new Text("&7Sitting function disabled!");
}
