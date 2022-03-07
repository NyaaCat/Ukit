package cat.nyaa.ukit.chat;

import land.melon.lab.simplelanguageloader.components.Text;

import java.util.List;

public class TailSettings {
    public boolean enabled = true;
    public double moneyCost = 50.0;
    public int expCostLvl = 5;
    public int maxLength = 10;
    public Text customPattern = Text.of("{text}");
    public List<String> prohibitedWords = List.of("&k", "§k");
}
