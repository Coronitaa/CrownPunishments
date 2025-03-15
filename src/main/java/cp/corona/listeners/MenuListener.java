package cp.corona.listeners;

import cp.corona.crownpunishments.CrownPunishments;
import cp.corona.menus.PunishDetailsMenu;
import cp.corona.menus.PunishMenu;
import cp.corona.menus.TimeSelectorMenu;
import cp.corona.utils.MessageUtils;
import cp.corona.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import static cp.corona.menus.PunishDetailsMenu.*;

public class MenuListener implements Listener {
    private final CrownPunishments plugin;
    private final HashMap<UUID, BukkitTask> inputTimeouts = new HashMap<>();
    private final HashMap<UUID, PunishDetailsMenu> pendingDetailsMenus = new HashMap<>();
    private final HashMap<UUID, String> inputTypes = new HashMap<>();

    private static final String BAN_PUNISHMENT_TYPE = "ban";
    private static final String MUTE_PUNISHMENT_TYPE = "mute";
    private static final String SOFTBAN_PUNISHMENT_TYPE = "softban";
    private static final String PERMANENT_TIME_KEY = "permanent";
    private static final String CUSTOM_TIME_KEY = "custom";
    private static final String INFO_ITEM_KEY = "info";
    private static final String BAN_ITEM_MENU_KEY = "ban";
    private static final String MUTE_ITEM_MENU_KEY = "mute";
    private static final String SOFTBAN_ITEM_MENU_KEY = "softban";

    public MenuListener(CrownPunishments plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) return;

        playSound(player, "menu_click");

