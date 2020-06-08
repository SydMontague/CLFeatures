package de.craftlancer.clfeatures.spawnblocker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import de.craftlancer.core.Utils;

public class SpawnBlockGroup implements ConfigurationSerializable {
    private Material itemType;
    private String itemName;
    private List<String> itemLore;
    private List<EntityType> blockedEntities;
    private boolean defaultState;

    @SuppressWarnings("unchecked")
    public SpawnBlockGroup(Map<?,?> map) {
        this.itemType = Material.matchMaterial(map.get("itemType").toString());
        this.itemName = map.get("itemName").toString();
        this.itemLore = (List<String>) map.get("itemLore");
        this.blockedEntities = ((List<String>) map.get("blockedEntities")).stream().map(EntityType::valueOf).collect(Collectors.toList());
        this.defaultState = ((Boolean) map.get("defaultState")).booleanValue();
    }
    
    public boolean containsType(EntityType entityType) {
        return blockedEntities.contains(entityType);
    }

    public boolean getDefaultState() {
        return defaultState;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("itemType", itemType);
        map.put("itemName", itemName);
        map.put("itemLore", itemLore);
        map.put("blockedEntities", blockedEntities);
        map.put("defaultState", defaultState);
        return map;
    }

    public ItemStack getItem() {
        return Utils.buildItemStack(itemType, itemName, itemLore);
    }
}