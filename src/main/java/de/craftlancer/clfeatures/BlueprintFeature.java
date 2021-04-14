package de.craftlancer.clfeatures;

import de.craftlancer.core.CLCore;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPostPasteEvent;
import me.sizzlemcgrizzle.blueprints.util.SchematicUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public abstract class BlueprintFeature<T extends BlueprintFeatureInstance> extends Feature<T> {
    
    protected BlueprintFeature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        super(plugin, config, limitKey);
    }
    
    public abstract boolean createInstance(Player creator, BlueprintPostPasteEvent event);
    
    @Override
    public ItemStack getFeatureItem(T instance) {
        List<ItemStack> items = instance != null ? SchematicUtil.getBlueprint(instance.getUsedSchematic()) : Collections.emptyList();
        return items.isEmpty() ? CLCore.getInstance().getItemRegistry().getItem(getFeatureItemRegistryKey()).orElseGet(() -> new ItemStack(Material.AIR)) : items.get(0);
    }
}
