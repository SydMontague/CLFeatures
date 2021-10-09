package de.craftlancer.clfeatures.amplifiedbeacon;

import de.craftlancer.clapi.blueprints.event.BlueprintPostPasteEvent;
import de.craftlancer.clfeatures.BlueprintFeature;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AmplifiedBeaconFeature extends BlueprintFeature<AmplifiedBeaconFeatureInstance> {
    
    private List<AmplifiedBeaconFeatureInstance> instances;
    
    public AmplifiedBeaconFeature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        super(plugin, config, limitKey);
    }
    
    @Override
    public boolean createInstance(Player creator, BlueprintPostPasteEvent e) {
        return instances.add(new AmplifiedBeaconFeatureInstance(creator.getUniqueId(),
                new BlockStructure(e.getBlocksPasted()), e.getFeatureLocation(), e.getSchematic()));
    }
    
    @Override
    protected void deserialize(Configuration config) {
        this.instances = (List<AmplifiedBeaconFeatureInstance>) config.getList("instances", new ArrayList<>());
    }
    
    @Override
    protected Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("amplifiedBeacon", instances);
        return map;
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return null;
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        instances.remove(instance);
    }
    
    @Nonnull
    @Override
    public String getName() {
        return "AmplifiedBeacon";
    }
    
    @Override
    public List<AmplifiedBeaconFeatureInstance> getFeatures() {
        return instances;
    }
}
