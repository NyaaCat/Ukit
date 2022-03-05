package cat.nyaa.ukit.show;

import land.melon.lab.simplelanguageloader.components.Text;

public class ShowLang {
    // {amount} is also usable here
    public Text showMessageSingle = Text.of(
            "{player} is showing {item}"
    );
    public Text showMessageMultiple = Text.of(
            "{player} is showing {item} x {amount}"
    );
}
