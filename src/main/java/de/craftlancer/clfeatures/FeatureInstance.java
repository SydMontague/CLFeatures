package de.craftlancer.clfeatures;

import de.craftlancer.clapi.clfeatures.AbstractFeatureInstance;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//TODO transfer feature ownership
public abstract class FeatureInstance implements Listener, ConfigurationSerializable, AbstractFeatureInstance {
    private UUID ownerId;
    private BlockStructure structure;
    private Location initialBlock;
    
    protected FeatureInstance(UUID ownerId, BlockStructure blocks, Location location) {
        this.ownerId = ownerId;
        this.structure = blocks;
        this.initialBlock = location;
        
        Bukkit.getPluginManager().registerEvents(this, CLFeatures.getInstance());
    }
    
    protected FeatureInstance(Map<String, Object> map) {
        this.ownerId = UUID.fromString(map.get("owner").toString());
        this.initialBlock = (Location) map.get("lecternLoc");
        this.structure = (BlockStructure) map.get("structure");
        
        Bukkit.getPluginManager().registerEvents(this, CLFeatures.getInstance());
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("owner", ownerId.toString());
        map.put("structure", structure);
        map.put("lecternLoc", getInitialBlock()); // legacy name
        
        return map;
    }
    
    @Override
    public boolean isOwner(OfflinePlayer player) {
        return isOwner(player.getUniqueId());
    }
    
    @Override
    public boolean isOwner(UUID uuid) {
        return ownerId.equals(uuid);
    }
    
    protected abstract void tick();
    
    protected abstract Feature<? extends FeatureInstance> getManager();
    
    /**
     * Validation & moving is done by manager
     */
    protected void interact(PlayerInteractEvent event) {
    
    }
    
    @Override
    public void destroy() {
        HandlerList.unregisterAll(this);
        getManager().remove(this);
        
        structure.forEach(a -> a.getBlock().setType(Material.AIR));
    }
    
    @Override
    public Location getInitialBlock() {
        return initialBlock.clone();
    }
    
    @Override
    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
    
    @Override
    public UUID getOwnerId() {
        return ownerId;
    }
    
    @Override
    public BlockStructure getStructure() {
        return structure;
    }
}
