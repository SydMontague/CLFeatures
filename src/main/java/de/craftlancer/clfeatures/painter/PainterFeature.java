package de.craftlancer.clfeatures.painter;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureCommandHandler;
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

public class PainterFeature extends ManualPlacementFeature<PainterFeatureInstance> {
    
    private List<PainterFeatureInstance> instances;
    
    public PainterFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "painter.limit"));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void deserialize(Configuration config) {
        instances = (List<PainterFeatureInstance>) config.getList("painters", new ArrayList<>());        
    }
    
    @Override
    protected Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("painters", instances);
        return map;
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new FeatureCommandHandler(getPlugin(), this);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof PainterFeatureInstance)
            instances.remove(instance);
    }
    
    @Nonnull
    @Override
    public String getName() {
        return "Painter";
    }
    
    @Override
    public List<PainterFeatureInstance> getFeatures() {
        return instances;
    }
    
    @Override
    public boolean createInstance(Player creator, Block initialBlock, ItemStack hand) {
        return instances.add(new PainterFeatureInstance(creator.getUniqueId(), new BlockStructure(initialBlock.getLocation()), initialBlock.getLocation(), hand));
    }
    
    @Override
    public boolean isFeatureItem(ItemStack item) {
        return false;
    }
    
    @Override
    public Collection<Block> checkEnvironment(Block initialBlock) {
        return Collections.emptyList();
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
