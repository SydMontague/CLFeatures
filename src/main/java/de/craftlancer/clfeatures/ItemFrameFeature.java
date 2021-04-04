package de.craftlancer.clfeatures;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public abstract class ItemFrameFeature<T extends ItemFrameFeatureInstance> extends BlueprintFeature<T> {
    
    public ItemFrameFeature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        super(plugin, config, limitKey);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof ItemFrameFeatureInstance)
            ((ItemFrameFeatureInstance) instance).getEntities().forEach(Entity::remove);
    }
}
