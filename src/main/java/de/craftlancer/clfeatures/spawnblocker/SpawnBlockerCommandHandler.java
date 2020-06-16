package de.craftlancer.clfeatures.spawnblocker;

import de.craftlancer.core.command.CommandHandler;
import org.bukkit.plugin.Plugin;

public class SpawnBlockerCommandHandler extends CommandHandler {
    public SpawnBlockerCommandHandler(Plugin plugin, SpawnBlockerFeature replicatorFeature) {
        super(plugin);
        
        registerSubCommand("move", new SpawnBlockerMoveCommand(plugin));
        registerSubCommand("list", new SpawnBlockerListCommand(plugin, replicatorFeature));
    }
}
