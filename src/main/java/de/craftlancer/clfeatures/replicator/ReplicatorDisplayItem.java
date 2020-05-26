package de.craftlancer.clfeatures.replicator;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.Utils;

public class ReplicatorDisplayItem {
    public static final String DISPLAY_ITEM_METADATA = "replicatorDisplayItem";
    
    private Location spawnLocation;
    private World world;
    
    private ItemStack product;
    private Item item;
    
    /**
     * Used to create an item display for the ReplicatorFeature
     *
     * @param instance - The ReplicatorFeatureInstance this is being used from
     */
    public ReplicatorDisplayItem(ReplicatorFeatureInstance instance) {
        this.world = instance.getInitialBlock().getWorld();
        this.spawnLocation = instance.getDaylightSensor().clone().add(0.5, 0.4, 0.5);
        setItemStack(instance.getProduct());
    }
    
    public void tick() {
        if (product == null || !Utils.isChunkLoaded(spawnLocation))
            return;
        
        if (item == null || !item.isValid()) {
            item = world.dropItem(spawnLocation, product);
            item.setInvulnerable(true);
            item.setMetadata(DISPLAY_ITEM_METADATA, new FixedMetadataValue(CLFeatures.getInstance(), 0));
        }
        
        item.setVelocity(new Vector().zero());
        item.teleport(spawnLocation);
    }
    
    public void remove() {
        if (item == null)
            return;
        item.teleport(new Location(world, 0, 2, 0));
        item.remove();
    }
    
    public void setItemStack(ItemStack item) {
        remove();
        
        if (item == null)
            this.product = null;
        else {
            ItemStack tmp = item.clone();
            ItemMeta meta = tmp.getItemMeta();
            meta.setDisplayName(ChatColor.DARK_GRAY + "ReplicatorDisplayItem");
            
            tmp.setItemMeta(meta);
            tmp.setAmount(1);
            
            this.product = tmp;
        }
    }
    
    public Item getItem() {
        return item;
    }
}
