package de.craftlancer.clfeatures;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public abstract class ManualPlacementFeature<T extends ManualPlacementFeatureInstance> extends Feature<T> {
    
    public ManualPlacementFeature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        super(plugin, config, limitKey);
    }
    
    public abstract boolean createInstance(Player creator, Block initialBlock, ItemStack hand);
    
    public abstract boolean isFeatureItem(ItemStack item);
    
    public abstract Collection<Block> checkEnvironment(Block initialBlock);
    
    @Override
    public ItemStack getFeatureItem(T instance) {
        return instance.getUsedItem();
    }
}
