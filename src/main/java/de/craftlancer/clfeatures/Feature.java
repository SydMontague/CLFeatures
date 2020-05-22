package de.craftlancer.clfeatures;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;

import de.craftlancer.core.CLCore;
import de.craftlancer.core.Utils;
import de.craftlancer.core.command.CommandHandler;

public abstract class Feature<T extends FeatureInstance> implements Listener {
    
    private final CLFeatures plugin;
    private int defaultLimit;
    private int maxLimit;
    private Map<String, Integer> limitMap = new HashMap<>();
    
    private final NamespacedKey limitKey;
    private String featureItem;
    private final String limitToken;
    
    public Feature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        this.plugin = plugin;
        this.limitKey = limitKey;
        this.limitToken = config.getString("limitToken", "");
        
        featureItem = config.getString("featureItem");
        
        defaultLimit = config.getInt("defaultLimit", -1);
        maxLimit = config.getInt("maxLimit", -1);
        ConfigurationSection limitConfig = config.getConfigurationSection("limits");
        if (limitConfig != null)
            limitConfig.getKeys(false).forEach(a -> limitMap.put(a, limitConfig.getInt(a)));
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public int getLimit(Player player) {
        int groupLimit = limitMap.entrySet().stream().filter(a -> plugin.getPermissions().playerInGroup(player, a.getKey())).map(Entry::getValue)
                                 .max(Integer::compare).orElseGet(() -> defaultLimit);
        int individualLimit = player.getPersistentDataContainer().getOrDefault(limitKey, PersistentDataType.INTEGER, 0).intValue();
        
        return groupLimit < 0 ? -1 : groupLimit + individualLimit;
    }
    
    public void addFeatureLimit(Player player, int amount) {
        int individualLimit = player.getPersistentDataContainer().getOrDefault(limitKey, PersistentDataType.INTEGER, 0).intValue();
        player.getPersistentDataContainer().set(limitKey, PersistentDataType.INTEGER, individualLimit + amount);
    }
    
    public boolean isLimitToken(@Nonnull ItemStack item) {
        if (!CLCore.getInstance().getItemRegistry().hasItem(limitToken) || item.getType().isAir())
            return false;
        
        return item.isSimilar(CLCore.getInstance().getItemRegistry().getItem(limitToken));
    }
    
    public int getMaxLimit() {
        return maxLimit;
    }
    
    public String getFeatureItem() {
        return featureItem;
    }
    
    public CLFeatures getPlugin() {
        return plugin;
    }
    
    public void giveFeatureItem(Player player) {
        ItemStack item = CLCore.getInstance().getItemRegistry().getItem(getFeatureItem());
        
        if (item != null)
            player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
    }
    
    /**
     * @deprecated use blueprints instead
     */
    @Deprecated
    public abstract boolean isFeatureItem(ItemStack item);
    
    public abstract boolean checkFeatureLimit(Player player);
    
    public abstract Collection<Block> checkEnvironment(Block initialBlock);
    
    /**
     * @deprecated use blueprints instead
     */
    @Deprecated
    public abstract boolean createInstance(Player creator, Block initialBlock);
    
    public abstract boolean createInstance(Player creator, Block initialBlock, List<Location> blocks);
    
    public abstract void save();
    
    public abstract CommandHandler getCommandHandler();
    
    public abstract void remove(FeatureInstance instance);
    
    @Nonnull
    protected abstract String getName();
    
    public abstract List<T> getFeatures();
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        BoundingBox bb = Utils.calculateBoundingBoxBlock(event.getBlocks());
        
        if (getFeatures().stream().filter(a -> a.getStructure().containsBoundingBox(bb))
                         .anyMatch(a -> a.getStructure().containsAnyBlock(event.getBlocks())))
            event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        BoundingBox bb = Utils.calculateBoundingBoxBlock(event.getBlocks());
        
        if (getFeatures().stream().filter(a -> a.getStructure().containsBoundingBox(bb))
                         .anyMatch(a -> a.getStructure().containsAnyBlock(event.getBlocks())))
            event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onExplosion(EntityExplodeEvent event) {
        BoundingBox bb = Utils.calculateBoundingBoxBlock(event.blockList());
        
        Set<Location> locs = getFeatures().stream().map(T::getStructure).filter(a -> a.containsBoundingBox(bb)).flatMap(a -> a.getBlocks().stream())
                                          .collect(Collectors.toSet());
        
        event.blockList().removeIf(a -> locs.contains(a.getLocation()));
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onGrow(BlockFormEvent event) {
        if (getFeatures().stream().anyMatch(a -> a.getStructure().containsBlock(event.getBlock())))
            event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onFlow(BlockFromToEvent event) {
        if (getFeatures().stream().anyMatch(a -> a.getStructure().containsBlock(event.getToBlock())))
            event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (getFeatures().stream().anyMatch(a -> event.getBlock().getLocation().equals(a.getInitialBlock())))
            return;
        
        if (getFeatures().stream().anyMatch(a -> a.getStructure().containsBlock(event.getBlock())))
            event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (getFeatures().stream().anyMatch(a -> event.getBlock().getLocation().equals(a.getInitialBlock())))
            return;
        
        if (getFeatures().stream().anyMatch(a -> a.getStructure().containsBlock(event.getBlock())))
            event.setCancelled(true);
    }
}
