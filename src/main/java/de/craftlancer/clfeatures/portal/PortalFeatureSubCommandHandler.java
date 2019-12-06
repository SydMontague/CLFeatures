package de.craftlancer.clfeatures.portal;

import org.bukkit.plugin.Plugin;

import de.craftlancer.core.command.CommandHandler;

public class PortalFeatureSubCommandHandler extends CommandHandler {
    
    public PortalFeatureSubCommandHandler(Plugin plugin, PortalFeature feature) {
        super(plugin);
        
        registerSubCommand("list", new PortalListCommand(getPlugin(), feature));
        registerSubCommand("name", new PortalNameCommand(getPlugin(), feature));
        registerSubCommand("move", new PortalMoveCommand(getPlugin()));
        registerSubCommand("help", new PortalHelpCommand(getPlugin(), getCommands()));
    }
}
