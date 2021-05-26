package de.craftlancer.clfeatures.portal;

import de.craftlancer.clfeatures.FeatureCommandHandler;
import org.bukkit.plugin.Plugin;

public class PortalFeatureSubCommandHandler extends FeatureCommandHandler {
    
    public PortalFeatureSubCommandHandler(Plugin plugin, PortalFeature feature) {
        super(plugin, feature, true, false, true);
        
        registerSubCommand("list", new PortalListCommand(getPlugin(), feature));
        registerSubCommand("name", new PortalNameCommand(getPlugin(), feature), "address");
        registerSubCommand("help", new PortalHelpCommand(getPlugin(), getCommands()));
    }
}
