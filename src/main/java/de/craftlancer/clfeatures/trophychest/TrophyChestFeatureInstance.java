package de.craftlancer.clfeatures.trophychest;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.structure.BlockStructure;
import net.md_5.bungee.api.ChatColor;

public class TrophyChestFeatureInstance extends FeatureInstance {
    
    public static final String MOVE_METADATA = "trophyChest.move";
    private TrophyChestFeature manager;
    private int score = 0;
    
    public TrophyChestFeatureInstance(TrophyChestFeature manager, UUID ownerId, BlockStructure blocks, Location location) {
        super(ownerId, blocks, location);
        this.manager = manager;
    }
    
    public TrophyChestFeatureInstance(Map<String, Object> map) {
        super(map);
        
        this.score = (int) map.getOrDefault("score", 0);
    }
    
    public double getScore() {
        return score;
    }
    
    @Override
    protected void tick() {
        World w = getInitialBlock().getWorld();
        
        if (!w.isChunkLoaded(getInitialBlock().getBlockX() >> 4, getInitialBlock().getBlockZ() >> 4))
            return;
        
        w.spawnParticle(Particle.PORTAL, getInitialBlock().clone().add(0, 1, 0), 5);
    }
    
    @Override
    protected TrophyChestFeature getManager() {
        if (manager == null)
            manager = (TrophyChestFeature) CLFeatures.getInstance().getFeature("trophyChest");
        
        return manager;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("score", score);
        
        return map;
    }
    
    private boolean isInventory(InventoryHolder holder) {
        Inventory i = holder.getInventory();
        
        if (holder instanceof DoubleChest)
            return ((DoubleChestInventory) i).getLeftSide().getLocation().equals(getInitialBlock())
                    || ((DoubleChestInventory) i).getRightSide().getLocation().equals(getInitialBlock());
        else if (holder instanceof BlockInventoryHolder)
            return ((BlockInventoryHolder) holder).getBlock().getLocation().equals(getInitialBlock());
        else
            return false;
    }
    
    private void recalculateScore(Inventory inventory) {
        this.score = StreamSupport.stream(inventory.spliterator(), false).collect(Collectors.summingInt(a -> getManager().getItemValue(a)));
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onItemMove(InventoryMoveItemEvent event) {
        if (isInventory(event.getDestination().getHolder()) || isInventory(event.getSource().getHolder()))
            event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClose(InventoryCloseEvent event) {
        if (!isInventory(event.getInventory().getHolder()))
            return;
        
        recalculateScore(event.getInventory());
        event.getPlayer().sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "New trophy score is: " + this.score);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteractMove(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (!event.hasBlock() || !p.hasMetadata(MOVE_METADATA))
            return;
        
        if (!getStructure().containsBlock(event.getClickedBlock()))
            return;
        
        if (!getOwnerId().equals(p.getUniqueId()))
            return;
        
        destroy();
        getManager().giveFeatureItem(p);
        p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "TrophyChest successfully moved back to your inventory.");
        p.removeMetadata(MOVE_METADATA, getManager().getPlugin());
    }
    
    // override destroy listener
    @Override
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInitialDestroy(BlockBreakEvent event) {
        if (!event.getBlock().getLocation().equals(getInitialBlock()))
            return;
        
        event.getPlayer().sendMessage("§4The Trophychest cannot be destroyed! §eUse §2/trophychest move §e instead!");
        event.setCancelled(true);
    }
}
