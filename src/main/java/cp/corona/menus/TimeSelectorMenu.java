// Modified TimeSelectorMenu.java
/*
 * // TimeSelectorMenu.java
 * // The chronomancer of menus, allowing precise selection of punishment
 * // durations. Like a time-bending device, it offers controls to adjust time,
 * // ensuring punishments are perfectly calibrated to fit the offense, now
 * // with more efficient time increment/decrement buttons.
 */
//TimeSelectorMenu.java
package cp.corona.menus;

import cp.corona.crownpunishments.CrownPunishments;
import cp.corona.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Menu for selecting predefined ban times or choosing a custom time.
 * Now with more efficient time increment/decrement buttons: +15m, +6h, +1d, +7d and -5m, -2h, -1d, -5d.
 */
public class TimeSelectorMenu implements InventoryHolder {
    private final Inventory inventory;
    private final PunishDetailsMenu punishDetailsMenu; // Changed back to PunishDetailsMenu - assuming TimeSelectorMenu is primarily used with PunishDetailsMenu
    private final CrownPunishments plugin;
    private int currentTimeSeconds = 0;
    private ItemStack timeDisplayItem;

    // Define item keys as constants for menu items in TimeSelectorMenu - UPDATED INCREMENTS
    private static final String MINUS_5_DAY_KEY = "minus_5_day"; // Changed from minus_7_day to minus_5_day
    private static final String MINUS_1_DAY_KEY = "minus_1_day";
    private static final String MINUS_2_HOUR_KEY = "minus_2_hour"; // Changed from minus_30_min to minus_2_hour
    private static final String MINUS_5_MIN_KEY = "minus_5_min";
    private static final String TIME_DISPLAY_KEY = "time_display";
    private static final String PLUS_15_MIN_KEY = "plus_15_min"; // Changed from plus_5_min to plus_15_min
    private static final String PLUS_6_HOUR_KEY = "plus_6_hour"; // Changed from plus_30_min to plus_6_hour
    private static final String PLUS_1_DAY_KEY = "plus_1_day";
    private static final String PLUS_7_DAY_KEY = "plus_7_day";


    private static final String PERMANENT_TIME_KEY = "permanent";
    private static final String CUSTOM_TIME_KEY = "custom";


    private final List<String> timeSelectorItemKeys = Arrays.asList(
            MINUS_5_DAY_KEY, MINUS_1_DAY_KEY, MINUS_2_HOUR_KEY, MINUS_5_MIN_KEY, // Updated keys
            TIME_DISPLAY_KEY,
            PLUS_15_MIN_KEY, PLUS_6_HOUR_KEY, PLUS_1_DAY_KEY, PLUS_7_DAY_KEY, // Updated keys
            PERMANENT_TIME_KEY, CUSTOM_TIME_KEY
    );

    public TimeSelectorMenu(PunishDetailsMenu punishDetailsMenu, CrownPunishments plugin) { // Changed back to PunishDetailsMenu
        this.punishDetailsMenu = punishDetailsMenu;
        this.plugin = plugin;
        String title = plugin.getConfigManager().getMenuText("time_selector_title", Bukkit.getOfflinePlayer(punishDetailsMenu.getTargetUUID())); // No cast needed now
        inventory = Bukkit.createInventory(this, 36, title); // Using a fixed size inventory (36 slots = 4 rows)
        initializeItems();
    }

