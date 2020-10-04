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
    private static final String GREATERFRAGMENT = "greaterfragment";
    private static final String COMMONFRAGMENT = "commonfragment";
    private static final String LESSERFRAGMENT = "lesserfragment";
    private static final String PETTYFRAGMENT = "pettyfragment";
    
    private GUIInventory gui;
    private CustomItemRegistry registry;
    
    public TransmutationStationGUI() {
        this.registry = CLCore.getInstance().getItemRegistry();
    }
    
    public void display(Player player) {
        if (gui == null)
            createInventory();
        
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.5F, 1F);
        player.openInventory(gui.getInventory());
    }
    
    private void createInventory() {
        gui = new GUIInventory(CLFeatures.getInstance(), ChatColor.DARK_PURPLE + "Transportable Transmutation Station", 3);
        
        //set the fragment items in their correct slots
        registry.getItem(PETTYFRAGMENT).ifPresent(fragment -> gui.setItem(0, new ItemBuilder(fragment).setAmountUnsafe(10).build()));
        registry.getItem(LESSERFRAGMENT).ifPresent(fragment -> gui.setItem(2, new ItemBuilder(fragment).setAmountUnsafe(1).build()));
        registry.getItem(LESSERFRAGMENT).ifPresent(fragment -> gui.setItem(6, new ItemBuilder(fragment).setAmountUnsafe(1).build()));
        registry.getItem(PETTYFRAGMENT).ifPresent(fragment -> gui.setItem(8, new ItemBuilder(fragment).setAmountUnsafe(10).build()));
        registry.getItem(LESSERFRAGMENT).ifPresent(fragment -> gui.setItem(9, new ItemBuilder(fragment).setAmountUnsafe(5).build()));
        registry.getItem(COMMONFRAGMENT).ifPresent(fragment -> gui.setItem(11, new ItemBuilder(fragment).setAmountUnsafe(1).build()));
        registry.getItem(COMMONFRAGMENT).ifPresent(fragment -> gui.setItem(15, new ItemBuilder(fragment).setAmountUnsafe(1).build()));
        registry.getItem(LESSERFRAGMENT).ifPresent(fragment -> gui.setItem(17, new ItemBuilder(fragment).setAmountUnsafe(5).build()));
        registry.getItem(COMMONFRAGMENT).ifPresent(fragment -> gui.setItem(18, new ItemBuilder(fragment).setAmountUnsafe(4).build()));
        registry.getItem(GREATERFRAGMENT).ifPresent(fragment -> gui.setItem(20, new ItemBuilder(fragment).setAmountUnsafe(1).build()));
        registry.getItem(GREATERFRAGMENT).ifPresent(fragment -> gui.setItem(24, new ItemBuilder(fragment).setAmountUnsafe(1).build()));
        registry.getItem(COMMONFRAGMENT).ifPresent(fragment -> gui.setItem(26, new ItemBuilder(fragment).setAmountUnsafe(4).build()));
        
        //set the arrows, gray the arrow out if the transaction isn't/shouldn't be possible
        gui.setItem(1, new ItemBuilder(Material.ARROW).setEnchantmentGlow(true).setCustomModelData(2).setDisplayName(ChatColor.GREEN + "10 Petty Fragments -> 1 Lesser Fragment").build());
        gui.setItem(7, new ItemBuilder(Material.ARROW).setEnchantmentGlow(true).setCustomModelData(2).setDisplayName(ChatColor.GREEN + "1 Lesser Fragment -> 10 Petty Fragments").build());
        gui.setItem(10, new ItemBuilder(Material.ARROW).setCustomModelData(1).setDisplayName(ChatColor.GRAY + "5 Lesser Fragments -> 1 Common Fragment")
                .setLore("",
                        ChatColor.RED + "Visit the Valgard transmutation stations",
                        ChatColor.RED + "to upgrade fragments!").build());
        gui.setItem(16, new ItemBuilder(Material.ARROW).setEnchantmentGlow(true).setCustomModelData(2).setDisplayName(ChatColor.GREEN + "1 Common Fragment -> 5 Lesser Fragments").build());
        gui.setItem(19, new ItemBuilder(Material.ARROW).setCustomModelData(1).setDisplayName(ChatColor.GRAY + "4 Common Fragments -> 1 Greater Fragment")
                .setLore("",
                        ChatColor.RED + "Visit the Valgard transmutation stations",
                        ChatColor.RED + "to upgrade fragments!").build());
        gui.setItem(25, new ItemBuilder(Material.ARROW).setEnchantmentGlow(true).setCustomModelData(2).setDisplayName(ChatColor.GREEN + "1 Greater Fragment -> 4 Common Fragments").build());
        
        //set the info item
        gui.setItem(13, new ItemBuilder(Material.STONE).setCustomModelData(5)
                .setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "What is this?")
                .setLore("",
                        ChatColor.GRAY + "Upgrade fragments on the left.",
                        ChatColor.GRAY + "Downgrade fragments on the right.",
                        ChatColor.GRAY + "Click the arrows to transmute.")
                .build());
        
        gui.setClickAction(1, player -> {
            if (!removeFromInventory(registry.getItem(PETTYFRAGMENT).get(), 10, player.getInventory())) {
                player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.RED + "You do not have the necessary items!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
                return;
            }
            
            player.getInventory().addItem(registry.getItem(LESSERFRAGMENT).get()).forEach((k, v) -> player.getWorld().dropItemNaturally(player.getLocation(), v));
            player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.GREEN + "You have transmuted 10 petty fragments for 1 lesser fragment.");
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5F, 1F);
        });
        gui.setClickAction(7, player -> {
            if (!removeFromInventory(registry.getItem(LESSERFRAGMENT).get(), 1, player.getInventory())) {
                player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.RED + "You do not have the necessary items!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
                return;
            }
            
            player.getInventory().addItem(new ItemBuilder(registry.getItem(PETTYFRAGMENT).get()).setAmountUnsafe(10).build()).forEach((k, v) -> player.getWorld().dropItemNaturally(player.getLocation(), v));
            player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.GREEN + "You have transmuted 1 lesser fragment for 10 petty fragments.");
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5F, 1F);
        });
        gui.setClickAction(10, player -> {
            player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.RED + "This transmutation is locked. Please use the transmutation stations at Valgard.");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
        });
        gui.setClickAction(16, player -> {
            if (!removeFromInventory(registry.getItem(GREATERFRAGMENT).get(), 1, player.getInventory())) {
                player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.RED + "You do not have the necessary items!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
                return;
            }
            
            player.getInventory().addItem(new ItemBuilder(registry.getItem(LESSERFRAGMENT).get()).setAmountUnsafe(5).build()).forEach((k, v) -> player.getWorld().dropItemNaturally(player.getLocation(), v));
            player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.GREEN + "You have transmuted 1 common fragment for 5 lesser fragments.");
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5F, 1F);
        });
        gui.setClickAction(19, player -> {
            player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.RED + "This transmutation is locked. Please use the transmutation stations at Valgard.");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
        });
        gui.setClickAction(25, player -> {
            if (!removeFromInventory(registry.getItem(COMMONFRAGMENT).get(), 1, player.getInventory())) {
                player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.RED + "You do not have the necessary items!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
                return;
            }
            
            player.getInventory().addItem(new ItemBuilder(registry.getItem(GREATERFRAGMENT).get()).setAmountUnsafe(4).build()).forEach((k, v) -> player.getWorld().dropItemNaturally(player.getLocation(), v));
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
        
        ItemStack item2 = item.clone();
        item2.setAmount(amount);
        inventory.removeItem(item2);
        
        return true;
    }
    
    
}
