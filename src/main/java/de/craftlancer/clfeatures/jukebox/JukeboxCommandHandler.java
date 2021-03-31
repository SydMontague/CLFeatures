package de.craftlancer.clfeatures.jukebox;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.command.CommandHandler;

public class JukeboxCommandHandler extends CommandHandler {
    public JukeboxCommandHandler(CLFeatures plugin, JukeboxFeature feature) {
        super(plugin);
        
        registerSubCommand("song", new JukeboxSongCommand(plugin, feature));
    }
}
