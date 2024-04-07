package cat.nyaa.ukit;

import cat.nyaa.ukit.chat.ChatSettings;
import cat.nyaa.ukit.elytra.ElytraConfig;
import cat.nyaa.ukit.item.ItemConfig;
import cat.nyaa.ukit.mailer.MailerConfig;
import cat.nyaa.ukit.redbag.RedbagConfig;
import cat.nyaa.ukit.show.ShowConfig;
import cat.nyaa.ukit.signedit.SignEditConfig;
import cat.nyaa.ukit.sit.SitConfig;
import cat.nyaa.ukit.xpstore.XpStoreConfig;

public class MainConfig {
    public SitConfig sitConfig = new SitConfig();
    public SignEditConfig signEditConfig = new SignEditConfig();
    public ChatSettings chatSettings = new ChatSettings();
    public RedbagConfig redbagConfig = new RedbagConfig();
    public ItemConfig itemConfig = new ItemConfig();
    public XpStoreConfig xpStoreConfig = new XpStoreConfig();
    public ElytraConfig elytraConfig = new ElytraConfig();

    public MailerConfig mailerConfig = new MailerConfig();
    public ShowConfig showConfig = new ShowConfig();
}
