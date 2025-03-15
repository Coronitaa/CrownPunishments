/*
 * // CommandBlockerListener.java
 * // The vigilant guardian against unauthorized commands, intercepting
 * // player inputs and enforcing soft ban restrictions, ensuring that
 * // restricted players adhere to their limitations, final version.
 */
//CommandBlockerListener.java
package cp.corona.listeners;

import cp.corona.crownpunishments.CrownPunishments;
import cp.corona.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.logging.Level;

public class CommandBlockerListener implements Listener {

    private final CrownPunishments plugin;
    private final List<String> blockedCommands;

    public CommandBlockerListener(CrownPunishments plugin) {
        this.plugin = plugin;
        this.blockedCommands = plugin.getConfigManager().getBlockedCommands();
    }

    /*
     * // onPlayerCommandPreprocess
     * // Intercepts player command attempts to enforce soft ban restrictions.
     */
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("crown.softban.bypass")) {
            return; // Bypass if player has bypass permission
        }

        if (plugin.getSoftBanDatabaseManager().isSoftBanned(player.getUniqueId())) {
            String command = event.getMessage().substring(1).split(" ")[0].toLowerCase(); // Get command without '/'

            plugin.getLogger().log(Level.INFO, "Player " + player.getName() + " is softbanned, attempting command: " + command); // Enhanced logging

            for (String blockedCmd : blockedCommands) {
                if (command.equalsIgnoreCase(blockedCmd.toLowerCase())) { // Ensure blockedCmd is also lowercased for comparison
                    event.setCancelled(true);
                    player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.softban_command_blocked")));
                    player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.softban_received", "{reason}", plugin.getSoftBanDatabaseManager().getSoftBanReason(player.getUniqueId())))); // Send SoftBan received message
                    plugin.getLogger().log(Level.INFO, "Command " + command + " BLOCKED for softbanned player " + player.getName()); // Enhanced logging - command blocked
                    return;
                }
            }
            plugin.getLogger().log(Level.INFO, "Command " + command + " NOT blocked for softbanned player " + player.getName() + ", command not in blocked list."); // Enhanced logging - command not blocked
        } else {
            plugin.getLogger().log(Level.INFO, "Player " + player.getName() + " is NOT softbanned."); // Enhanced logging - not softbanned
        }
    }
}