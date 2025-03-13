package cp.corona.config;

import cp.corona.crownpunishments.CrownPunishments;
import cp.corona.utils.MessageUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainConfigManager {
    private final CustomConfig configFile;
    private final CrownPunishments plugin;

    public MainConfigManager(CrownPunishments plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("config.yml", null, plugin, false);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        configFile.reloadConfig();
    }

    // Helper method to replace ALL internal placeholders (null-safe)
    private String processPlaceholders(String text, OfflinePlayer target) {
        String prefix = configFile.getConfig().getString("prefix", "&8[&6C&cP&8] &r");
        text = MessageUtils.getColorMessage(text).replace("{prefix}", prefix);

        if (target == null) return text; // Skip target-dependent replacements

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

        // Apply PlaceholderAPI if target is online
        if (plugin.isPlaceholderAPIEnabled() && target.isOnline()) {
            text = PlaceholderAPI.setPlaceholders(target.getPlayer(), text);
        }

        return text;
    }

    public String getMessage(String path, String... replacements) {
        String message = configFile.getConfig().getString(path, "");
        message = processPlaceholders(message, null); // Safe for non-target messages

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 >= replacements.length) break;
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        return message;
    }

    public String getMenuText(String path, OfflinePlayer target) {
        String text = configFile.getConfig().getString("menu." + path, "");
        return processPlaceholders(text, target);
    }

    public ItemStack getMenuItem(String itemKey, OfflinePlayer target) {
        ItemStack item;
        String materialName = configFile.getConfig().getString("menu.items." + itemKey + ".material", "STONE");
        Material material = Material.matchMaterial(materialName);

        if (material == null) material = Material.STONE;

        if (material == Material.PLAYER_HEAD) {
            String playerHead = configFile.getConfig().getString("menu.items." + itemKey + ".player_head", "");
            item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();

            if (!playerHead.isEmpty()) {
                String headOwnerName = playerHead.replace("{target}", target.getName());
                OfflinePlayer headOwner = Bukkit.getOfflinePlayer(headOwnerName);
                meta.setOwningPlayer(headOwner);
            }

            item.setItemMeta(meta);
        } else {
            item = new ItemStack(material);
        }

        ItemMeta meta = item.getItemMeta();
        String name = getMenuText("items." + itemKey + ".name", target);
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        for (String line : configFile.getConfig().getStringList("menu.items." + itemKey + ".lore")) {
            lore.add(processPlaceholders(line, target));
        }
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public CustomConfig getConfigFile() {
        return configFile;
    }
}