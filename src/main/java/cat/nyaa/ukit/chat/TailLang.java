package cat.nyaa.ukit.chat;

import land.melon.lab.simplelanguageloader.components.Text;

public class TailLang {
    public Text cantOffered;
    public Text textTooLong;
    public Text settingApplied;
    public Text settingRemoved;
    public Text notEnabled;

    public TailLang(Text cantOffered, Text textTooLong, Text settingApplied, Text settingRemoved, Text notEnabled) {
        this.cantOffered = cantOffered;
        this.textTooLong = textTooLong;
        this.settingApplied = settingApplied;
        this.settingRemoved = settingRemoved;
        this.notEnabled = notEnabled;
    }

    public TailLang() {
    }
}
