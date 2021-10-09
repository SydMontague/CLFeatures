package de.craftlancer.clfeatures;

import de.craftlancer.clapi.clfeatures.AbstractManualPlacementFeature;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public abstract class ManualPlacementFeature<T extends ManualPlacementFeatureInstance> extends Feature<T> implements AbstractManualPlacementFeature {
    
    protected ManualPlacementFeature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        super(plugin, config, limitKey);
    }
    
    @Override
    public ItemStack getFeatureItem(T instance) {
        return instance.getUsedItem();
    }
}
