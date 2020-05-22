package de.craftlancer.clfeatures.replicator;

import de.craftlancer.clfeatures.Feature;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Location;

import java.util.UUID;

public class ReplicatorFeatureInstance extends FeatureInstance {
    public static final String MOVE_METADATA = "replicatorMove";
    
    public ReplicatorFeatureInstance(UUID ownerId, BlockStructure blocks, Location location) {
        super(ownerId, blocks, location);
    }
    
    @Override
    protected void tick() {
    
    }
    
    @Override
    protected Feature getManager() {
        return null;
    }
}
