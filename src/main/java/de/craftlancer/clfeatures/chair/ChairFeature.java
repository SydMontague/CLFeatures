package de.craftlancer.clfeatures.chair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureCommandHandler;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.ItemFrameFeature;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPostPasteEvent;

public class ChairFeature extends ItemFrameFeature<ChairFeatureInstance> {
    
    private List<ChairFeatureInstance> instances;
    
    public ChairFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "chair.limit"));
    }
    
    @Override
    public boolean createInstance(Player creator, BlueprintPostPasteEvent event) {
        return instances.add(new ChairFeatureInstance(creator.getUniqueId(), new BlockStructure(event.getBlocksPasted()),
                event.getFeatureLocation(), event.getSchematic(), event.getPastedEntities()));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void deserialize(Configuration config) {
        instances = (List<ChairFeatureInstance>) config.getList("chairs", new ArrayList<>());
    }
    
    @Override
    protected Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("chairs", instances);
        return map;
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        super.remove(instance);
        
        if (instance instanceof ChairFeatureInstance)
            instances.remove(instance);
    }
    
    @Override
    public long getTickFrequency() {
        return 80;
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new FeatureCommandHandler(CLFeatures.getInstance(), this);
    }
    
    @Nonnull
    @Override
    public String getName() {
        return "Chair";
    }
    
    @Override
    public List<ChairFeatureInstance> getFeatures() {
        return instances;
    }
    
    @Override
    protected BreakAction getBreakAction() {
        return BreakAction.DROP_IF_ANY;
    }
}
