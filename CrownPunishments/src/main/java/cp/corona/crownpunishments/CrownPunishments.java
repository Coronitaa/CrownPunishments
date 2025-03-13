package cp.corona.crownpunishments;

import cp.corona.commands.MainCommand;
import cp.corona.config.MainConfigManager;
import cp.corona.listeners.MenuListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrownPunishments extends JavaPlugin {
    public static String prefix = "&8[&6&lC&c&lP&r&8] &r";
    private final String version = getDescription().getVersion();
    private MainConfigManager configManager;
    private boolean placeholderAPIEnabled;

    @Override
    public void onEnable() {
        this.configManager = new MainConfigManager(this);
        placeholderAPIEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        registerCommands();
        registerEvents();

        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                prefix + "&dPlugin enabled. Version: " + version));
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                prefix + "&dPlugin disabled."));
    }

    public void registerCommands() {
        MainCommand mainCommand = new MainCommand(this);
        getCommand("crown").setExecutor(mainCommand);
        getCommand("crown").setTabCompleter(mainCommand);
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
    }

    public MainConfigManager getConfigManager() {
        return configManager;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
}