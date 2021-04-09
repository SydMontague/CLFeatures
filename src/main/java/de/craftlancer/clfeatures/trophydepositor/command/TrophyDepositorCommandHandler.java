package de.craftlancer.clfeatures.trophydepositor.command;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureCommandHandler;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorFeature;

public class TrophyDepositorCommandHandler extends FeatureCommandHandler {
    public TrophyDepositorCommandHandler(CLFeatures plugin, TrophyDepositorFeature feature) {
        super(plugin, feature);
        
        registerSubCommand("listTrophies", new TrophyDepositorItemListCommand(plugin, feature));
        registerSubCommand("addTrophy", new TrophyDepositorItemAddCommand(plugin, feature));
        registerSubCommand("removeTrophy", new TrophyDepositorItemRemoveCommand(plugin, feature));
        registerSubCommand("addBoost", new TrophyDepositorAddBoostCommand(plugin, feature));
        registerSubCommand("getTrophy", new TrophyDepositorItemGetCommand(plugin, feature));
        registerSubCommand("boosts", new TrophyDepositorBoostsCommand(plugin, feature));
        registerSubCommand("setScore", new TrophyDepositorSetScoreCommand(plugin, feature));
        registerSubCommand("clearBoosts", new TrophyDepositorClearBoostsCommand(plugin, feature));
    }
}
