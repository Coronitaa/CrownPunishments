// Modified MainConfigManager.java
/*
 * // MainConfigManager.java
 * // The central command for configuration management, orchestrating the
 * // loading, processing, and retrieval of plugin settings. Like a seasoned
 * // general, it ensures all configurations are in perfect order, now also
 * // including methods for retrieving soft ban configurations and processing
 * // new softban status placeholders.
 */
//MainConfigManager.java

package cp.corona.config;

import cp.corona.crownpunishments.CrownPunishments;
import cp.corona.utils.MessageUtils;
import cp.corona.utils.TimeUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainConfigManager {
    private final CustomConfig configFile;
    private final CrownPunishments plugin;
    private final String defaultTimeUnit; // Store default time unit

    public MainConfigManager(CrownPunishments plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("config.yml", null, plugin, false);
        configFile.registerConfig();
        loadConfig();
        this.defaultTimeUnit = getTimeUnit("default"); // Load default time unit on startup
    }

    /*
     * // loadConfig
     * // Loads configuration from file.
     */
    public void loadConfig() {
        configFile.reloadConfig();
    }

    /*
     * // processPlaceholders
     * // Processes placeholders in a given text, now including softban status placeholders.
     */
    /**
     * Processes placeholders in a given text.
     *
     * @param text   The text to process.
     * @param target The target player, can be null.
     * @return The processed text with placeholders replaced.
     */
    private String processPlaceholders(String text, OfflinePlayer target) {
        String prefix = configFile.getConfig().getString("prefix", "&8[&6C&cP&8] &r");
        text = MessageUtils.getColorMessage(text).replace("{prefix}", prefix);

        if (target == null) return text;

        Player onlineTarget = target.isOnline() ? target.getPlayer() : null;
        String targetName = target.getName() != null ? target.getName() : "Unknown";

        text = text
                .replace("{target}", targetName)
                .replace("{target_online}", target.isOnline() ? "Yes" : "No")
                .replace("{target_ip}", onlineTarget != null && onlineTarget.getAddress() != null ?
                        onlineTarget.getAddress().getHostString() : "-")
                .replace("{target_coords}", onlineTarget != null ?
                        String.format("%d, %d, %d",
                                onlineTarget.getLocation().getBlockX(),
                                onlineTarget.getLocation().getBlockY(),
                                onlineTarget.getLocation().getBlockZ()) : "-")
                .replace("{target_world}", onlineTarget != null ?
                        onlineTarget.getWorld().getName() : "-");

        // New Softban Placeholders - added here
        boolean isSoftBanned = plugin.getSoftBanDatabaseManager().isSoftBanned(target.getUniqueId());
        text = text.replace("{target_softban_status}", isSoftBanned ? "&cSoftBanned" : "&aNot SoftBanned"); // Status placeholder

        if (isSoftBanned) {
            long endTime = plugin.getSoftBanDatabaseManager().getSoftBanEndTime(target.getUniqueId());
            int remainingSeconds = (int) ((endTime - System.currentTimeMillis()) / 1000);
            String remainingTimeFormatted = TimeUtils.formatTime(remainingSeconds, plugin.getConfigManager());
            text = text.replace("{target_softban_remaining_time}", remainingTimeFormatted); // Remaining time placeholder
        } else {
            text = text.replace("{target_softban_remaining_time}", "N/A"); // Or any default text when not softbanned
        }


        if (plugin.isPlaceholderAPIEnabled() && target.isOnline()) {
            text = PlaceholderAPI.setPlaceholders(target.getPlayer(), text);
        }

        return text;
    }

    /*
     * // getMessage
     * // Gets a message from config and processes placeholders.
     */
    public String getMessage(String path, String... replacements) {
        String message = configFile.getConfig().getString(path, "");
        message = processPlaceholders(message, null);

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 >= replacements.length) break;
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        return message;
    }

    /*
     * // getMenuText
     * // Gets menu text from config and processes placeholders.
     */
    public String getMenuText(String path, OfflinePlayer target) {
        String text = configFile.getConfig().getString("menu." + path, "");
        return processPlaceholders(text, target);
    }

    /*
     * // getDetailsMenuText
     * // Gets details menu text from config and processes placeholders.
     */
    public String getDetailsMenuText(String path, OfflinePlayer target, String punishmentType) {
        String text = configFile.getConfig().getString("menu.punish_details." + punishmentType + "." + path, "");
        return processPlaceholders(text, target);
    }

    /*
     * // getMenuItem
     * // Gets a menu item ItemStack from config.
     */
    public ItemStack getMenuItem(String itemKey, OfflinePlayer target) {
        return createItemFromConfig("menu.items." + itemKey, target);
    }

    /*
     * // getDetailsMenuItem
     * // Gets a details menu item ItemStack from config.
     */
    public ItemStack getDetailsMenuItem(String punishmentType, String itemKey, OfflinePlayer target) {
        return createItemFromConfig("menu.punish_details." + punishmentType + ".items." + itemKey, target);
    }

    /*
     * // getTimeOptionMenuItem
     * // Gets a time option menu item ItemStack from config.
     */
    public ItemStack getTimeOptionMenuItem(String itemKey) {
        return createItemFromConfig("menu.time_options." + itemKey, null);
    }

    /*
     * // getTimeSelectorMenuItem
     * // Gets a time selector menu item ItemStack from config.
     */
    public ItemStack getTimeSelectorMenuItem(String itemKey) {
        return createItemFromConfig("menu.time_selector_items." + itemKey, null);
    }

    /*
     * // createItemFromConfig
     * // Creates an ItemStack from config path.
     */
    private ItemStack createItemFromConfig(String configPath, OfflinePlayer target) {
        String materialName = configFile.getConfig().getString(configPath + ".material", "STONE");
        Material material = Material.matchMaterial(materialName);
        if (material == null) return null;

        ItemStack item = new ItemStack(material);

        if (material == Material.PLAYER_HEAD) {
            String playerHeadValue = configFile.getConfig().getString(configPath + ".player_head_value", null);
            if (playerHeadValue != null && !playerHeadValue.isEmpty()) {
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                if (meta != null) {
                    PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID()); // Use random UUID as it's for display
                    PlayerTextures textures = profile.getTextures();
                    try {
                        textures.setSkin(new URL(playerHeadValue));
                    } catch (MalformedURLException e) {
                        plugin.getLogger().warning("Invalid player_head_value URL: " + playerHeadValue + " for path: " + configPath);
                    }
                    profile.setTextures(textures);
                    meta.setOwnerProfile(profile);
                    item.setItemMeta(meta);
                }
            } else {
                item.setType(Material.PLAYER_HEAD); // Ensure type is PLAYER_HEAD
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                if (meta != null) {
                    String playerHead = configFile.getConfig().getString(configPath + ".player_head", "");
                    if (!playerHead.isEmpty() && target != null) {
                        String headOwnerName = processPlaceholders(playerHead, target);
                        OfflinePlayer headOwner = Bukkit.getOfflinePlayer(headOwnerName);
                        meta.setOwningPlayer(headOwner);
                    }
                    item.setItemMeta(meta);
                }
            }
        }


        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = getMenuTextFromPath(configPath + ".name", target);
            meta.setDisplayName(name);
            List<String> lore = getMenuLoreFromPath(configPath + ".lore", target);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /*
     * // getMenuItemSlot
     * // Gets the slot number of a menu item from config.
     */
    public int getMenuItemSlot(String itemKey) {
        return configFile.getConfig().getInt("menu.items." + itemKey + ".slot", 0);
    }

    /*
     * // getDetailsMenuItemSlot
     * // Gets the slot number of a details menu item from config.
     */
    public int getDetailsMenuItemSlot(String punishmentType, String itemKey) {
        return configFile.getConfig().getInt("menu.punish_details." + punishmentType + ".items." + itemKey + ".slot", 0);
    }

    /*
     * // getTimeOptionMenuItemSlot
     * // Gets the slot number of a time option menu item from config.
     */
    public int getTimeOptionMenuItemSlot(String itemKey) {
        return configFile.getConfig().getInt("menu.time_options." + itemKey + ".slot", 0);
    }

    /*
     * // getTimeSelectorMenuItemSlot
     * // Gets the slot number of a time selector menu item from config.
     */
    public int getTimeSelectorMenuItemSlot(String itemKey) {
        return configFile.getConfig().getInt("menu.time_selector_items." + itemKey + ".slot", 0);
    }

    /*
     * // getMenuTextFromPath
     * // Gets menu text from config path and processes placeholders.
     */
    private String getMenuTextFromPath(String path, OfflinePlayer target) {
        return processPlaceholders(configFile.getConfig().getString(path, ""), target);
    }

    /*
     * // getMenuLoreFromPath
     * // Gets menu lore from config path and processes placeholders.
     */
    private List<String> getMenuLoreFromPath(String path, OfflinePlayer target) {
        List<String> lore = new ArrayList<>();
        List<String> configLore = configFile.getConfig().getStringList(path);
        for (String line : configLore) {
            lore.add(processPlaceholders(line, target));
        }
        return lore;
    }

    /*
     * // getDetailsMenuItemLore
     * // Gets details menu item lore from config and processes placeholders with replacements.
     */
    public List<String> getDetailsMenuItemLore(String punishmentType, String itemKey, OfflinePlayer target, String... replacements) {
        List<String> lore = new ArrayList<>();
        List<String> configLore = configFile.getConfig().getStringList("menu.punish_details." + punishmentType + ".items." + itemKey + ".lore");
        for (String line : configLore) {
            String processedLine = processPlaceholders(line, target);
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 >= replacements.length) break;
                processedLine = processedLine.replace(replacements[i], replacements[i + 1]);
            }
            lore.add(processedLine);
        }
        return lore;
    }

    /*
     * // getBanCommand
     * // Gets the ban command from config.
     */
    public String getBanCommand() {
        return configFile.getConfig().getString("commands.ban_command", "ban {target} {time} {reason}");
    }

    /*
     * // getMuteCommand
     * // Gets the mute command from config.
     */
    public String getMuteCommand() {
        return configFile.getConfig().getString("commands.mute_command", "mute {target} {time} {reason}");
    }

    /*
     * // getWarnCommand
     * // Gets the warn command from config.
     */
    public String getWarnCommand() {
        return configFile.getConfig().getString("commands.warn_command", "warn {target} {reason}");
    }

    /*
     * // getSoftBanCommand
     * // Gets the soft ban command from config (though softban is managed internally).
     */
    public String getSoftBanCommand() {
        return configFile.getConfig().getString("commands.softban_command", ""); // Softban is handled internally
    }


    /*
     * // getTimeOptions
     * // Gets the list of time options keys from config.
     */
    public List<String> getTimeOptions() {
        Set<String> keys = configFile.getConfig().getConfigurationSection("menu.time_options").getKeys(false);
        return new ArrayList<>(keys);
    }

    /*
     * // getTimeOptionValue
     * // Gets the value of a specific time option from config.
     */
    public String getTimeOptionValue(String timeOptionKey) {
        return configFile.getConfig().getString("menu.time_options." + timeOptionKey + ".value", "");
    }

    /*
     * // getSoundName
     * // Gets sound name from config by key.
     */
    public String getSoundName(String soundKey) {
        return configFile.getConfig().getString("sounds." + soundKey, "");
    }

    /*
     * // getTimeUnit
     * // Gets the time unit from config by key.
     */
    public String getTimeUnit(String unitKey) {
        return configFile.getConfig().getString("time_units." + unitKey, "");
    }

    /*
     * // getHoursTimeUnit
     * // Gets the time unit for hours from config.
     */
    public String getHoursTimeUnit() {
        return configFile.getConfig().getString("time_units.hours", "h");
    }

    /*
     * // getMinutesTimeUnit
     * // Gets the time unit for minutes from config.
     */
    public String getMinutesTimeUnit() {
        return configFile.getConfig().getString("time_units.minutes", "m");
    }

    /*
     * // getSecondsTimeUnit
     * // Gets the time unit for seconds from config.
     */
    public String getSecondsTimeUnit() {
        return configFile.getConfig().getString("time_units.seconds", "s");
    }

    /*
     * // getDayTimeUnit
     * // Gets the time unit for day from config.
     */
    public String getDayTimeUnit() {
        return configFile.getConfig().getString("time_units.day", "d");
    }

    /*
     * // getYearsTimeUnit
     * // Gets the time unit for year from config.
     */
    public String getYearsTimeUnit() {
        return configFile.getConfig().getString("time_units.years", "y");
    }

    /*
     * // getDefaultTimeUnit
     * // Gets the default time unit.
     */
    public String getDefaultTimeUnit() {
        return this.defaultTimeUnit;
    }

    /*
     * // getBlockedCommands
     * // Gets the blocked commands list from config.
     */
    public List<String> getBlockedCommands() {
        return configFile.getConfig().getStringList("softban.blocked_commands");
    }

    /*
     * // getDatabaseType
     * // Gets the database type from config.
     */
    public String getDatabaseType() {
        return configFile.getConfig().getString("database.type", "sqlite"); // Default to sqlite
    }

    /*
     * // getDatabaseName
     * // Gets the database name from config.
     */
    public String getDatabaseName() {
        return configFile.getConfig().getString("database.name", "crownpunishments");
    }

    /*
     * // getDatabaseAddress
     * // Gets the database address from config.
     */
    public String getDatabaseAddress() {
        return configFile.getConfig().getString("database.address", "localhost");
    }

    /*
     * // getDatabasePort
     * // Gets the database port from config.
     */
    public String getDatabasePort() {
        return configFile.getConfig().getString("database.port", "3306");
    }

    /*
     * // getDatabaseUsername
     * // Gets the database username from config.
     */
    public String getDatabaseUsername() {
        return configFile.getConfig().getString("database.username", "username");
    }

    /*
     * // getDatabasePassword
     * // Gets the database password from config.
     */
    public String getDatabasePassword() {
        return configFile.getConfig().getString("database.password", "password");
    }
}