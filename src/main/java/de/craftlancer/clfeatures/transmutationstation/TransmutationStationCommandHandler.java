package de.craftlancer.clfeatures.transmutationstation;

import de.craftlancer.clfeatures.FeatureCommandHandler;
import org.bukkit.plugin.Plugin;

public class TransmutationStationCommandHandler extends FeatureCommandHandler {
    public TransmutationStationCommandHandler(Plugin plugin, TransmutationStationFeature transmutationStationFeature) {
        super(plugin, transmutationStationFeature);
        
        registerSubCommand("list", new TransmutationStationListCommand(plugin, transmutationStationFeature));
    }
}
