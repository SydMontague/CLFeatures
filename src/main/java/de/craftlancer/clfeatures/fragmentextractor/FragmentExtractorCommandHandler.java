package de.craftlancer.clfeatures.fragmentextractor;

import de.craftlancer.clfeatures.FeatureCommandHandler;
import org.bukkit.plugin.Plugin;

public class FragmentExtractorCommandHandler extends FeatureCommandHandler {
    public FragmentExtractorCommandHandler(Plugin plugin, FragmentExtractorFeature feature) {
        super(plugin, feature);
        
        registerSubCommand("notifications", new FragmentExtractorNotificationsCommand(plugin, feature));
    }
}
