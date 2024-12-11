package com.etrigan.ItemShopPlugin;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemShop extends JavaPlugin implements Listener {
    private final String SHOP_TITLE = "Item Shop";
    private Map<Integer, ShopItem> shopItems = new HashMap<>();
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadShopItems();
        getServer().getPluginManager().registerEvents(this, this);
        
        getCommand("shop").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                openShop((Player) sender);
                return true;
            }
            return false;
        });
    }
    
    private void loadShopItems() {
        shopItems.clear();
        ConfigurationSection items = getConfig().getConfigurationSection("shop-items");
        
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection item = items.getConfigurationSection(key);
                if (item != null) {
                    try {
                        Material sellItem = Material.valueOf(item.getString("sell-item"));
                        Material costItem = Material.valueOf(item.getString("cost-item"));
                        int sellAmount = item.getInt("sell-amount");
                        int costAmount = item.getInt("cost-amount");
                        int slot = item.getInt("slot");
                        
                        shopItems.put(slot, new ShopItem(
                            sellItem,
                            sellAmount,
                            costItem,
                            costAmount
                        ));
                    } catch (IllegalArgumentException e) {
                        getLogger().warning("Invalid material in config for item: " + key);
                    }
                }
            }
        }
    }
    
    private void openShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 27, SHOP_TITLE);
        
        for (Map.Entry<Integer, ShopItem> entry : shopItems.entrySet()) {
            ShopItem shopItem = entry.getValue();
            ItemStack displayItem = new ItemStack(shopItem.getSellItem(), shopItem.getSellAmount());
            ItemMeta meta = displayItem.getItemMeta();
            
            meta.setDisplayName("§bBuy " + shopItem.getSellAmount() + "x " + formatName(shopItem.getSellItem().name()));
            meta.setLore(Arrays.asList(
                Message.COST.toString() + shopItem.getCostAmount() + "x " + formatName(shopItem.getCostItem().name()),
                Message.PURCH.toString()
            ));
            
            displayItem.setItemMeta(meta);
            shop.setItem(entry.getKey(), displayItem);
        }
        
        player.openInventory(shop);
    }
    
    private String formatName(String name) {
        String[] words = name.toLowerCase().split("_");
        List<String> capitalizedWords = new ArrayList<>();
        
        for (String word : words) {
            capitalizedWords.add(word.substring(0, 1).toUpperCase() + word.substring(1));
        }
        
        return String.join(" ", capitalizedWords);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(SHOP_TITLE)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (event.getCurrentItem() == null) return;
        
        ShopItem shopItem = shopItems.get(event.getSlot());
        if (shopItem == null) return;
        
        ItemStack costItems = new ItemStack(shopItem.getCostItem(), shopItem.getCostAmount());
        if (hasItems(player, costItems)) {
            removeItems(player, costItems);
            player.getInventory().addItem(new ItemStack(shopItem.getSellItem(), shopItem.getSellAmount()));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.sendMessage(Message.SUCS_PURCH.toString() + shopItem.getSellAmount() + "x " + 
                formatName(shopItem.getSellItem().name()) + "!");
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(Message.MORE_MSG1.toString() + shopItem.getCostAmount() + "x " + 
                formatName(shopItem.getCostItem().name()) + Message.MORE_MSG2.toString());
        }
    }
    
    private boolean hasItems(Player player, ItemStack items) {
        return player.getInventory().containsAtLeast(items, items.getAmount());
    }
    
    private void removeItems(Player player, ItemStack items) {
        player.getInventory().removeItem(items);
    }
    
    private static class ShopItem {
        private final Material sellItem;
        private final int sellAmount;
        private final Material costItem;
        private final int costAmount;
        
        public ShopItem(Material sellItem, int sellAmount, Material costItem, int costAmount) {
            this.sellItem = sellItem;
            this.sellAmount = sellAmount;
            this.costItem = costItem;
            this.costAmount = costAmount;
        }
        
        public Material getSellItem() { return sellItem; }
        public int getSellAmount() { return sellAmount; }
        public Material getCostItem() { return costItem; }
        public int getCostAmount() { return costAmount; }
    }
}
