// Modified MainCommand.java
/*
 * // MainCommand.java
 * // Crafted with precision and care, this class serves as the conductor
 * // of our command orchestra. It gracefully handles user input,
 * // routing commands to their respective handlers with elegance and efficiency,
 * // now allowing console execution of the 'reload' command.
 */
//MainCommand.java
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

    // Define command and subcommand names as constants for better maintainability
    private static final String RELOAD_SUBCOMMAND = "reload";
    private static final String PUNISH_SUBCOMMAND = "punish";
    private static final String HELP_SUBCOMMAND = "help";
    private static final String ADMIN_PERMISSION = "crown.admin";

    public MainCommand(CrownPunishments plugin) {
        this.plugin = plugin;
    }

    /*
     * // onCommand
     * // The grand stage for command execution. Here, we discern the user's
     * // intent and orchestrate the appropriate actions. This method now
     * // supports console execution for specific subcommands like 'reload'.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {


        if (args.length == 0) {
            help(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case RELOAD_SUBCOMMAND:
                if (!sender.hasPermission(ADMIN_PERMISSION)) {
                    sendConfigMessage(sender, "messages.no_permission");
                    return true;
                }
                plugin.getConfigManager().loadConfig();
                sendConfigMessage(sender, "messages.reload_success");
                return true;

            case PUNISH_SUBCOMMAND:
                if (!(sender instanceof Player)) { // Keep player check for punish command only
                    sendConfigMessage(sender, "messages.player_only");
                    return false;
                }
                handlePunishCommand(sender, args);
                return true;
            case HELP_SUBCOMMAND:
                help(sender);
                return true;

            default:
                help(sender);
                return true;
        }
    }

    /*
     * // handlePunishCommand
     * // The delicate art of handling the 'punish' subcommand.
     */
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

    /*
     * // sendConfigMessage
     * // A messenger of the configuration realm.
     */
    private void sendConfigMessage(CommandSender sender, String path, String... replacements) {
        String message = plugin.getConfigManager().getMessage(path, replacements);
        sender.sendMessage(MessageUtils.getColorMessage(message));
    }

    /*
     * // onTabComplete
     * // The helpful hand guiding command input.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList(PUNISH_SUBCOMMAND, HELP_SUBCOMMAND, RELOAD_SUBCOMMAND), completions);
        } else if (args.length == 2 && args[0].equalsIgnoreCase(PUNISH_SUBCOMMAND)) {
            Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
        }
        Collections.sort(completions);
        return completions;
    }

    /*
     * // help
     * // The beacon of guidance for lost commanders.
     */
    private void help(CommandSender sender) {
        // Help messages are now read from config for better customization
        sender.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.help_header")));
        sender.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.help_punish")));
        sender.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.help_reload")));
    }
}