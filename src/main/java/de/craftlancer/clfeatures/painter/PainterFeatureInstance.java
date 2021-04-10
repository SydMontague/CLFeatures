package de.craftlancer.clfeatures.painter;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.ItemFrameFeatureInstance;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PainterFeatureInstance extends ItemFrameFeatureInstance {
    
    public PainterFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, String usedSchematic, List<Entity> entities) {
        super(ownerId, blocks, location, usedSchematic, entities);
    }
    
    public PainterFeatureInstance(Map<String, Object> map) {
        super(map);
    }
    
    @Override
    protected void tick() {
    
    }
    
    @Override
    protected void interact(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        event.setCancelled(true);
        event.getPlayer().openInventory(new PaintMenu(getManager().getPlugin()).getInventory());
    }
    
    @Override
    protected PainterFeature getManager() {
        return (PainterFeature) CLFeatures.getInstance().getFeature("painter");
    }
}
