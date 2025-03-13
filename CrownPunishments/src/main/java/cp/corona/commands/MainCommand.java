package cp.corona.commands;

import cp.corona.crownpunishments.CrownPunishments;
import cp.corona.menus.PunishMenu;
import cp.corona.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class MainCommand implements CommandExecutor, TabCompleter {
    private final CrownPunishments plugin;

    public MainCommand(CrownPunishments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sendConfigMessage(sender, "messages.player_only");
            return false;
        }

        if (args.length == 0) {
            help(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("crown.admin")) {
                    sendConfigMessage(sender, "messages.no_permission");
                    return true;
                }
                plugin.getConfigManager().loadConfig();
                sendConfigMessage(sender, "messages.reload_success");
                return true;

            case "punish":
                handlePunishCommand(sender, args);
                return true;

            default:
                help(sender);
                return true;
        }
    }

    private void handlePunishCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendConfigMessage(sender, "messages.invalid_player", "{usage}", "/crown punish [Player]");
            return;
        }

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sendConfigMessage(sender, "messages.never_played", "{input}", targetName);
            return;
        }

        new PunishMenu(target.getUniqueId(), plugin).open((Player) sender);
    }

    private void sendConfigMessage(CommandSender sender, String path, String... replacements) {
        String message = plugin.getConfigManager().getMessage(path, replacements);
        sender.sendMessage(MessageUtils.getColorMessage(message));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("punish", "help", "reload"), completions);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("punish")) {
            Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
        }
        Collections.sort(completions);
        return completions;
    }

    private void help(CommandSender sender) {
        sender.sendMessage(MessageUtils.getColorMessage("&6&lCrownPunishments Commands:"));
        sender.sendMessage(MessageUtils.getColorMessage("&b/crown punish <player> &7- Open punishment menu"));
        sender.sendMessage(MessageUtils.getColorMessage("&b/crown reload &7- Reload configuration"));
    }
}