package de.craftlancer.clfeatures.replicator;

import de.craftlancer.clfeatures.FeatureCommandHandler;
import org.bukkit.plugin.Plugin;

public class ReplicatorCommandHandler extends FeatureCommandHandler {
    public ReplicatorCommandHandler(Plugin plugin, ReplicatorFeature replicatorFeature) {
        super(plugin, replicatorFeature);
        
        registerSubCommand("list", new ReplicatorListCommand(plugin, replicatorFeature));
    }
}
