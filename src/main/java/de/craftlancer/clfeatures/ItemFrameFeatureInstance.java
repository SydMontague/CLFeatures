package de.craftlancer.clfeatures;

import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public abstract class ItemFrameFeatureInstance extends FeatureInstance {
    
    private UUID itemFrame;
    private ItemStack usedItem;
    
    public ItemFrameFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, ItemStack usedItem, UUID itemFrameUUID) {
        super(ownerId, blocks, location, null);
        
        this.usedItem = usedItem.clone();
        this.itemFrame = itemFrameUUID;
    }
    
    public ItemFrameFeatureInstance(Map<String, Object> map) {
        super(map);
        
        this.itemFrame = UUID.fromString((String) map.get("itemFrame"));
        this.usedItem = (ItemStack) map.get("usedItem");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("itemFrame", itemFrame.toString());
        map.put("usedItem", usedItem);
        
        return map;
    }
    
    public ItemStack getUsedItem() {
        return usedItem;
    }
    
    public UUID getItemFrame() {
        return itemFrame;
    }
}
