package de.craftlancer.clfeatures;

import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public abstract class ItemFrameFeatureInstance extends ManualPlacementFeatureInstance {
    
    private UUID itemFrame;
    
    public ItemFrameFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, ItemStack usedItem, UUID itemFrameUUID) {
        super(ownerId, blocks, location, usedItem);
        
        this.itemFrame = itemFrameUUID;
    }
    
    public ItemFrameFeatureInstance(Map<String, Object> map) {
        super(map);
        
        this.itemFrame = UUID.fromString((String) map.get("itemFrame"));
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("itemFrame", itemFrame.toString());
        
        return map;
    }
    
    public UUID getItemFrame() {
        return itemFrame;
    }
}
