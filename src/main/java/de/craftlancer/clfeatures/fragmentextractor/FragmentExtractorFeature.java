package de.craftlancer.clfeatures.fragmentextractor;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.ManualPlacementFeature;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentExtractorFeature extends ManualPlacementFeature<FragmentExtractorFeatureInstance> {
    
    private List<FragmentExtractorFeatureInstance> instances;
    
    public FragmentExtractorFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "fragment.limit"));
    }
    
    @Override
    public boolean createInstance(Player creator, Block initialBlock, ItemStack hand) {
        return instances.add(new FragmentExtractorFeatureInstance(creator,
                new BlockStructure(initialBlock.getLocation()), initialBlock.getLocation(), hand));
    }
    
    @Override
    public boolean isFeatureItem(ItemStack item) {
        return false;
    }
    
    @Override
    public Collection<Block> checkEnvironment(Block initialBlock) {
        return Collections.emptyList();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void deserialize(Configuration config) {
        instances = (List<FragmentExtractorFeatureInstance>) config.getList("instances", new ArrayList<>());
    }
    
    @Override
    protected Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("instances", instances);
        
        return map;
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new FragmentExtractorCommandHandler(getPlugin(), this);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof FragmentExtractorFeatureInstance)
            instances.remove(instance);
    }
    
    @Nonnull
    @Override
    public String getName() {
        return "FragmentExtractor";
    }
    
    @Override
    public List<FragmentExtractorFeatureInstance> getFeatures() {
        return instances;
    }
    
    @Override
    protected BreakAction getBreakAction() {
        return BreakAction.DROP_IF_ANY;
    }
    
    @Override
    public long getTickFrequency() {
        return 20;
    }
    
    protected boolean setNotifications(Player player, boolean notify) {
        List<FragmentExtractorFeatureInstance> list = getFeaturesByUUID(player.getUniqueId());
        
        if (list.isEmpty())
            return false;
        
        list.forEach(f -> f.setNotify(notify));
        return true;
    }
}
