package de.craftlancer.clfeatures.trophychest;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.command.CommandHandler;

public class TrophyCommandHandler extends CommandHandler {
    
    public TrophyCommandHandler(CLFeatures plugin, TrophyChestFeature feature) {
        super(plugin);
        
        registerSubCommand("list", new TrophyItemListCommand(plugin, feature));
        registerSubCommand("add", new TrophyItemAddCommand(plugin, feature));
        registerSubCommand("remove", new TrophyItemRemoveCommand(plugin, feature));
    }
    
}
