package de.craftlancer.clfeatures.chair;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.ItemFrameFeatureInstance;
import de.craftlancer.core.structure.BlockStructure;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChairFeatureInstance extends ItemFrameFeatureInstance {
    
    private ChairFeature manager;
    // Location -> ArmorStand UUID
    private Map<Location, UUID> entities = new HashMap<>();
    
    public ChairFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, String usedSchematic, List<Entity> entities) {
        super(ownerId, blocks, location, usedSchematic, entities);
    }
    
    public ChairFeatureInstance(Map<String, Object> map) {
        super(map);
        
        entities = ((Map<Location, String>) map.getOrDefault("armorstands", new HashMap<>())).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> UUID.fromString(e.getValue())));
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("armorstands", entities.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())));
        
        return map;
    }
    
    @Override
    protected void tick() {
        entities.entrySet().removeIf(a -> Bukkit.getEntity(a.getValue()) == null || Bukkit.getEntity(a.getValue()).getPassengers().isEmpty());
    }
    
    @Override
    protected ChairFeature getManager() {
        if (manager == null)
            manager = (ChairFeature) CLFeatures.getInstance().getFeature("chair");
        
        return manager;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        Block block = event.getClickedBlock();
        Player p = event.getPlayer();
        
        if (!getStructure().containsBlock(block))
            return;
        
        if (p.hasMetadata(getManager().getMoveMetaData()) && getOwnerId().equals(p.getUniqueId())) {
            destroy();
            getManager().giveFeatureItem(p, this);
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "Chair successfully moved back to your inventory.");
            p.removeMetadata(getManager().getMoveMetaData(), getManager().getPlugin());
            return;
        }
        
        if (entities.containsKey(block.getLocation()) && Bukkit.getEntity(entities.get(block.getLocation())) != null) {
            ArmorStand a = (ArmorStand) Bukkit.getEntity(entities.get(block.getLocation()));
            if (a.getPassengers().isEmpty())
                a.addPassenger(p);
            return;
        }
        
        ArmorStand armorStand = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().add(0.5, 0.25, 0.5), EntityType.ARMOR_STAND);
        
        armorStand.setMarker(true);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        
        armorStand.addPassenger(p);
        entities.put(block.getLocation(), armorStand.getUniqueId());
    }
    
    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        entities.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(event.getDismounted().getUniqueId())) {
                event.getDismounted().remove();
                return true;
            }
            return false;
        });
        
    }
}