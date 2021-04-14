package de.craftlancer.clfeatures;

import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class ItemFrameFeatureInstance extends BlueprintFeatureInstance {
    
    private List<UUID> entities;
    
    protected ItemFrameFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, String usedSchematic, List<Entity> entities) {
        super(ownerId, blocks, location, usedSchematic);
        
        this.entities = entities.stream().filter(e -> e instanceof ItemFrame).map(Entity::getUniqueId).collect(Collectors.toList());
    }
    
    @SuppressWarnings("unchecked")
    protected ItemFrameFeatureInstance(Map<String, Object> map) {
        super(map);
        
        this.entities = ((List<String>) map.get("itemFrame")).stream().map(UUID::fromString).collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("itemFrame", entities.stream().map(UUID::toString).collect(Collectors.toList()));
        
        return map;
    }
    
    public List<Entity> getEntities() {
        return entities.stream().filter(e -> Bukkit.getEntity(e) != null).map(Bukkit::getEntity).collect(Collectors.toList());
    }
}
