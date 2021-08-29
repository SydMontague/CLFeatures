package de.craftlancer.clfeatures.transmutationstation;

import de.craftlancer.clapi.blueprints.event.BlueprintPostPasteEvent;
import de.craftlancer.clfeatures.BlueprintFeature;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureCommandHandler;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TransmutationStationFeature extends BlueprintFeature<TransmutationStationFeatureInstance> {
    private List<TransmutationStationFeatureInstance> instances;
    private TransmutationStationGUI gui;
    
    public TransmutationStationFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "transmutationStation.limit"));
        
        gui = new TransmutationStationGUI();
    }
    
    @Override
    public boolean createInstance(Player creator, BlueprintPostPasteEvent e) {
        return instances.add(new TransmutationStationFeatureInstance(creator.getUniqueId(),
                new BlockStructure(e.getBlocksPasted()), e.getFeatureLocation(), e.getSchematic()));
    }
    
    @Override
    protected void deserialize(Configuration config) {
        instances = (List<TransmutationStationFeatureInstance>) config.getList("transmutationStation", new ArrayList<>());
    }
    
    @Override
    protected Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("transmutationStation", instances);
        return map;
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new FeatureCommandHandler(CLFeatures.getInstance(), this);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof TransmutationStationFeatureInstance)
            instances.remove(instance);
    }
    
    @Nonnull
    @Override
    public String getName() {
        return "TransmutationStation";
    }
    
    @Override
    public List<TransmutationStationFeatureInstance> getFeatures() {
        instances.removeIf(Objects::isNull);
        return instances;
    }
    
    public TransmutationStationGUI getGui() {
        return gui;
    }
}
