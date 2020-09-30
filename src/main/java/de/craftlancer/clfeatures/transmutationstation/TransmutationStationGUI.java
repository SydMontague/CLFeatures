package de.craftlancer.clfeatures.transmutationstation;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.CLCore;
import de.craftlancer.core.gui.GUIInventory;
import de.craftlancer.core.items.CustomItemRegistry;
import de.craftlancer.core.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TransmutationStationGUI {
    
    private GUIInventory inventory;
    private CustomItemRegistry registry;
    
    public TransmutationStationGUI() {
        this.registry = CLCore.getInstance().getItemRegistry();
    }
    
    public void display(Player player) {
        if (inventory == null)
            createInventory();
        
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.5F, 1F);
        player.openInventory(inventory.getInventory());
    }
    
    private void createInventory() {
        inventory = new GUIInventory(CLFeatures.getInstance(), ChatColor.DARK_PURPLE + "Transportable Transmutation Station", 3);
        
        //set the fragment items in their correct slots
        registry.getItem("pettyfragment").ifPresent(fragment -> inventory.setItem(0, new ItemBuilder(fragment).setAmountUnsafe(10).build()));
        registry.getItem("lesserfragment").ifPresent(fragment -> inventory.setItem(2, new ItemBuilder(fragment).setAmountUnsafe(1).build()));
        registry.getItem("lesserfragment").ifPresent(fragment -> inventory.setItem(6, new ItemBuilder(fragment).setAmountUnsafe(1).build()));
        registry.getItem("pettyfragment").ifPresent(fragment -> inventory.setItem(8, new ItemBuilder(fragment).setAmountUnsafe(10).build()));
        registry.getItem("lesserfragment").ifPresent(fragment -> inventory.setItem(9, new ItemBuilder(fragment).setAmountUnsafe(5).build()));
        registry.getItem("commonfragment").ifPresent(fragment -> inventory.setItem(11, new ItemBuilder(fragment).setAmountUnsafe(1).build()));
        registry.getItem("commonfragment").ifPresent(fragment -> inventory.setItem(15, new ItemBuilder(fragment).setAmountUnsafe(1).build()));
        registry.getItem("lesserfragment").ifPresent(fragment -> inventory.setItem(17, new ItemBuilder(fragment).setAmountUnsafe(5).build()));
        registry.getItem("commonfragment").ifPresent(fragment -> inventory.setItem(18, new ItemBuilder(fragment).setAmountUnsafe(4).build()));
        registry.getItem("greaterfragment").ifPresent(fragment -> inventory.setItem(20, new ItemBuilder(fragment).setAmountUnsafe(1).build()));
        registry.getItem("greaterfragment").ifPresent(fragment -> inventory.setItem(24, new ItemBuilder(fragment).setAmountUnsafe(1).build()));
        registry.getItem("commonfragment").ifPresent(fragment -> inventory.setItem(26, new ItemBuilder(fragment).setAmountUnsafe(4).build()));
        
        //set the arrows, gray the arrow out if the transaction isn't/shouldn't be possible
        inventory.setItem(1, new ItemBuilder(Material.ARROW).setEnchantmentGlow(true).setCustomModelData(2).setDisplayName(ChatColor.GREEN + "10 Petty Fragments -> 1 Lesser Fragment").build());
        inventory.setItem(7, new ItemBuilder(Material.ARROW).setEnchantmentGlow(true).setCustomModelData(2).setDisplayName(ChatColor.GREEN + "1 Lesser Fragment -> 10 Petty Fragments").build());
        inventory.setItem(10, new ItemBuilder(Material.ARROW).setCustomModelData(1).setDisplayName(ChatColor.GRAY + "5 Lesser Fragments -> 1 Common Fragment")
                .setLore("",
                        ChatColor.RED + "Visit the Valgard transmutation stations",
                        ChatColor.RED + "to upgrade fragments!").build());
        inventory.setItem(16, new ItemBuilder(Material.ARROW).setEnchantmentGlow(true).setCustomModelData(2).setDisplayName(ChatColor.GREEN + "1 Common Fragment -> 5 Lesser Fragments").build());
        inventory.setItem(19, new ItemBuilder(Material.ARROW).setCustomModelData(1).setDisplayName(ChatColor.GRAY + "4 Common Fragments -> 1 Greater Fragment")
                .setLore("",
                        ChatColor.RED + "Visit the Valgard transmutation stations",
                        ChatColor.RED + "to upgrade fragments!").build());
        inventory.setItem(25, new ItemBuilder(Material.ARROW).setEnchantmentGlow(true).setCustomModelData(2).setDisplayName(ChatColor.GREEN + "1 Greater Fragment -> 4 Common Fragments").build());
        
        //set the info item
        inventory.setItem(13, new ItemBuilder(Material.STONE).setCustomModelData(5)
                .setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "What is this?")
                .setLore("",
                        ChatColor.GRAY + "Upgrade fragments on the left.",
                        ChatColor.GRAY + "Downgrade fragments on the right.",
                        ChatColor.GRAY + "Click the arrows to transmute.")
                .build());
        
        inventory.setClickAction(1, player -> {
            if (!removeFromInventory(registry.getItem("pettyfragment").get(), 10, player.getInventory())) {
                player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.RED + "You do not have the necessary items!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
                return;
            }
            
            player.getInventory().addItem(registry.getItem("lesserfragment").get()).forEach((k, v) -> player.getWorld().dropItemNaturally(player.getLocation(), v));
            player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.GREEN + "You have transmuted 10 petty fragments for 1 lesser fragment.");
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5F, 1F);
        });
        inventory.setClickAction(7, player -> {
            if (!removeFromInventory(registry.getItem("lesserfragment").get(), 1, player.getInventory())) {
                player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.RED + "You do not have the necessary items!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
                return;
            }
            
            player.getInventory().addItem(new ItemBuilder(registry.getItem("pettyfragment").get()).setAmountUnsafe(10).build()).forEach((k, v) -> player.getWorld().dropItemNaturally(player.getLocation(), v));
            player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.GREEN + "You have transmuted 1 lesser fragment for 10 petty fragments.");
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5F, 1F);
        });
        inventory.setClickAction(10, player -> {
            player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.RED + "This transmutation is locked. Please use the transmutation stations at Valgard.");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
        });
        inventory.setClickAction(16, player -> {
            if (!removeFromInventory(registry.getItem("greaterfragment").get(), 1, player.getInventory())) {
                player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.RED + "You do not have the necessary items!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
                return;
            }
            
            player.getInventory().addItem(new ItemBuilder(registry.getItem("lesserfragment").get()).setAmountUnsafe(5).build()).forEach((k, v) -> player.getWorld().dropItemNaturally(player.getLocation(), v));
            player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.GREEN + "You have transmuted 1 common fragment for 5 lesser fragments.");
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5F, 1F);
        });
        inventory.setClickAction(19, player -> {
            player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.RED + "This transmutation is locked. Please use the transmutation stations at Valgard.");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
        });
        inventory.setClickAction(25, player -> {
            if (!removeFromInventory(registry.getItem("commonfragment").get(), 1, player.getInventory())) {
                player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.RED + "You do not have the necessary items!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
                return;
            }
            
            player.getInventory().addItem(new ItemBuilder(registry.getItem("greaterfragment").get()).setAmountUnsafe(4).build()).forEach((k, v) -> player.getWorld().dropItemNaturally(player.getLocation(), v));
            player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.GREEN + "You have transmuted 1 greater fragment for 4 common fragments.");
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5F, 1F);
        });
        
    }
    
    /**
     * @return true if items were removed successfully, false if the inventory didn't contain the correct amount of items
     */
    private boolean removeFromInventory(ItemStack item, int amount, Inventory inventory) {
        if (!inventory.containsAtLeast(item, amount))
            return false;
        
        item.setAmount(amount);
        
        inventory.removeItem(item);
        
        return true;
    }
    
    
}
