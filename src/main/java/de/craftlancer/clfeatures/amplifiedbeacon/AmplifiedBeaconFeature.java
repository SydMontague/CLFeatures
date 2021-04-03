package de.craftlancer.clfeatures.amplifiedbeacon;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.Feature;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class AmplifiedBeaconFeature extends Feature<AmplifiedBeaconFeatureInstance> {
    
    private List<AmplifiedBeaconFeatureInstance> instances;
    
    public AmplifiedBeaconFeature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        super(plugin, config, limitKey);
        
        this.instances = (List<AmplifiedBeaconFeatureInstance>) config.getList("instances", new ArrayList<>());
    }
    
    // Unused with blueprints
    @Override
    public boolean isFeatureItem(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().getKeys().stream()
                .anyMatch(k -> k.getKey().equals(getPlugin().getFeatureItemKey().getKey())
                        && item.getItemMeta().getPersistentDataContainer().get(k, PersistentDataType.STRING).equalsIgnoreCase(getName()));
    }
    
    // Unused with blueprints
    @Override
    public Collection<Block> checkEnvironment(Block initialBlock) {
        return null;
    }
    
    // Unused with blueprints
    @Override
    public boolean createInstance(Player creator, Block initialBlock, ItemStack hand) {
        return false;
    }
    
    @Override
    public boolean createInstance(Player creator, Block initialBlock, List<Location> blocks, String schematic) {
        return instances.add(new AmplifiedBeaconFeatureInstance(creator.getUniqueId(), new BlockStructure(blocks), initialBlock.getLocation(), schematic));
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
    protected String getName() {
        return "AmplifiedBeacon";
    }
    
    @Override
    public List<AmplifiedBeaconFeatureInstance> getFeatures() {
        return instances;
    }
}
