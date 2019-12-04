package de.craftlancer.clfeatures;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitTask;

import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.structure.BlockStructure;

public abstract class FeatureInstance implements Listener {
    private BukkitTask task;
    
    private UUID ownerId;
    private BlockStructure structure;
    private Location initialBlock;
    
    public FeatureInstance(UUID ownerId, BlockStructure blocks, Location location) {
        this.ownerId = ownerId;
        this.structure = blocks;
        this.initialBlock = location;

        Bukkit.getPluginManager().registerEvents(this, CLFeatures.getInstance());
        task = new LambdaRunnable(this::tick).runTaskTimer(CLFeatures.getInstance(), 10L, 10L);
    }
    
    public FeatureInstance(Map<String, Object> map) {
        this.ownerId = UUID.fromString(map.get("owner").toString());
        this.initialBlock = (Location) map.get("lecternLoc");
        this.structure = (BlockStructure) map.get("structure");

        Bukkit.getPluginManager().registerEvents(this, CLFeatures.getInstance());
        task = new LambdaRunnable(this::tick).runTaskTimer(CLFeatures.getInstance(), 10L, 10L);
    }

    public boolean isOwner(Player player) {
        return player.getUniqueId().equals(ownerId);
    }
    
    protected abstract void tick();
    
    protected abstract Feature getManager();
    
    protected void destroy() {
        HandlerList.unregisterAll(this);
        getManager().remove(this);
        task.cancel();
        
        structure.forEach(a -> a.getBlock().setType(Material.AIR));
    }
    
    public Location getInitialBlock() {
        return initialBlock.clone();
    }
    
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public BlockStructure getStructure() {
        return structure;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.getBlocks().stream().anyMatch(structure::containsBlock))
            event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.getBlocks().stream().anyMatch(structure::containsBlock))
            event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExplosion(EntityExplodeEvent event) {
        event.blockList().removeIf(structure::containsBlock);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInitialDestroy(BlockBreakEvent event) {
        if (!event.getBlock().getLocation().equals(initialBlock))
            return;
        
        
        destroy();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getLocation().equals(initialBlock))
            return;
        
        if (structure.containsBlock(event.getBlock()))
            event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getLocation().equals(initialBlock))
            return;
        
        if (structure.containsBlock(event.getBlock()))
            event.setCancelled(true);
    }
}
