package cat.nyaa.ukit.mailer;

import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class MailerFunction implements SubCommandExecutor, SubTabCompleter {

    public static final String MAILER_PERMISSION_NODE = "ukit.mailer";


    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        return false;
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

        return commandSender.hasPermission(MAILER_PERMISSION_NODE);
    }
}
