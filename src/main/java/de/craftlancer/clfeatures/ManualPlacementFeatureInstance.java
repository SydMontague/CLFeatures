package de.craftlancer.clfeatures;

import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public abstract class ManualPlacementFeatureInstance extends FeatureInstance {
    
    private ItemStack usedItem;
    
    public ManualPlacementFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, ItemStack usedItem) {
        super(ownerId, blocks, location);
        
        this.usedItem = usedItem.clone();
        this.usedItem.setAmount(1);
    }
    
    public ManualPlacementFeatureInstance(Map<String, Object> map) {
        super(map);
        
        this.usedItem = (ItemStack) map.get("usedItem");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("usedItem", usedItem);
        
        return map;
    }
    
    public ItemStack getUsedItem() {
        return usedItem;
    }
}
