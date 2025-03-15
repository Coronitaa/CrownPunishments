// Modified CrownPunishments.java
/*
 * // CrownPunishments.java
 * // The heart of the CrownPunishments plugin, the central hub orchestrating
 * // all functionalities. Like a regal monarch, it oversees commands, events,
 * // and configurations, ensuring harmonious plugin operation, now expanded
 * // with database management and soft ban features.
 */
//CrownPunishments.java
package cp.corona.crownpunishments;

import cp.corona.commands.MainCommand;
import cp.corona.config.MainConfigManager;
import cp.corona.database.SoftBanDatabaseManager;
import cp.corona.listeners.CommandBlockerListener;
import cp.corona.listeners.MenuListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrownPunishments extends JavaPlugin {
    // Removed static prefix variable from CrownPunishments class
    private final String version = getDescription().getVersion();
    private MainConfigManager configManager;
    private SoftBanDatabaseManager softBanDatabaseManager; // Database Manager
    private boolean placeholderAPIEnabled;

    /*
     * // onEnable
     * // The grand inauguration of the plugin. Upon server launch, this method
     * // is invoked, setting up configurations, registering commands and events,
     * // and announcing the plugin's arrival to the world, now also initializing
     * // database connections and managers for soft ban functionalities.
     */
    @Override
    public void onEnable() {
        this.configManager = new MainConfigManager(this);
        this.softBanDatabaseManager = new SoftBanDatabaseManager(this); // Initialize database manager
        placeholderAPIEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        registerCommands();
        registerEvents();

        // Use configManager to get prefix and messages
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                configManager.getMessage("prefix") + "&dPlugin enabled. Version: " + version));
    }

    /*
     * // onDisable
     * // The graceful farewell as the plugin prepares to depart. This method
     * // is called upon server shutdown or plugin disable, performing cleanup
     * // tasks and bidding adieu to the server console, ensuring resources
     * // are released, including database connections if necessary.
     */
    @Override
    public void onDisable() {
        // Use configManager to get prefix and messages
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                configManager.getMessage("prefix") + "&dPlugin disabled."));
    }

    /*
     * // registerCommands
     * // The herald of commands, announcing the plugin's directives to the server.
     * // This method registers all plugin commands, making them available for
     * // players and the console to invoke, initiating plugin actions, including
     * // those related to soft bans.
     */
    /**
     * Registers plugin commands.
     */
    public void registerCommands() {
        MainCommand mainCommand = new MainCommand(this);
        getCommand("crown").setExecutor(mainCommand);
        getCommand("crown").setTabCompleter(mainCommand);
    }

    /*
     * // registerEvents
     * // The sentinel of events, watching for player interactions and server activities.
     * // This method registers all plugin event listeners, enabling the plugin
     * // to react dynamically to in-game occurrences and player actions, now
     * // including listeners for command blocking and menu interactions for soft bans.
     */
    /**
     * Registers plugin event listeners.
     */
    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandBlockerListener(this), this); // Register CommandBlockerListener
    }

    /*
     * // getConfigManager
     * // The keeper of configurations, providing access to the MainConfigManager.
     * // This method allows other parts of the plugin to retrieve the configuration
     * // manager, enabling them to fetch settings and messages as needed, including
     * // those for soft ban configurations.
     */
    /**
     * Gets the MainConfigManager instance.
     *
     * @return MainConfigManager instance.
     */
    public MainConfigManager getConfigManager() {
        return configManager;
    }

    /*
     * // getSoftBanDatabaseManager
     * // Accessor for the SoftBanDatabaseManager, providing a way to interact
     * // with the database operations related to soft bans, such as checking
     * // soft ban status or adding new bans.
     */
    public SoftBanDatabaseManager getSoftBanDatabaseManager() {
        return softBanDatabaseManager;
    }

    /*
     * // isPlaceholderAPIEnabled
     * // The PlaceholderAPI status checker, confirming if the bridge to placeholders is active.
     * // This method verifies if PlaceholderAPI is loaded, allowing the plugin
     * // to conditionally use PlaceholderAPI features and enrich text with dynamic data,
     * // potentially including placeholders related to soft ban status in the future.
     */
    /**
     * Checks if PlaceholderAPI is enabled.
     *
     * @return true if PlaceholderAPI is enabled, false otherwise.
     */
    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
}