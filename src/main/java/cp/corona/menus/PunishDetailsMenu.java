// Modified PunishDetailsMenu.java
/*
 * // PunishDetailsMenu.java
 * // The versatile menu for punishment details, dynamically adapting to
 * // various punishment types like ban, mute, and softban, now with added
 * // "Unsoftban" button for easy removal of softban restrictions.
 */
//PunishDetailsMenu.java
package cp.corona.menus;

import cp.corona.crownpunishments.CrownPunishments;
import cp.corona.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Represents the dynamic punishment details menu, now versatile for Ban, Mute, and SoftBan, and includes Unsoftban option.
 * Allows setting specific details for a punishment like time and reason, adaptable by punishment type, now with Unsoftban button.
 */
public class PunishDetailsMenu implements InventoryHolder {
    private final Inventory inventory;
    private final UUID targetUUID;
    private final CrownPunishments plugin;
    private final String punishmentType; // Now dynamic, set in constructor
    private String banTime; // Using banTime as a general time variable for all timed punishments
    private String banReason; // Using banReason as a general reason variable for all punishments
    private boolean timeSet = false;
    private boolean reasonSet = false;

    // Define item keys as constants for menu items in PunishDetailsMenu
    public static final String SET_TIME_KEY = "set_time";
    public static final String SET_REASON_KEY = "set_reason";
    public static final String CONFIRM_PUNISH_KEY = "confirm_punish"; // Reusing confirm key as it serves same purpose
    public static final String BACK_BUTTON_KEY = "back_button";
    public static final String UNSOFTBAN_BUTTON_KEY = "unsoftban_button"; // New button for unsoftban


    public PunishDetailsMenu(UUID targetUUID, CrownPunishments plugin, String punishmentType) {
        this.targetUUID = targetUUID;
        this.plugin = plugin;
        this.punishmentType = punishmentType; // Setting punishmentType dynamically
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        String title = plugin.getConfigManager().getDetailsMenuText("title", target, punishmentType);
        inventory = Bukkit.createInventory(this, 36, title); // Fixed size inventory
        initializeItems();
    }

    /*
     * // initializeItems
     * // Initializes the items in the menu, now including the new "Unsoftban" button.
     */
    /**
     * Initializes the items in the menu.
     */
    private void initializeItems() {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);

        // Set Time Item - only for Ban and Mute (Softban can also have time if needed)
        if (punishmentType.equalsIgnoreCase("ban") || punishmentType.equalsIgnoreCase("mute") || punishmentType.equalsIgnoreCase("softban")) {
            ItemStack setTimeItem = getSetTimeItem(target);
            if (setTimeItem != null) {
                inventory.setItem(plugin.getConfigManager().getDetailsMenuItemSlot(punishmentType, SET_TIME_KEY), setTimeItem);
            }
        }

        // Set Reason Item - for all punishment types
        ItemStack setReasonItem = getSetReasonItem(target);
        if (setReasonItem != null) {
            inventory.setItem(plugin.getConfigManager().getDetailsMenuItemSlot(punishmentType, SET_REASON_KEY), setReasonItem);
        }

        // Confirm Punish Item - Initially disabled until time and reason are set (if applicable)
        ItemStack confirmPunishItem = getConfirmPunishItem(target);
        if (confirmPunishItem != null) {
            inventory.setItem(plugin.getConfigManager().getDetailsMenuItemSlot(punishmentType, CONFIRM_PUNISH_KEY), confirmPunishItem);
        }

        // Back Button Item
        ItemStack backButtonItem = getBackButton(target);
        if (backButtonItem != null) {
            inventory.setItem(plugin.getConfigManager().getDetailsMenuItemSlot(punishmentType, BACK_BUTTON_KEY), backButtonItem);
        }

