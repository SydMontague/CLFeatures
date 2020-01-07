package de.craftlancer.clfeatures.portal;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.command.SubCommand;
import net.md_5.bungee.api.ChatColor;

public class PortalListCommand extends SubCommand {
    
    private PortalFeature feature;
    
    public PortalListCommand(Plugin plugin, PortalFeature feature) {
        super("", plugin, false);
        this.feature = feature;
    }

    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if(!checkSender(sender))
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't use this command.";
        
        sender.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "Name | Location");
        feature.getPortalsByPlayer((Player) sender).forEach(a -> {
            Location loc = a.getInitialBlock();
            sender.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + String.format("%s | %d, %d, %d", a.getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        });
        
        return null;
    }
    
    @Override
    public void help(CommandSender sender) {
        // TODO Auto-generated method stub
        
    }
    
}
