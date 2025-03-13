package cp.corona.menus;

import cp.corona.crownpunishments.CrownPunishments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class PunishMenu implements InventoryHolder {
    private final Inventory inventory;
    private final UUID targetUUID;
    private final CrownPunishments plugin;

    public PunishMenu(UUID targetUUID, CrownPunishments plugin) {
        this.targetUUID = targetUUID;
        this.plugin = plugin;
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        String title = plugin.getConfigManager().getMenuText("title", target);
        inventory = Bukkit.createInventory(this, 54, title);
        initializeItems();
    }

    private void initializeItems() {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);

        // Load info item from config
        int slot = plugin.getConfigManager().getConfigFile().getConfig().getInt("menu.items.info.slot", 10);
        inventory.setItem(slot, plugin.getConfigManager().getMenuItem("info", target));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}