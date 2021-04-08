package de.craftlancer.clfeatures;

import de.craftlancer.core.command.CommandHandler;
import org.bukkit.plugin.Plugin;

public class FeatureCommandHandler extends CommandHandler {
    
    public FeatureCommandHandler(Plugin plugin, Feature feature) {
        super(plugin);
        
        registerSubCommand("move", new FeatureMoveCommand(plugin, feature));
    }
    
}
