package de.craftlancer.clfeatures.furniture;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.Feature;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.ItemFrameFeatureInstance;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FurnitureFeatureInstance extends ItemFrameFeatureInstance {
    protected FurnitureFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, String usedSchematic, List<Entity> entities) {
        super(ownerId, blocks, location, usedSchematic, entities);
    }
    
    public FurnitureFeatureInstance(Map<String, Object> map) {
        super(map);
    }
    
    @Override
    public Map<String, Object> serialize() {
        return super.serialize();
    }
    
    @Override
    protected void tick() {
    
    }
    
    @Override
    protected Feature<? extends FeatureInstance> getManager() {
        return CLFeatures.getInstance().getFeature("furniture");
    }
}
