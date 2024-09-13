package cat.nyaa.ukit.show;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.EssentialsDiscordRelayUtils;
import cat.nyaa.ukit.utils.EssentialsPluginUtils;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import land.melon.lab.simplelanguageloader.utils.ItemUtils;
import land.melon.lab.simplelanguageloader.utils.LocaleUtils;
import land.melon.lab.simplelanguageloader.utils.Pair;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ShowFunction implements SubCommandExecutor, SubTabCompleter {
    private final SpigotLoader pluginInstance;
    private final String SHOW_PERMISSION_NODE = "ukit.show";

    public ShowFunction(SpigotLoader pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender instanceof ConsoleCommandSender) {
            commandSender.sendMessage(pluginInstance.language.commonLang.playerOnlyCommand.produce());
            return true;
        }
        var senderPlayer = (Player) commandSender;
        if (!senderPlayer.hasPermission(SHOW_PERMISSION_NODE)) {
            senderPlayer.sendMessage(pluginInstance.language.commonLang.permissionDenied.produce());
            return true;
        }
        var itemInHand = senderPlayer.getInventory().getItemInMainHand();

        var message = (itemInHand.getAmount() == 1 ? pluginInstance.language.showLang.showMessageSingle : pluginInstance.language.showLang.showMessageMultiple).produceAsComponent(
                Pair.of("player",
                        EssentialsPluginUtils.nickWithHoverOrNormalName(senderPlayer.getUniqueId())
                ),
                Pair.of("item", itemInHand.getType().isAir() || itemInHand.getAmount() == 0 ?
                        LocaleUtils.getTranslatableItemComponent(itemInHand) :  // to fix runtime error thrown by packet clientbound/minecraft:system_chat validity check
                        ItemUtils.itemTextWithHover(itemInHand)), // amount must be within range [1;99], Item must not be minecraft:air
                Pair.of("amount", itemInHand.getAmount())
        );

        pluginInstance.getServer().broadcast(message);
        // fail-safe
        if (pluginInstance.config.showConfig.enableDiscordRelay) {
            EssentialsDiscordRelayUtils.broadCastMessageToChat(pluginInstance.config.showConfig.discordRelayPrefix + LegacyComponentSerializer.legacySection().serialize(message));
        }
        return true;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public boolean checkPermission(CommandSender commandSender) {
        return commandSender.hasPermission(SHOW_PERMISSION_NODE);
    }
}
