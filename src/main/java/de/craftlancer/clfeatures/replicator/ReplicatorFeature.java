package de.craftlancer.clfeatures.replicator;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.Feature;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.command.CommandHandler;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public class ReplicatorFeature extends Feature {
    public ReplicatorFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "replicator.limit"));
    }
    
    @Override
    public boolean isFeatureItem(ItemStack item) {
        return false;
    }
    
    @Override
    public boolean checkFeatureLimit(Player player) {
        return false;
    }
    
    @Override
    public Collection<Block> checkEnvironment(Block initialBlock) {
        return null;
    }
    
    @Override
    public boolean createInstance(Player creator, Block initialBlock) {
        return false;
    }
    
    @Override
    public boolean createInstance(Player creator, Block initialBlock, List<Location> blocks) {
        return false;
    }
    
    @Override
    public void save() {
    
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return null;
    }
    
    @Override
    public void remove(FeatureInstance instance) {
    
    }
    
    @Nonnull
    @Override
    protected String getName() {
        return null;
    }
}
