package de.craftlancer.clfeatures;

import de.craftlancer.clapi.blueprints.AbstractBlueprint;
import de.craftlancer.clapi.blueprints.PluginBlueprints;
import de.craftlancer.clapi.clfeatures.AbstractBlueprintFeature;
import de.craftlancer.core.CLCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public abstract class BlueprintFeature<T extends BlueprintFeatureInstance> extends Feature<T> implements AbstractBlueprintFeature {
    
    protected BlueprintFeature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        super(plugin, config, limitKey);
    }
    
    @Override
    public ItemStack getFeatureItem(T instance) {
        
        Optional<? extends AbstractBlueprint> optional = Optional.empty();
        
        if (instance != null)
            optional = Bukkit.getServicesManager().load(PluginBlueprints.class).getBlueprint(instance.getUsedSchematic());
        
        return optional.isPresent() ?
                optional.get().getItem().clone() :
                CLCore.getInstance().getItemRegistry().getItem(getFeatureItemRegistryKey()).orElseGet(() -> new ItemStack(Material.AIR));
    }
}
