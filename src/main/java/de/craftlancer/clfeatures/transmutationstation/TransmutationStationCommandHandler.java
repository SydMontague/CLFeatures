package de.craftlancer.clfeatures.transmutationstation;

import de.craftlancer.core.command.CommandHandler;
import org.bukkit.plugin.Plugin;

public class TransmutationStationCommandHandler extends CommandHandler {
    public TransmutationStationCommandHandler(Plugin plugin, TransmutationStationFeature transmutationStationFeature) {
        super(plugin);
        
        registerSubCommand("move", new TransmutationStationMoveCommand(plugin));
        registerSubCommand("list", new TransmutationStationListCommand(plugin, transmutationStationFeature));
    }
}
