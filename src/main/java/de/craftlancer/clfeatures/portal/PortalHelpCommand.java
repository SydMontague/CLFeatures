package de.craftlancer.clfeatures.portal;

import java.util.Map;

import org.bukkit.Bukkit;
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
        Bukkit.dispatchCommand(sender, "cchelp portal");
    }
    
}
