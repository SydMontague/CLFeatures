package de.craftlancer.clfeatures;

import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Location;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class BlueprintFeatureInstance extends FeatureInstance {
    
    private String usedSchematic;
    
    protected BlueprintFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, String usedSchematic) {
        super(ownerId, blocks, location);
        
        this.usedSchematic = usedSchematic;
    }
    
    protected BlueprintFeatureInstance(Map<String, Object> map) {
        super(map);
        
        this.usedSchematic = Objects.toString(map.get("usedSchematic"), null);
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("usedSchematic", usedSchematic);
        
        return map;
    }
    
    public String getUsedSchematic() {
        return usedSchematic;
    }
}
