package de.craftlancer.clfeatures.amplifiedbeacon;

import de.craftlancer.clfeatures.BlueprintFeature;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPostPasteEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class AmplifiedBeaconFeature extends BlueprintFeature<AmplifiedBeaconFeatureInstance> {
    
    private List<AmplifiedBeaconFeatureInstance> instances;
    
    public AmplifiedBeaconFeature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        super(plugin, config, limitKey);
        
        this.instances = (List<AmplifiedBeaconFeatureInstance>) config.getList("instances", new ArrayList<>());
    }
    
    @Override
    public boolean createInstance(Player creator, BlueprintPostPasteEvent e) {
        return instances.add(new AmplifiedBeaconFeatureInstance(creator.getUniqueId(),
                new BlockStructure(e.getBlocksPasted()), e.getFeatureLocation(), e.getSchematic()));
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/amplifiedBeacon.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("amplifiedBeacon", instances);
        
        BukkitRunnable saveTask = new LambdaRunnable(() -> {
            try {
                config.save(f);
            } catch (IOException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Error while saving Amplified Beacon: ", e);
            }
        });
        
        if (getPlugin().isEnabled())
            saveTask.runTaskAsynchronously(getPlugin());
        else
            saveTask.run();
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return null;
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        instances.remove(instance);
    }
    
    @Nonnull
    @Override
    public String getName() {
        return "AmplifiedBeacon";
    }
    
    @Override
    public List<AmplifiedBeaconFeatureInstance> getFeatures() {
        return instances;
    }
}
