package de.craftlancer.clfeatures.stonecrusher;

import org.bukkit.plugin.Plugin;

import de.craftlancer.core.command.CommandHandler;

public class StoneCrusherCommandHandler extends CommandHandler {

    public StoneCrusherCommandHandler(Plugin plugin, StoneCrusherFeature feature) {
        super(plugin);
        
        registerSubCommand("move", new StoneCrusherMoveCommand(plugin));
        registerSubCommand("list", new StoneCrusherListCommand(plugin, feature));
    }
    
}
