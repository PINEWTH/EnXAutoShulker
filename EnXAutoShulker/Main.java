package ru.pine.neptunautoshulker;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        config = getConfig();
        config.options().copyDefaults(true);
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();

        if (isShulkerBox(itemInHand) || isShulkerBox(itemInOffHand)) {
            if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && player.isSneaking()) {
                openShulker(player, itemInHand);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItemInHand();
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();
            boolean hasShulkerIdentifier = false;
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() == Material.NAME_TAG && item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta.hasDisplayName() && meta.getDisplayName().startsWith("Шалкер - ")) {
                        hasShulkerIdentifier = true;
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();
            String title = inventory.getType().name();

            if (title.startsWith("Шалкер - ")) {
                for (ItemStack item : inventory.getContents()) {
                    if (item != null) {
                        Bukkit.getLogger().info("Содержимое шалкера: " + item.toString());
                    }
                }
            }
        }
    }

    private boolean isShulkerBox(ItemStack item) {
        return item != null && item.getType().name().endsWith("SHULKER_BOX");
    }

    private void openShulker(Player player, ItemStack shulker) {
        String customName = getCustomName(shulker);
        int inventorySize = config.getInt("shulker-inventory-size");
        Inventory inventory = Bukkit.createInventory(player, inventorySize, "Шалкер - " + customName);
        ItemStack identifier = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = identifier.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Шалкер - " + customName);
            identifier.setItemMeta(meta);
        }
        inventory.addItem(identifier);

        player.sendMessage(getOpenShulkerMessage());
        player.openInventory(inventory);
    }

    private String getCustomName(ItemStack item) {
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                return displayName.replace("Шалкер - ", "").replace(" - " + getOwnerName(displayName), "");
            }
        }
        return "Без названия";
    }

    private String getOwnerName(String displayName) {
        String[] parts = displayName.split(" - ");
        if (parts.length > 1) {
            return parts[parts.length - 1];
        }
        return "";
    }

    private String getOpenShulkerMessage() {
        return config.getString("shulker-messages.open").replace("&", "§");
    }

    private String getPvpModeMessage() {
        return config.getString("shulker-messages.pvp-mode").replace("&", "§");
    }
}
