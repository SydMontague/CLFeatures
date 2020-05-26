package de.craftlancer.clfeatures.replicator;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.Feature;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ReplicatorFeature extends Feature<ReplicatorFeatureInstance> {
    private List<ReplicatorFeatureInstance> instances;
    
    private List<Material> blockedProducts;
    
    public ReplicatorFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "replicator.limit"));
        
        List<String> stringList = config.getStringList("excludedProducts");
        blockedProducts = new ArrayList<>();
        stringList.forEach(string -> blockedProducts.add(Material.getMaterial(string)));
        
        instances = (List<ReplicatorFeatureInstance>) YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/replicator.yml"))
                .getList("replicator", new ArrayList<>());
    }
    
    public List<Material> getBlockedProducts() {
        return blockedProducts;
    }
    
    //Unused with blueprints
    @Override
    public boolean isFeatureItem(ItemStack item) {
        return false;
    }
    
    
    @Override
    public boolean checkFeatureLimit(Player player) {
        if (player.hasPermission("clfeature.portal.ignoreLimit"))
            return true;
        
        int limit = getLimit(player);
        
        if (limit < 0)
            return true;
        
        long current = instances.stream().filter(a -> a.isOwner(player)).count();
        
        return current < limit;
    }
    
    //Unused with blueprints
    @Override
    public Collection<Block> checkEnvironment(Block initialBlock) {
        return null;
    }
    
    //Unused with blueprints
    @Override
    public boolean createInstance(Player creator, Block initialBlock) {
        return false;
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/replicator.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("replicator", instances);
        
        BukkitRunnable saveTask = new LambdaRunnable(() -> {
            try {
                config.save(f);
            } catch (IOException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Error while saving Replicator: ", e);
            }
        });
        
        if (getPlugin().isEnabled())
            saveTask.runTaskAsynchronously(getPlugin());
        else
            saveTask.run();
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new ReplicatorCommandHandler(getPlugin(), this);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof ReplicatorFeatureInstance) {
            instances.remove(instance);
        }
    }
    
    @Nonnull
    @Override
    protected String getName() {
        return "Replicator";
    }
    
    @Override
    public List getFeatures() {
        instances.removeIf(Objects::isNull);
        return instances;
    }
    
    @Override
    public boolean createInstance(Player creator, Block initialLocation, List blocks) {
        return instances.add(new ReplicatorFeatureInstance(this, creator.getUniqueId(), new BlockStructure(blocks), initialLocation.getLocation()));
    }
    
    public List<ReplicatorFeatureInstance> getReplicatorsByUUID(UUID uuid) {
        return instances.stream().filter(a -> a.isOwner(uuid)).collect(Collectors.toList());
    }
    
    /*
     * DUPE PREVENTION
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (getInstanceItems().stream().anyMatch(item -> item.equals(event.getItem().getItemStack().getItemMeta())))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void onInventoryPickup(InventoryPickupItemEvent event) {
        if (getInstanceItems().stream().anyMatch(item -> item.equals(event.getItem().getItemStack().getItemMeta())))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (getInstanceItems().stream().anyMatch(item -> item.equals(event.getEntity().getItemStack().getItemMeta())))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void onItemPortalUse(EntityPortalEvent event) {
        if (!(event.getEntity() instanceof Item))
            return;
        
        if (getInstanceItems().stream().anyMatch(item -> item.equals(((Item) event.getEntity()).getItemStack().getItemMeta())))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void onItemMerge(ItemMergeEvent event) {
        if (getInstanceItems().stream().anyMatch(item -> item.equals(event.getEntity().getItemStack().getItemMeta())
                || item.equals(event.getTarget().getItemStack().getItemMeta())))
            event.setCancelled(true);
    }
    
    private List<ItemMeta> getInstanceItems() {
        List<ItemMeta> itemList = new ArrayList<>();
        instances.stream().filter(instances -> instances.getDisplayItem() != null
                && instances.getDisplayItem().getItem() != null
                && instances.getDisplayItem().getItem().getItemStack().getItemMeta() != null)
                .forEach(instance -> itemList.add(instance.getDisplayItem().getItem().getItemStack().getItemMeta()));
        return itemList;
    }
    
    
}
