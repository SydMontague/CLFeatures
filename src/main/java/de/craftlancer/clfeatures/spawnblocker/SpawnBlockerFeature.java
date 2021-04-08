package de.craftlancer.clfeatures.spawnblocker;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class SpawnBlockerFeature extends BlueprintFeature<SpawnBlockerFeatureInstance> {
    private List<SpawnBlockerFeatureInstance> instances = new ArrayList<>();
    private Map<SpawnBlockGroupSlot, SpawnBlockGroup> blockGroups = new EnumMap<>(SpawnBlockGroupSlot.class);
    
    public SpawnBlockerFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "spawnblocker.limit"));
        
        YamlConfiguration groupConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "spawnGroups.yml"));
        for (SpawnBlockGroupSlot a : SpawnBlockGroupSlot.values())
            blockGroups.put(a, (SpawnBlockGroup) groupConfig.get(a.name()));
        
        instances = (List<SpawnBlockerFeatureInstance>) YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/spawnBlocker.yml"))
                .getList("spawnBlocker", new ArrayList<>());
    }
    
    public Map<SpawnBlockGroupSlot, SpawnBlockGroup> getBlockGroups() {
        return blockGroups;
    }
    
    @Override
    public boolean createInstance(Player creator, BlueprintPostPasteEvent e) {
        return instances.add(new SpawnBlockerFeatureInstance(this, creator.getUniqueId(),
                new BlockStructure(e.getBlocksPasted()), e.getFeatureLocation(), e.getSchematic()));
    }
    
    @Override
    public String getMoveMetaData() {
        return "spawnBlockerMove";
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/spawnBlocker.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("spawnBlocker", getFeatures());
        
        BukkitRunnable saveTask = new LambdaRunnable(() -> {
            try {
                config.save(f);
            } catch (IOException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Error while saving SpawnBlockers: ", e);
            }
        });
        
        if (getPlugin().isEnabled())
            saveTask.runTaskAsynchronously(getPlugin());
        else
            saveTask.run();
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new SpawnBlockerCommandHandler(getPlugin(), this);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof SpawnBlockerFeatureInstance) {
            instances.remove(instance);
        }
    }
    
    @Override
    protected String getName() {
        return "SpawnBlocker";
    }
    
    @Override
    public List<SpawnBlockerFeatureInstance> getFeatures() {
        return instances;
    }
    
}
