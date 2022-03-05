package cat.nyaa.ukit.show;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import land.melon.lab.simplelanguageloader.nms.ItemUtils;
import land.melon.lab.simplelanguageloader.nms.LocaleUtils;
import land.melon.lab.simplelanguageloader.utils.Pair;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ShowFunction implements SubCommandExecutor, SubTabCompleter {
    private final SpigotLoader pluginInstance;

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
        if (!senderPlayer.hasPermission("ukit.show")) {
            senderPlayer.sendMessage(pluginInstance.language.commonLang.permissionDenied.produce());
            return true;
        }
        var itemInHand = senderPlayer.getInventory().getItemInMainHand();
        var itemComponent = LocaleUtils.getTranslatableItemComponent(itemInHand);
        itemComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(ItemUtils.itemStackToJson(itemInHand))}));
        // don't know how to use new Content API
        pluginInstance.getServer().spigot().broadcast(
                (itemInHand.getAmount() == 1 ? pluginInstance.language.showLang.showMessageSingle : pluginInstance.language.showLang.showMessageMultiple).produceWithBaseComponent(
                        Pair.of("player", senderPlayer.getName()),
                        Pair.of("item", itemComponent),
                        Pair.of("amount", itemInHand.getAmount())
                )
        );
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
}
