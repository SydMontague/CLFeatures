package de.craftlancer.clfeatures.portal;

import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import de.craftlancer.core.command.HelpCommand;
import de.craftlancer.core.command.SubCommand;

public class PortalHelpCommand extends HelpCommand {
    
    public PortalHelpCommand(Plugin plugin, Map<String, SubCommand> map) {
        super("", plugin, map);
    }

    @Override
    public void help(CommandSender sender) {
        sender.sendMessage("Get your first portal by ranking up to citizen rank.");
        sender.sendMessage("Make sure to have enough space for a frame to generate and place down your portal (lectern).");
        sender.sendMessage("Use \"/portal name x\" to name your portal. Keep your portal name to yourself and trusted friends!");
        sender.sendMessage("Place a written book with the portal name you want to travel to in the lectern to use the portal.");
        sender.sendMessage("Visit: https://craftlancer.de/wiki/index.php/Portals for more information!");
    }
    
}
