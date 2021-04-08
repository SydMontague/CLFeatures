package de.craftlancer.clfeatures.jukebox;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureCommandHandler;

public class JukeboxCommandHandler extends FeatureCommandHandler {
    public JukeboxCommandHandler(CLFeatures plugin, JukeboxFeature feature) {
        super(plugin, feature);
        
        registerSubCommand("song", new JukeboxSongCommand(plugin, feature));
    }
}
