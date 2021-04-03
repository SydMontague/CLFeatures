package de.craftlancer.clfeatures.transmutationstation;

import de.craftlancer.clfeatures.BlueprintFeatureInstance;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.structure.BlockStructure;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;
import java.util.UUID;

public class TransmutationStationFeatureInstance extends BlueprintFeatureInstance {
    
    public static final String MOVE_METADATA = "transmutationStationMove";
    private TransmutationStationFeature manager;
    
    public TransmutationStationFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, String usedSchematic) {
        super(ownerId, blocks, location, usedSchematic);
    }
    
    public TransmutationStationFeatureInstance(Map<String, Object> map) {
        super(map);
    }
    
    @Override
    protected void tick() {
        // nothing to tick
    }
    
    @Override
    protected TransmutationStationFeature getManager() {
        if (manager == null)
            manager = (TransmutationStationFeature) CLFeatures.getInstance().getFeature("transmutationStation");
        
        return manager;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        Player p = event.getPlayer();
        Block block = event.getClickedBlock();
        
        if (!getStructure().containsBlock(block))
            return;
        
        if (event.getPlayer().hasMetadata(MOVE_METADATA)) {
            if (!getOwnerId().equals(p.getUniqueId()))
                return;
            
            destroy();
            getManager().giveFeatureItem(p);
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "Transmutation station successfully moved back to your inventory.");
            p.removeMetadata(MOVE_METADATA, getManager().getPlugin());
        } else
            getManager().getGui().display(event.getPlayer());
    }
}
