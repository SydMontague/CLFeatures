package de.craftlancer.clfeatures.transmutationstation;

import de.craftlancer.clfeatures.BlueprintFeature;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class TransmutationStationFeature extends BlueprintFeature<TransmutationStationFeatureInstance> {
    private List<TransmutationStationFeatureInstance> instances;
    private TransmutationStationGUI gui;
    
    public TransmutationStationFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "transmutationStation.limit"));
        
        instances = (List<TransmutationStationFeatureInstance>) YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/transmutationStation.yml"))
                .getList("transmutationStation", new ArrayList<>());
        
        gui = new TransmutationStationGUI();
    }
    
    @Override
    public boolean createInstance(Player creator, Block initialBlock, List<Location> blocks, String schematic) {
        return instances.add(new TransmutationStationFeatureInstance(creator.getUniqueId(), new BlockStructure(blocks), initialBlock.getLocation(), schematic));
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/transmutationStation.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("transmutationStation", instances);
        
        BukkitRunnable saveTask = new LambdaRunnable(() -> {
            try {
                config.save(f);
            } catch (IOException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Error while saving TransmutationStation: ", e);
            }
        });
        
        if (getPlugin().isEnabled())
            saveTask.runTaskAsynchronously(getPlugin());
        else
            saveTask.run();
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new TransmutationStationCommandHandler(CLFeatures.getInstance(), this);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof TransmutationStationFeatureInstance)
            instances.remove(instance);
    }
    
    @Nonnull
    @Override
    protected String getName() {
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
