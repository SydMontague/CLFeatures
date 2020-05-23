package de.craftlancer.clfeatures.replicator;

import de.craftlancer.core.command.CommandHandler;
import org.bukkit.plugin.Plugin;

public class ReplicatorCommandHandler extends CommandHandler {
    public ReplicatorCommandHandler(Plugin plugin, ReplicatorFeature replicatorFeature) {
        super(plugin);
        
        registerSubCommand("move", new ReplicatorMoveCommand(plugin));
        registerSubCommand("list", new ReplicatorListCommand(plugin, replicatorFeature));
    }
}