        // Unsoftban Button - Only for Softban Menu
        if (punishmentType.equalsIgnoreCase("softban")) {
            ItemStack unSoftBanItem = getUnSoftBanButton(target);
            if (unSoftBanItem != null) {
                inventory.setItem(plugin.getConfigManager().getDetailsMenuItemSlot(punishmentType, UNSOFTBAN_BUTTON_KEY), unSoftBanItem);
            }
        }
    }

    /*
     * // getSetTimeItem
     * // Creates the "Set Time" item.
     */
    private ItemStack getSetTimeItem(OfflinePlayer target) {
        ItemStack item = plugin.getConfigManager().getDetailsMenuItem(punishmentType, SET_TIME_KEY, target);
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayTime = this.banTime != null ? this.banTime : plugin.getConfigManager().getMessage("messages.not_set");
            List<String> lore = plugin.getConfigManager().getDetailsMenuItemLore(punishmentType, SET_TIME_KEY, target, "{time}", displayTime);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /*
     * // getSetReasonItem
     * // Creates the "Set Reason" item.
     */
    private ItemStack getSetReasonItem(OfflinePlayer target) {
        ItemStack item = plugin.getConfigManager().getDetailsMenuItem(punishmentType, SET_REASON_KEY, target);
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayReason = this.banReason != null ? this.banReason : plugin.getConfigManager().getMessage("messages.not_set");
            List<String> lore = plugin.getConfigManager().getDetailsMenuItemLore(punishmentType, SET_REASON_KEY, target, "{reason}", displayReason);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /*
     * // getConfirmPunishItem
     * // Creates the "Confirm Punish" item.
     */
    private ItemStack getConfirmPunishItem(OfflinePlayer target) {
        ItemStack item = plugin.getConfigManager().getDetailsMenuItem(punishmentType, CONFIRM_PUNISH_KEY, target);
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (!timeSet || !reasonSet) {
                meta.setDisplayName(plugin.getConfigManager().getDetailsMenuText("items." + punishmentType + "." + CONFIRM_PUNISH_KEY + ".disabled_name", target, punishmentType));
                List<String> lore = plugin.getConfigManager().getDetailsMenuItemLore(punishmentType, CONFIRM_PUNISH_KEY, target,
                        "{time_status}", getTimeStatusText(),
                        "{reason_status}", getReasonStatusText());
                meta.setLore(lore);
            } else {
                meta.setDisplayName(plugin.getConfigManager().getDetailsMenuText("items." + punishmentType + "." + CONFIRM_PUNISH_KEY + ".name", target, punishmentType));
                List<String> lore = plugin.getConfigManager().getDetailsMenuItemLore(punishmentType, CONFIRM_PUNISH_KEY, target,
                        "{time_status}", getTimeStatusText(),
                        "{reason_status}", getReasonStatusText());
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /*
     * // getBackButton
     * // Gets the "Back Button" item.
     */
    private ItemStack getBackButton(OfflinePlayer target) {
        return plugin.getConfigManager().getDetailsMenuItem(punishmentType, BACK_BUTTON_KEY, target);
    }

    /*
     * // getUnSoftBanButton
     * // Creates the "Unsoftban" button, specifically for the SoftBanDetailsMenu.
     */
    private ItemStack getUnSoftBanButton(OfflinePlayer target) {
        return plugin.getConfigManager().getDetailsMenuItem(
                "softban", // Tipo explícito para softban
                UNSOFTBAN_BUTTON_KEY,
                target
        );
    }

    /*
     * // getTimeStatusText
     * // Gets the formatted status text for time.
     */
    private String getTimeStatusText() {
        return timeSet ? MessageUtils.getColorMessage("&a\u2705 Set") : MessageUtils.getColorMessage("&c\u274c Not Set");
    }

    /*
     * // getReasonStatusText
     * // Gets the formatted status text for reason.
     */
    private String getReasonStatusText() {
        return reasonSet ? MessageUtils.getColorMessage("&a\u2705 Set") : MessageUtils.getColorMessage("&c\u274c Not Set");
    }

    /*
     * // getInventory
     * // Gets the Inventory for this menu.
     */
    @Override
    @NotNull
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
     * // getPunishmentType
     * // Gets the punishment type.
     */
    public String getPunishmentType() {
        return punishmentType;
    }

    /*
     * // getBanTime
     * // Gets the ban time.
     */
    public String getBanTime() {
        return banTime;
    }

    /*
     * // setBanTime
     * // Sets the ban time and updates menu items.
     */
    public void setBanTime(String banTime) {
        this.banTime = banTime;
        this.timeSet = true;
        updateSetTimeItem();
        updateConfirmButtonStatus();
    }

    /*
     * // getBanReason
     * // Gets the ban reason.
     */
    public String getBanReason() {
        return banReason;
    }

    /*
     * // setBanReason
     * // Sets the ban reason and updates menu items.
     */
    public void setBanReason(String banReason) {
        this.banReason = banReason;
        this.reasonSet = true;
        updateSetReasonItem();
        updateConfirmButtonStatus();
    }

    /*
     * // getTargetUUID
     * // Gets the target player UUID.
     */
    public UUID getTargetUUID() {
        return targetUUID;
    }

    /*
     * // isTimeSet
     * // Checks if time is set.
     */
    public boolean isTimeSet() {
        return timeSet;
    }

    /*
     * // isReasonSet
     * // Checks if reason is set.
     */
    public boolean isReasonSet() {
        return reasonSet;
    }

    /*
     * // updateSetTimeItem
     * // Updates the "Set Time" item in the menu.
     */
    public void updateSetTimeItem() {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        ItemStack setTimeItem = getSetTimeItem(target);
        if (setTimeItem != null) {
            inventory.setItem(plugin.getConfigManager().getDetailsMenuItemSlot(punishmentType, SET_TIME_KEY), setTimeItem);
        }
    }

    /*
     * // updateSetReasonItem
     * // Updates the "Set Reason" item in the menu.
     */
    public void updateSetReasonItem() {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        ItemStack setReasonItem = getSetReasonItem(target);
        if (setReasonItem != null) {
            inventory.setItem(plugin.getConfigManager().getDetailsMenuItemSlot(punishmentType, SET_REASON_KEY), setReasonItem);
        }
    }

    /*
     * // updateConfirmButtonStatus
     * // Updates the "Confirm Punish" button based on whether time and reason are set.
     */
    public void updateConfirmButtonStatus() {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        ItemStack confirmPunishItem = getConfirmPunishItem(target);
        if (confirmPunishItem != null) {
            inventory.setItem(plugin.getConfigManager().getDetailsMenuItemSlot(punishmentType, CONFIRM_PUNISH_KEY), confirmPunishItem);
        }
    }
}