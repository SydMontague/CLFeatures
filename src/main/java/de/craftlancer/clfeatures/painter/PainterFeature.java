package de.craftlancer.clfeatures.painter;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureCommandHandler;
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

public class PainterFeature extends ItemFrameFeature<PainterFeatureInstance> {
    
    private List<PainterFeatureInstance> instances;
    
    public PainterFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "painter.limit"));
        
        YamlConfiguration painterConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/painter.yml"));
        
        instances = (List<PainterFeatureInstance>) painterConfig.getList("painters", new ArrayList<>());
    }
    
    @Override
    public boolean createInstance(Player creator, BlueprintPostPasteEvent event) {
        return instances.add(new PainterFeatureInstance(creator.getUniqueId(), new BlockStructure(event.getBlocksPasted()),
                event.getFeatureLocation(), event.getSchematic(), event.getPastedEntities()));
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
    
    @Nonnull
    @Override
    protected String getName() {
        return "Painter";
    }
    
    @Override
    public List<PainterFeatureInstance> getFeatures() {
        return instances;
    }
}
