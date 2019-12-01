package de.craftlancer.clfeatures.portal;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.craftlancer.core.command.SubCommand;

public class PortalListCommand extends SubCommand {
    
    private PortalFeature feature;
    
    public PortalListCommand(Plugin plugin, PortalFeature feature) {
        super("", plugin, false);
        this.feature = feature;
    }

    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if(!checkSender(sender))
            return "You can't use this command.";
        
        sender.sendMessage("Name | Location");
        feature.getPortalsByPlayer((Player) sender).forEach(a -> {
            Location loc = a.getInitialBlock();
            sender.sendMessage(String.format("%s | %d, %d, %d", a.getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        });
        
        return null;
    }
    
    @Override
    public void help(CommandSender sender) {
        // TODO Auto-generated method stub
        
    }
    
}
