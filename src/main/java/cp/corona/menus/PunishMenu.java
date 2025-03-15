// Modified PunishMenu.java
/*
 * // PunishMenu.java
 * // The gateway to justice, presenting the main punishment options to users.
 * // Like a judge's bench, it offers choices for different penalties, guiding
 * // administrators in their disciplinary actions with clarity and purpose,
 * // now streamlined and focused on ban, mute, and softban options.
 */
//PunishMenu.java
package cp.corona.menus;

import cp.corona.crownpunishments.CrownPunishments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Represents the main punishment menu.
 * Allows selecting different punishment categories: Ban, Mute, and SoftBan.
 */
public class PunishMenu implements InventoryHolder {
    private final Inventory inventory;
    private final UUID targetUUID;
    private final CrownPunishments plugin;

    // Define item keys as constants for menu items in PunishMenu
    private static final String INFO_ITEM_KEY = "info";
    private static final String BAN_ITEM_KEY = "ban";
    private static final String MUTE_ITEM_KEY = "mute";
    private static final String SOFTBAN_ITEM_KEY = "softban"; // New item for softban


    public PunishMenu(UUID targetUUID, CrownPunishments plugin) {
        this.targetUUID = targetUUID;
        this.plugin = plugin;
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        String title = plugin.getConfigManager().getMenuText("title", target);
        inventory = Bukkit.createInventory(this, 54, title); // Using a fixed size inventory (54 slots = 6 rows)
        initializeItems();
    }

    /*
     * // initializeItems
     * // The menu item initializer, populating the PunishMenu with core
     * // punishment options: "Info," "Ban," "Mute," and "SoftBan," setting up
     * // the main interface for punishment selection.
     */
    /**
     * Initializes the items in the menu.
     */
    private void initializeItems() {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);

        // Info Item
        inventory.setItem(plugin.getConfigManager().getMenuItemSlot(INFO_ITEM_KEY),
                plugin.getConfigManager().getMenuItem(INFO_ITEM_KEY, target));

        // Ban Item
        inventory.setItem(plugin.getConfigManager().getMenuItemSlot(BAN_ITEM_KEY),
                plugin.getConfigManager().getMenuItem(BAN_ITEM_KEY, target));

        // Mute Item
        inventory.setItem(plugin.getConfigManager().getMenuItemSlot(MUTE_ITEM_KEY),
                plugin.getConfigManager().getMenuItem(MUTE_ITEM_KEY, target));

        // SoftBan Item
        inventory.setItem(plugin.getConfigManager().getMenuItemSlot(SOFTBAN_ITEM_KEY), // New SoftBan Item
                plugin.getConfigManager().getMenuItem(SOFTBAN_ITEM_KEY, target));
    }

    /*
     * // getInventory
     * // The inventory accessor, providing the Inventory object for this menu.
     */
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /*
     * // open
     * // The menu opener, displaying the PunishMenu to a player.
     */
    /**
     * Opens the menu for a player.
     *
     * @param player The player to open the menu for.
     */
    public void open(Player player) {
        player.openInventory(inventory);
    }

    /*
     * // getTargetUUID
     * // The target UUID accessor, retrieving the UUID of the player being punished.
     */
    /**
     * Gets the target player UUID.
     *
     * @return The target player UUID.
     */
    public UUID getTargetUUID() {
        return targetUUID;
    }
}