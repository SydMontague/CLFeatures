package de.craftlancer.clfeatures.spawnblocker;

import de.craftlancer.clfeatures.FeatureCommandHandler;
import org.bukkit.plugin.Plugin;

public class SpawnBlockerCommandHandler extends FeatureCommandHandler {
    public SpawnBlockerCommandHandler(Plugin plugin, SpawnBlockerFeature replicatorFeature) {
        super(plugin, replicatorFeature);
        
        registerSubCommand("list", new SpawnBlockerListCommand(plugin, replicatorFeature));
    }
}
