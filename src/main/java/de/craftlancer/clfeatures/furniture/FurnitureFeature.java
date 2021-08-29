package de.craftlancer.clfeatures.furniture;

import de.craftlancer.clapi.blueprints.event.BlueprintPostPasteEvent;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureCommandHandler;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.ItemFrameFeature;
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

public class FurnitureFeature extends ItemFrameFeature<FurnitureFeatureInstance> {
    
    private List<FurnitureFeatureInstance> instances;
    
    public FurnitureFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "furniture.limit"));
    }
    
    @Override
    public boolean createInstance(Player creator, BlueprintPostPasteEvent event) {
        return instances.add(new FurnitureFeatureInstance(creator.getUniqueId(), new BlockStructure(event.getBlocksPasted()), event.getFeatureLocation(),
                event.getSchematic(), event.getPastedEntities()));
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        super.remove(instance);
        
        if (instance instanceof FurnitureFeatureInstance)
            instances.remove(instance);
    }
    
    @Override
    protected void deserialize(Configuration config) {
        this.instances = (List<FurnitureFeatureInstance>) config.getList("instances", new ArrayList<>());
    }
    
    @Override
    protected Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("instances", instances);
        
        return map;
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new FeatureCommandHandler(getPlugin(), this);
    }
    
    @Nonnull
    @Override
    public String getName() {
        return "Furniture";
    }
    
    @Override
    public List<FurnitureFeatureInstance> getFeatures() {
        return instances;
    }
    
    @Override
    protected BreakAction getBreakAction() {
        return BreakAction.DROP_IF_ANY;
    }
    
    @Override
    public long getTickFrequency() {
        return -1;
    }
}
