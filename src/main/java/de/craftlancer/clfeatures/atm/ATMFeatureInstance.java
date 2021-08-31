package de.craftlancer.clfeatures.atm;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.Feature;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.ItemFrameFeatureInstance;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ATMFeatureInstance extends ItemFrameFeatureInstance {

    private ATMFeature manager;

    protected ATMFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, String usedSchematic, List<Entity> entities) {
        super(ownerId, blocks, location, usedSchematic, entities);
    }

    protected ATMFeatureInstance(Map<String, Object> map) {
        super(map);
    }

    @Override
    protected void tick() {

    }

    @Override
    protected void interact(PlayerInteractEvent event) {
        super.interact(event);
    }

    @Override
    protected ATMFeature getManager() {
        if (manager == null)
            manager = (ATMFeature) CLFeatures.getInstance().getFeature("atm");
        return manager;
    }
}
