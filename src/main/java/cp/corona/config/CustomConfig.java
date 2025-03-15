package cp.corona.config;

import cp.corona.crownpunishments.CrownPunishments;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class CustomConfig {
    private final CrownPunishments plugin; // Made final
    private final String fileName; // Made final
    private FileConfiguration fileConfiguration = null;
    private File file = null;
    private final String folderName; // Made final
    private final boolean newFile; // Made final

    /**
     * Constructor for CustomConfig.
     *
     * @param fileName   The name of the file.
     * @param folderName The folder name where the file is located (null if in plugin's data folder root).
     * @param plugin     Instance of the main plugin class.
     * @param newFile    Whether to create a new file if it doesn't exist.
     */
    public CustomConfig(String fileName, String folderName, CrownPunishments plugin, boolean newFile) {
        this.fileName = fileName;
        this.folderName = folderName;
        this.plugin = plugin;
        this.newFile = newFile;
    }


    public void registerConfig() {
        if (folderName != null) {
            file = new File(plugin.getDataFolder() + File.separator + folderName, fileName);
        } else {
            file = new File(plugin.getDataFolder(), fileName);
        }

        if (!file.exists()) {
            if (newFile) {
                try {
                    boolean created = file.createNewFile(); // Check result
                    if (!created) {
                        plugin.getLogger().log(Level.WARNING, "Failed to create new file: " + fileName); // More robust logging
                    }
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error creating new file: " + fileName, e); // More robust logging
                }
            } else {
                if (folderName != null) {
                    plugin.saveResource(folderName + File.separator + fileName, false);
                } else {
                    plugin.saveResource(fileName, false);
                }
            }
        }

        fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading config file: " + fileName, e); // More robust logging
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Invalid configuration in file: " + fileName, e); // More robust logging
        }
    }

    /**
     * Reloads the configuration from file.
     *
     * @return true if reload was successful.
     */
    public boolean reloadConfig() {
        if (fileConfiguration == null) {
            if (folderName != null) {
                file = new File(plugin.getDataFolder() + File.separator + folderName, fileName);
            } else {
                file = new File(plugin.getDataFolder(), fileName);
            }
        }
        fileConfiguration = YamlConfiguration.loadConfiguration(file);

        if (file != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(file);
            fileConfiguration.setDefaults(defConfig);
        }
        return true;
    }

    /**
     * Gets the FileConfiguration associated with this CustomConfig.
     *
     * @return The FileConfiguration.
     */
    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            reloadConfig();
        }
        return fileConfiguration;
    }
}