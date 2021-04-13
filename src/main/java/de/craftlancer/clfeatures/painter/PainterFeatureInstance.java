package de.craftlancer.clfeatures.painter;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.ManualPlacementFeatureInstance;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Location;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class PainterFeatureInstance extends ManualPlacementFeatureInstance {
    
    public PainterFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, ItemStack usedItem) {
        super(ownerId, blocks, location, usedItem);
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