    /*
     * // initializeItems
     * // The menu item initializer, populating the TimeSelectorMenu with interactive items,
     * // now with more efficient time increment/decrement buttons initialized.
     */
    private void initializeItems() {
        // Time adjustment buttons (Negative - Left side) - UPDATED INCREMENTS
        inventory.setItem(plugin.getConfigManager().getTimeSelectorMenuItemSlot(MINUS_5_DAY_KEY), plugin.getConfigManager().getTimeSelectorMenuItem(MINUS_5_DAY_KEY)); // Updated key
        inventory.setItem(plugin.getConfigManager().getTimeSelectorMenuItemSlot(MINUS_1_DAY_KEY), plugin.getConfigManager().getTimeSelectorMenuItem(MINUS_1_DAY_KEY));
        inventory.setItem(plugin.getConfigManager().getTimeSelectorMenuItemSlot(MINUS_2_HOUR_KEY), plugin.getConfigManager().getTimeSelectorMenuItem(MINUS_2_HOUR_KEY)); // Updated key
        inventory.setItem(plugin.getConfigManager().getTimeSelectorMenuItemSlot(MINUS_5_MIN_KEY), plugin.getConfigManager().getTimeSelectorMenuItem(MINUS_5_MIN_KEY));

        // Time display item (Center)
        timeDisplayItem = getTimeDisplayItem();
        inventory.setItem(plugin.getConfigManager().getTimeSelectorMenuItemSlot(TIME_DISPLAY_KEY), timeDisplayItem);

        // Time adjustment buttons (Positive - Right side) - UPDATED INCREMENTS
        inventory.setItem(plugin.getConfigManager().getTimeSelectorMenuItemSlot(PLUS_15_MIN_KEY), plugin.getConfigManager().getTimeSelectorMenuItem(PLUS_15_MIN_KEY)); // Updated key
        inventory.setItem(plugin.getConfigManager().getTimeSelectorMenuItemSlot(PLUS_6_HOUR_KEY), plugin.getConfigManager().getTimeSelectorMenuItem(PLUS_6_HOUR_KEY)); // Updated key
        inventory.setItem(plugin.getConfigManager().getTimeSelectorMenuItemSlot(PLUS_1_DAY_KEY), plugin.getConfigManager().getTimeSelectorMenuItem(PLUS_1_DAY_KEY));
        inventory.setItem(plugin.getConfigManager().getTimeSelectorMenuItemSlot(PLUS_7_DAY_KEY), plugin.getConfigManager().getTimeSelectorMenuItem(PLUS_7_DAY_KEY));

        // Permanent and Custom Time buttons (Bottom row)
        inventory.setItem(plugin.getConfigManager().getTimeSelectorMenuItemSlot(PERMANENT_TIME_KEY), plugin.getConfigManager().getTimeSelectorMenuItem(PERMANENT_TIME_KEY));
        inventory.setItem(plugin.getConfigManager().getTimeSelectorMenuItemSlot(CUSTOM_TIME_KEY), plugin.getConfigManager().getTimeSelectorMenuItem(CUSTOM_TIME_KEY));
    }

    /*
     * // getTimeDisplayItem
     * // Creates the time display item.
     */
    private ItemStack getTimeDisplayItem() {
        ItemStack item = plugin.getConfigManager().getTimeSelectorMenuItem(TIME_DISPLAY_KEY);
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(plugin.getConfigManager().getDetailsMenuItemLore(punishDetailsMenu.getPunishmentType(), "set_time", Bukkit.getOfflinePlayer(punishDetailsMenu.getTargetUUID()), "{time}", getFormattedTime()));
            item.setItemMeta(meta);
        }
        return item;
    }

    /*
     * // updateTimeDisplayItem
     * // Updates the time display item in the menu with the current time.
     */
    public void updateTimeDisplayItem(Player player) {
        timeDisplayItem = getTimeDisplayItem();
        inventory.setItem(plugin.getConfigManager().getTimeSelectorMenuItemSlot(TIME_DISPLAY_KEY), timeDisplayItem); // Use slot from config
        player.updateInventory();
    }


    /*
     * // adjustTime
     * // Adjusts the current time by a specified amount in seconds.
     */
    public void adjustTime(int seconds) {
        this.currentTimeSeconds += seconds;
        if (currentTimeSeconds < 0) {
            currentTimeSeconds = 0;
        }
    }

    /*
     * // getCurrentTimeSeconds
     * // Gets the current time in seconds.
     */
    public int getCurrentTimeSeconds() {
        return currentTimeSeconds;
    }

    /*
     * // getFormattedTime
     * // Gets the formatted time string from current time in seconds.
     */
    public String getFormattedTime() {
        return TimeUtils.formatTime(currentTimeSeconds, plugin.getConfigManager());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /*
     * // open
     * // Opens the menu for a player.
     */
    public void open(Player player) {
        player.openInventory(inventory);
    }

    /*
     * // getPunishDetailsMenu
     * // Gets the PunishDetailsMenu associated with this TimeSelectorMenu.
     */
    public PunishDetailsMenu getPunishDetailsMenu() {
        return punishDetailsMenu;
    }

    /*
     * // getTimeSelectorItemKeys
     * // Gets the list of time selector item keys.
     */
    public List<String> getTimeSelectorItemKeys() {
        return timeSelectorItemKeys;
    }
}