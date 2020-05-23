package de.craftlancer.clfeatures.replicator;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class ReplicatorDisplayItem {
    private final ReplicatorFeatureInstance instance;
    
    private Location spawnLocation;
    private Location daylightDetectorLocation;
    private World world;
    
    private ItemStack product;
    private Item item;
    
    /**
     * Used to create an item display for the ReplicatorFeature
     *
     * @param instance - The ReplicatorFeatureInstance this is being used from
     */
    public ReplicatorDisplayItem(ReplicatorFeatureInstance instance) {
        this.instance = instance;
        
        this.daylightDetectorLocation = instance.getDaylightCensor().clone();
        this.world = instance.getInitialBlock().getWorld();
        
        this.product = instance.getProduct();
        
        setSpawnLocation();
    }
    
    public void spawn() {
        if (!ReplicatorFeatureInstance.isChunkLoadedAtLocation(daylightDetectorLocation))
            return;
        item = world.dropItem(spawnLocation, getSingleProduct());
        item.setItemStack(getSingleProduct());
        item.setVelocity(new Vector().zero());
    }
    
    public void remove() {
        if (item == null)
            return;
        item.teleport(new Location(world, 0, 2, 0));
        item.remove();
    }
    
    public void teleport() {
        if (item.getLocation().getX() == spawnLocation.getX() && item.getLocation().getZ() == spawnLocation.getZ())
            return;
        item.setVelocity(new Vector().zero());
        item.teleport(spawnLocation);
    }
    
    private void setSpawnLocation() {
        spawnLocation = daylightDetectorLocation;
        spawnLocation.setX(spawnLocation.getX() + 0.5);
        spawnLocation.setZ(spawnLocation.getZ() + 0.5);
        spawnLocation.setY(spawnLocation.getY() + 0.6);
    }
    
    private ItemStack getSingleProduct() {
        ItemStack i = product.clone();
        i.setAmount(1);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "ReplicatorDisplayItem");
        return i;
    }
    
    public Item getItem() {
        return item;
    }
}
