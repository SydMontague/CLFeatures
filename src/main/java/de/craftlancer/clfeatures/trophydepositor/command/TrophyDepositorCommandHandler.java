package de.craftlancer.clfeatures.trophydepositor.command;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorFeature;
import de.craftlancer.core.command.CommandHandler;

public class TrophyDepositorCommandHandler extends CommandHandler {
    public TrophyDepositorCommandHandler(CLFeatures plugin, TrophyDepositorFeature feature) {
        super(plugin);
        
        registerSubCommand("listTrophies", new TrophyDepositorItemListCommand(plugin, feature));
        registerSubCommand("addTrophy", new TrophyDepositorItemAddCommand(plugin, feature));
        registerSubCommand("removeTrophy", new TrophyDepositorItemRemoveCommand(plugin, feature));
        registerSubCommand("move", new TrophyDepositorMoveCommand(plugin));
        registerSubCommand("addBoost", new TrophyDepositorAddBoostCommand(plugin, feature));
        registerSubCommand("getTrophy", new TrophyDepositorItemGetCommand(plugin, feature));
        registerSubCommand("boosts", new TrophyDepositorBoostsCommand(plugin, feature));
        registerSubCommand("setScore", new TrophyDepositorSetScoreCommand(plugin, feature));
        registerSubCommand("clearBoosts", new TrophyDepositorClearBoostsCommand(plugin, feature));
    }
}
