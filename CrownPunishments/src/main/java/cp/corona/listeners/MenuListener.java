package cp.corona.listeners;

import cp.corona.crownpunishments.CrownPunishments;
import cp.corona.menus.PunishMenu;
import cp.corona.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

import static cp.corona.crownpunishments.CrownPunishments.prefix;

public class MenuListener implements Listener {
    private final CrownPunishments plugin;
    private final HashMap<UUID, BukkitTask> inputTimeouts = new HashMap<>();

    public MenuListener(CrownPunishments plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof PunishMenu) {
            event.setCancelled(true);

            if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) return;

            Player player = (Player) event.getWhoClicked();
            PunishMenu punishMenu = (PunishMenu) holder;

            // Get configured slot from config
            int infoSlot = plugin.getConfigManager().getConfigFile().getConfig().getInt("menu.items.info.slot", 10);

            if (event.getRawSlot() == infoSlot) { // Dynamic slot check
                player.closeInventory();
                player.sendMessage("§aEnter a new player name (or type 'cancel' to cancel):");

                if (inputTimeouts.containsKey(player.getUniqueId())) {
                    inputTimeouts.get(player.getUniqueId()).cancel();
                }

                BukkitTask timeoutTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendMessage("§cTime expired. Punish menu cancelled.");
                        inputTimeouts.remove(player.getUniqueId());
                    }
                }.runTaskLater(plugin, 400L);

                inputTimeouts.put(player.getUniqueId(), timeoutTask);
            }
        }
    }
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        //Verificar si está en la lista de espera de input
        if (inputTimeouts.containsKey(player.getUniqueId())) {
            event.setCancelled(true); // Cancelar el mensaje normal
            String input = event.getMessage();

            // Cancelar el timeout
            inputTimeouts.get(player.getUniqueId()).cancel();
            inputTimeouts.remove(player.getUniqueId());

            if (input.equalsIgnoreCase("cancel")) {
                player.sendMessage("§cPunish menu cancelled.");
                return; // No hacer nada más
            }

            OfflinePlayer newTarget = Bukkit.getOfflinePlayer(input); // Obtener el nuevo target
            if (!newTarget.hasPlayedBefore() && !newTarget.isOnline())
            {
                player.sendMessage(MessageUtils.getColorMessage(prefix + "&cPlayer '" + input + "' has never played before."));

            } else {
                //abrir menu nuevo
                Bukkit.getScheduler().runTask(plugin, ()-> new PunishMenu(newTarget.getUniqueId(), plugin).open(player)); // Abrir el menú para el nuevo target DE FORMA SEGURA.
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        //Limpiar la lista de timeouts si el jugador cierra el menu.
        if (holder instanceof PunishMenu) {
            Player player = (Player) event.getPlayer();
            if (inputTimeouts.containsKey(player.getUniqueId())) {
                inputTimeouts.get(player.getUniqueId()).cancel();
                inputTimeouts.remove(player.getUniqueId());
            }
        }
    }
}