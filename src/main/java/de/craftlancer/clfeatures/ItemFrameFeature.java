package de.craftlancer.clfeatures;

import de.craftlancer.clapi.clfeatures.AbstractFeature;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public abstract class ItemFrameFeature<T extends ItemFrameFeatureInstance> extends BlueprintFeature<T> implements AbstractFeature {
    
    protected ItemFrameFeature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        super(plugin, config, limitKey);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof ItemFrameFeatureInstance)
            ((ItemFrameFeatureInstance) instance).getEntities().forEach(Entity::remove);
    }
}