        if (holder instanceof PunishMenu) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            handlePunishMenuClick(event, player, event.getCurrentItem(), (PunishMenu) holder);

        } else if (holder instanceof PunishDetailsMenu) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            handlePunishDetailsMenuClick(event, player, event.getCurrentItem(), (PunishDetailsMenu) holder);

        } else if (holder instanceof TimeSelectorMenu) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            handleTimeSelectorMenuClick(event, player, event.getCurrentItem(), (TimeSelectorMenu) holder);
        }
    }

    private void handlePunishMenuClick(InventoryClickEvent event, Player player, ItemStack clickedItem, PunishMenu punishMenu) {
        UUID targetUUID = punishMenu.getTargetUUID();

        if (event.getRawSlot() == plugin.getConfigManager().getMenuItemSlot(INFO_ITEM_KEY)) {
            player.closeInventory();
            requestNewTargetName(player);
        } else if (event.getRawSlot() == plugin.getConfigManager().getMenuItemSlot(BAN_ITEM_MENU_KEY)) {
            new PunishDetailsMenu(targetUUID, plugin, BAN_PUNISHMENT_TYPE).open(player);
        } else if (event.getRawSlot() == plugin.getConfigManager().getMenuItemSlot(MUTE_ITEM_MENU_KEY)) {
            new PunishDetailsMenu(targetUUID, plugin, MUTE_PUNISHMENT_TYPE).open(player);
        } else if (event.getRawSlot() == plugin.getConfigManager().getMenuItemSlot(SOFTBAN_ITEM_MENU_KEY)) {
            new PunishDetailsMenu(targetUUID, plugin, SOFTBAN_PUNISHMENT_TYPE).open(player);
        }
    }

    private void handlePunishDetailsMenuClick(InventoryClickEvent event, Player player, ItemStack clickedItem, PunishDetailsMenu punishDetailsMenu) {
        String punishmentType = punishDetailsMenu.getPunishmentType();

        if (event.getRawSlot() == plugin.getConfigManager().getDetailsMenuItemSlot(punishmentType, SET_TIME_KEY)) {
            new TimeSelectorMenu(punishDetailsMenu, plugin).open(player);
        } else if (event.getRawSlot() == plugin.getConfigManager().getDetailsMenuItemSlot(punishmentType, SET_REASON_KEY)) {
            requestReasonInput(player, punishDetailsMenu);
        } else if (event.getRawSlot() == plugin.getConfigManager().getDetailsMenuItemSlot(punishmentType, CONFIRM_PUNISH_KEY)) {
            handleConfirmButtonClick(player, punishDetailsMenu);
        } else if (event.getRawSlot() == plugin.getConfigManager().getDetailsMenuItemSlot(punishmentType, BACK_BUTTON_KEY)) {
            new PunishMenu(punishDetailsMenu.getTargetUUID(), plugin).open(player);
        } else if (event.getRawSlot() == plugin.getConfigManager().getDetailsMenuItemSlot(punishmentType, UNSOFTBAN_BUTTON_KEY)) {
            handleUnsoftbanButtonClick(player, punishDetailsMenu);
        }
    }

    private void handleConfirmButtonClick(Player player, PunishDetailsMenu punishDetailsMenu) {
        if (punishDetailsMenu.isTimeSet() && punishDetailsMenu.isReasonSet()) {
            confirmDynamicPunishment(player, punishDetailsMenu);
        } else {
            sendValidationMessages(player, punishDetailsMenu);
        }
    }

    private void handleUnsoftbanButtonClick(Player player, PunishDetailsMenu punishDetailsMenu) {
        UUID targetUUID = punishDetailsMenu.getTargetUUID();
        if (plugin.getSoftBanDatabaseManager().isSoftBanned(targetUUID)) {
            confirmUnsoftban(player, punishDetailsMenu);
        } else {
            player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.no_active_softban")));
        }
    }

    private void sendValidationMessages(Player player, PunishDetailsMenu punishDetailsMenu) {
        if (!punishDetailsMenu.isTimeSet() && !punishDetailsMenu.isReasonSet()) {
            player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.set_time_reason_before_confirm")));
        } else if (!punishDetailsMenu.isTimeSet()) {
            player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.set_time_before_confirm")));
        } else if (!punishDetailsMenu.isReasonSet()) {
            player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.set_reason_before_confirm")));
        }
    }

    private void handleTimeSelectorMenuClick(InventoryClickEvent event, Player player, ItemStack clickedItem, TimeSelectorMenu timeSelectorMenu) {
        PunishDetailsMenu detailsMenu = timeSelectorMenu.getPunishDetailsMenu();
        String itemKey = getClickedItemKey(timeSelectorMenu, event.getRawSlot());

        if (itemKey != null) {
            handleTimeAdjustment(itemKey, timeSelectorMenu, player, detailsMenu);
        }
    }

    private String getClickedItemKey(TimeSelectorMenu menu, int slot) {
        for (String key : menu.getTimeSelectorItemKeys()) {
            if (slot == plugin.getConfigManager().getTimeSelectorMenuItemSlot(key)) {
                return key;
            }
        }
        return null;
    }

    private void handleTimeAdjustment(String itemKey, TimeSelectorMenu menu, Player player, PunishDetailsMenu detailsMenu) {
        switch (itemKey) {
            case CUSTOM_TIME_KEY:
                player.closeInventory();
                requestCustomTimeInput(player, detailsMenu);
                break;
            case PERMANENT_TIME_KEY:
                setPermanentTime(detailsMenu, player);
                break;
            case "time_display":
                handleTimeDisplayClick(menu, detailsMenu, player);
                break;
            case "minus_5_min":
                menu.adjustTime(-300);
                break;
            case "minus_2_hour":
                menu.adjustTime(-7200);
                break;
            case "minus_1_day":
                menu.adjustTime(-86400);
                break;
            case "minus_5_day":
                menu.adjustTime(-432000);
                break;
            case "plus_15_min":
                menu.adjustTime(900);
                break;
            case "plus_6_hour":
                menu.adjustTime(21600);
                break;
            case "plus_1_day":
                menu.adjustTime(86400);
                break;
            case "plus_7_day":
                menu.adjustTime(604800);
                break;
        }
        if (!itemKey.equals(PERMANENT_TIME_KEY) && !itemKey.equals(CUSTOM_TIME_KEY)) {
            menu.updateTimeDisplayItem(player);
        }
    }

    private void setPermanentTime(PunishDetailsMenu detailsMenu, Player player) {
        detailsMenu.setBanTime("Permanent");
        detailsMenu.updateSetTimeItem();
        detailsMenu.updateConfirmButtonStatus();
        detailsMenu.open(player);
    }

    private void handleTimeDisplayClick(TimeSelectorMenu menu, PunishDetailsMenu detailsMenu, Player player) {
        if (menu.getCurrentTimeSeconds() > 0) {
            detailsMenu.setBanTime(menu.getFormattedTime());
            detailsMenu.updateSetTimeItem();
            detailsMenu.updateConfirmButtonStatus();
            detailsMenu.open(player);
        } else {
            player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.set_valid_time_confirm")));
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        Player player = (Player) event.getPlayer();
        if (holder instanceof PunishMenu || holder instanceof PunishDetailsMenu || holder instanceof TimeSelectorMenu) {
            playSound(player, "menu_open");
        }
    }

    private void requestNewTargetName(Player player) {
        player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.prompt_new_target")));
        setupChatInputTimeout(player, null, "new_target");
    }

    private void requestReasonInput(Player player, PunishDetailsMenu punishDetailsMenu) {
        player.closeInventory();
        player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.prompt_" + punishDetailsMenu.getPunishmentType() + "_reason")));
        pendingDetailsMenus.put(player.getUniqueId(), punishDetailsMenu);
        setupChatInputTimeout(player, punishDetailsMenu, "ban_reason");
    }

    private void requestCustomTimeInput(Player player, PunishDetailsMenu punishDetailsMenu) {
        player.closeInventory();
        player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.prompt_custom_time")));
        pendingDetailsMenus.put(player.getUniqueId(), punishDetailsMenu);
        setupChatInputTimeout(player, punishDetailsMenu, "custom_time");
    }

    private void setupChatInputTimeout(Player player, PunishDetailsMenu menu, String inputType) {
        cancelExistingTimeout(player);

        BukkitTask timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                handleInputTimeout(player);
            }
        }.runTaskLater(plugin, 400L);

        storeInputData(player, timeoutTask, menu, inputType);
    }

    private void cancelExistingTimeout(Player player) {
        if (inputTimeouts.containsKey(player.getUniqueId())) {
            inputTimeouts.get(player.getUniqueId()).cancel();
        }
    }

    private void handleInputTimeout(Player player) {
        player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.input_timeout")));
        clearPlayerInputData(player);
    }

    private void storeInputData(Player player, BukkitTask task, PunishDetailsMenu menu, String inputType) {
        inputTimeouts.put(player.getUniqueId(), task);
        pendingDetailsMenus.put(player.getUniqueId(), menu);
        inputTypes.put(player.getUniqueId(), inputType);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!inputTimeouts.containsKey(player.getUniqueId())) return;

        event.setCancelled(true);
        handlePlayerInput(player, event.getMessage());
    }

    private void handlePlayerInput(Player player, String input) {
        cancelInputTimeout(player);

        PunishDetailsMenu detailsMenu = pendingDetailsMenus.remove(player.getUniqueId());
        String inputType = inputTypes.remove(player.getUniqueId());

        if (input.equalsIgnoreCase("cancel")) {
            handleCancelInput(player, detailsMenu);
            return;
        }

        processValidInput(player, input, detailsMenu, inputType);
    }

    private void cancelInputTimeout(Player player) {
        BukkitTask timeoutTask = inputTimeouts.remove(player.getUniqueId());
        if (timeoutTask != null) timeoutTask.cancel();
    }

    private void handleCancelInput(Player player, PunishDetailsMenu detailsMenu) {
        player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.input_cancelled")));
        if (detailsMenu != null) {
            Bukkit.getScheduler().runTask(plugin, () -> detailsMenu.open(player));
        }
    }

    private void processValidInput(Player player, String input, PunishDetailsMenu detailsMenu, String inputType) {
        if (inputType.equals("new_target")) {
            handleNewTargetInput(player, input);
        } else if (detailsMenu != null) {
            handleMenuSpecificInput(player, input, detailsMenu, inputType);
        }
    }

    private void handleMenuSpecificInput(Player player, String input, PunishDetailsMenu detailsMenu, String inputType) {
        if (inputType.equals("ban_reason")) {
            handleReasonInput(player, input, detailsMenu);
        } else if (inputType.equals("custom_time")) {
            handleCustomTimeInput(player, input, detailsMenu);
        }
    }

    private void handleNewTargetInput(Player player, String input) {
        OfflinePlayer newTarget = Bukkit.getOfflinePlayer(input);
        if (!newTarget.hasPlayedBefore() && !newTarget.isOnline()) {
            player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.player_never_played", "{input}", input)));
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> new PunishMenu(newTarget.getUniqueId(), plugin).open(player));
        }
    }

    private void handleReasonInput(Player player, String input, PunishDetailsMenu detailsMenu) {
        detailsMenu.setBanReason(input);
        detailsMenu.updateSetReasonItem();
        detailsMenu.updateConfirmButtonStatus();
        reopenDetailsMenu(player, detailsMenu);
    }

    private void handleCustomTimeInput(Player player, String input, PunishDetailsMenu detailsMenu) {
        if (isValidTimeFormat(input)) {
            detailsMenu.setBanTime(input);
            detailsMenu.updateSetTimeItem();
            detailsMenu.updateConfirmButtonStatus();
            reopenDetailsMenu(player, detailsMenu);
        } else {
            player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.invalid_time_format")));
            requestCustomTimeInput(player, detailsMenu);
        }
    }

    private void reopenDetailsMenu(Player player, PunishDetailsMenu detailsMenu) {
        Bukkit.getScheduler().runTask(plugin, () -> detailsMenu.open(player));
    }

    private boolean isValidTimeFormat(String time) {
        String units = String.join("|",
                plugin.getConfigManager().getDayTimeUnit(),
                plugin.getConfigManager().getHoursTimeUnit(),
                plugin.getConfigManager().getMinutesTimeUnit(),
                plugin.getConfigManager().getSecondsTimeUnit()
        );
        return time.matches("\\d+[" + units + "]");
    }

    private void confirmDynamicPunishment(Player player, PunishDetailsMenu punishDetailsMenu) {
        switch (punishDetailsMenu.getPunishmentType().toLowerCase()) {
            case BAN_PUNISHMENT_TYPE:
            case MUTE_PUNISHMENT_TYPE:
                confirmStandardPunishment(player, punishDetailsMenu);
                break;
            case SOFTBAN_PUNISHMENT_TYPE:
                confirmSoftBan(player, punishDetailsMenu);
                break;
            default:
                plugin.getLogger().warning("Unknown punishment type: " + punishDetailsMenu.getPunishmentType());
        }
    }

    private void confirmStandardPunishment(Player player, PunishDetailsMenu detailsMenu) {
        UUID targetUUID = detailsMenu.getTargetUUID();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        String command = getPunishmentCommand(detailsMenu.getPunishmentType());

        String processedCommand = command
                .replace("{target}", target.getName() != null ? target.getName() : "unknown")
                .replace("{time}", detailsMenu.getBanTime())
                .replace("{reason}", detailsMenu.getBanReason());

        executePunishmentCommand(player, processedCommand, target, detailsMenu);
    }

    private String getPunishmentCommand(String punishmentType) {
        switch (punishmentType.toLowerCase()) {
            case BAN_PUNISHMENT_TYPE:
                return plugin.getConfigManager().getBanCommand();
            case MUTE_PUNISHMENT_TYPE:
                return plugin.getConfigManager().getMuteCommand();
            default:
                return "";
        }
    }

    private void executePunishmentCommand(Player player, String command, OfflinePlayer target, PunishDetailsMenu detailsMenu) {
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command));
        playSound(player, "punish_confirm");
        sendConfirmationMessage(player, target, detailsMenu);
    }

    private void sendConfirmationMessage(Player player, OfflinePlayer target, PunishDetailsMenu detailsMenu) {
        player.closeInventory();
        player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.punishment_confirmed",
                "{target}", target.getName() != null ? target.getName() : "unknown",
                "{time}", detailsMenu.getBanTime(),
                "{reason}", detailsMenu.getBanReason(),
                "{punishment_type}", detailsMenu.getPunishmentType())));
    }

    private void confirmSoftBan(Player player, PunishDetailsMenu punishDetailsMenu) {
        UUID targetUUID = punishDetailsMenu.getTargetUUID();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        String reason = punishDetailsMenu.getBanReason();
        String timeInput = punishDetailsMenu.getBanTime();

        long endTime = calculateEndTime(timeInput);
        plugin.getSoftBanDatabaseManager().softBanPlayer(targetUUID, endTime, reason);

        playSound(player, "punish_confirm");
        sendSoftbanConfirmation(player, target, timeInput, reason);
    }

    private long calculateEndTime(String timeInput) {
        if (timeInput.equalsIgnoreCase("Permanent")) {
            return Long.MAX_VALUE;
        }
        int seconds = TimeUtils.parseTime(timeInput, plugin.getConfigManager());
        return System.currentTimeMillis() + (seconds * 1000L);
    }

    private void sendSoftbanConfirmation(Player player, OfflinePlayer target, String time, String reason) {
        player.closeInventory();
        player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.punishment_confirmed",
                "{target}", target.getName() != null ? target.getName() : "unknown",
                "{time}", time,
                "{reason}", reason,
                "{punishment_type}", SOFTBAN_PUNISHMENT_TYPE)));
    }

    private void confirmUnsoftban(Player player, PunishDetailsMenu punishDetailsMenu) {
        UUID targetUUID = punishDetailsMenu.getTargetUUID();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);

        plugin.getSoftBanDatabaseManager().unSoftBanPlayer(targetUUID);
        playSound(player, "punish_confirm");

        player.closeInventory();
        player.sendMessage(MessageUtils.getColorMessage(plugin.getConfigManager().getMessage("messages.unsoftban_success",
                "{target}", target.getName() != null ? target.getName() : "unknown")));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        Player player = (Player) event.getPlayer();

        if (holder instanceof PunishMenu || holder instanceof PunishDetailsMenu || holder instanceof TimeSelectorMenu) {
            clearPlayerInputData(player);
        }
    }

    private void clearPlayerInputData(Player player) {
        inputTimeouts.remove(player.getUniqueId());
        pendingDetailsMenus.remove(player.getUniqueId());
        inputTypes.remove(player.getUniqueId());
    }

    private void playSound(Player player, String soundKey) {
        try {
            Sound sound = Sound.valueOf(plugin.getConfigManager().getSoundName(soundKey).toUpperCase());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound configured: " + plugin.getConfigManager().getSoundName(soundKey));
        }
    }
}