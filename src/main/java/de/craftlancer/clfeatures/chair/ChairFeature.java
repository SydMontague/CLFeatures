package de.craftlancer.clfeatures.chair;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureCommandHandler;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.ItemFrameFeature;
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

public class ChairFeature extends ItemFrameFeature<ChairFeatureInstance> {
    
    private List<ChairFeatureInstance> instances;
    
    public ChairFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "chair.limit"));
        
        YamlConfiguration jukeboxConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/chair.yml"));
        
        instances = (List<ChairFeatureInstance>) jukeboxConfig.getList("chairs", new ArrayList<>());
    }
    
    @Override
    public boolean createInstance(Player creator, BlueprintPostPasteEvent event) {
        return instances.add(new ChairFeatureInstance(creator.getUniqueId(), new BlockStructure(event.getBlocksPasted()),
                event.getFeatureLocation(), event.getSchematic(), event.getPastedEntities()));
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/chair.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("chairs", instances);
        
        BukkitRunnable saveTask = new LambdaRunnable(() -> {
            try {
                config.save(f);
            } catch (IOException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Error while saving Chair: ", e);
            }
        });
        
        if (getPlugin().isEnabled())
            saveTask.runTaskAsynchronously(getPlugin());
        else
            saveTask.run();
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        super.remove(instance);
        
        if (instance instanceof ChairFeatureInstance)
            instances.remove(instance);
    }
    
    @Override
    public long getTickFrequency() {
        return 80;
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new FeatureCommandHandler(CLFeatures.getInstance(), this);
    }
    
    @Nonnull
    @Override
    protected String getName() {
        return "Chair";
    }
    
    @Override
    public List<ChairFeatureInstance> getFeatures() {
        return instances;
    }
    
    @Override
    protected BreakAction getBreakAction() {
        return BreakAction.DROP_IF_ANY;
    }
}
