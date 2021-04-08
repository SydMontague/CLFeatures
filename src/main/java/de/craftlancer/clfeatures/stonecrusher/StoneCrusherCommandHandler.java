package de.craftlancer.clfeatures.stonecrusher;

import de.craftlancer.clfeatures.FeatureCommandHandler;
import org.bukkit.plugin.Plugin;

public class StoneCrusherCommandHandler extends FeatureCommandHandler {
    
    public StoneCrusherCommandHandler(Plugin plugin, StoneCrusherFeature feature) {
        super(plugin, feature);
        
        registerSubCommand("list", new StoneCrusherListCommand(plugin, feature));
    }
    
}
