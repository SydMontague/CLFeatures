package de.craftlancer.clfeatures.painter;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureCommandHandler;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.ManualPlacementFeature;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class PainterFeature extends ManualPlacementFeature<PainterFeatureInstance> {
    
    private List<PainterFeatureInstance> instances;
    
    public PainterFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "painter.limit"));
        
        YamlConfiguration painterConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/painter.yml"));
        
        instances = (List<PainterFeatureInstance>) painterConfig.getList("painters", new ArrayList<>());
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/painter.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("painters", instances);
        
        BukkitRunnable saveTask = new LambdaRunnable(() -> {
            try {
                config.save(f);
            } catch (IOException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Error while saving Painter: ", e);
            }
        });
        
        if (getPlugin().isEnabled())
            saveTask.runTaskAsynchronously(getPlugin());
        else
            saveTask.run();
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
    protected String getName() {
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
}
