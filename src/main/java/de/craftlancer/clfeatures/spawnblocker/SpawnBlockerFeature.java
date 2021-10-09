package de.craftlancer.clfeatures.spawnblocker;

import de.craftlancer.clapi.blueprints.event.BlueprintPostPasteEvent;
import de.craftlancer.clfeatures.BlueprintFeature;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureCommandHandler;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnBlockerFeature extends BlueprintFeature<SpawnBlockerFeatureInstance> {
    private List<SpawnBlockerFeatureInstance> instances;
    private Map<SpawnBlockGroupSlot, SpawnBlockGroup> blockGroups = new EnumMap<>(SpawnBlockGroupSlot.class);
    
    public SpawnBlockerFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "spawnblocker.limit"));
        
        YamlConfiguration groupConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "spawnGroups.yml"));
        for (SpawnBlockGroupSlot a : SpawnBlockGroupSlot.values())
            blockGroups.put(a, (SpawnBlockGroup) groupConfig.get(a.name()));
    }
    
    public Map<SpawnBlockGroupSlot, SpawnBlockGroup> getBlockGroups() {
        return blockGroups;
    }
    
    @Override
    public boolean createInstance(Player creator, BlueprintPostPasteEvent e) {
        return instances.add(new SpawnBlockerFeatureInstance(this, creator.getUniqueId(),
                new BlockStructure(e.getBlocksPasted()), e.getFeatureLocation(), e.getSchematic()));
    }
    
    @Override
    protected void deserialize(Configuration config) {
        instances = (List<SpawnBlockerFeatureInstance>) config.getList("spawnBlocker", new ArrayList<>());
    }
    
    @Override
    protected Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("spawnBlocker", instances);
        return map;
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new FeatureCommandHandler(getPlugin(), this);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof SpawnBlockerFeatureInstance) {
            instances.remove(instance);
        }
    }
    
    @Override
    public String getName() {
        return "SpawnBlocker";
    }
    
    @Override
    public List<SpawnBlockerFeatureInstance> getFeatures() {
        return instances;
    }
    
}
