package de.craftlancer.clfeatures;

import de.craftlancer.core.command.CommandHandler;
import org.bukkit.plugin.Plugin;

public class FeatureCommandHandler extends CommandHandler {
    
    public FeatureCommandHandler(Plugin plugin, Feature<? extends FeatureInstance> feature) {
        this(plugin, feature, true, true);
    }
    
    public FeatureCommandHandler(Plugin plugin, Feature<? extends FeatureInstance> feature, boolean withMove, boolean withList) {
        this(plugin, feature, withMove, withList, false);
    }
    
    public FeatureCommandHandler(Plugin plugin, Feature<? extends FeatureInstance> feature, boolean withMove, boolean withList, boolean withLimit) {
        super(plugin);
        
        if (withMove)
            registerSubCommand("move", new FeatureMoveCommand(plugin, feature));
        if (withList)
            registerSubCommand("list", new FeatureListCommand(plugin, feature));
        if (withMove)
            registerSubCommand("addLimit", new FeatureAddLimitCommand(plugin, feature));
    }
    
}
