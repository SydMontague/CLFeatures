package de.craftlancer.clfeatures.portal;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.SubCommand;

public class PortalNameCommand extends SubCommand {
    private PortalFeature feature;
    
    public PortalNameCommand(Plugin plugin, PortalFeature feature) {
        super("", plugin, false);
        this.feature = feature;
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if(!checkSender(sender))
            return "You can't use this command.";
        
        if(args.length < 2)
            return "You must specify a name.";
        
        String name = args[1];
        
        if(feature.getPortal(name) != null)
            return "A portal with that name already exists.";
        
        Player p = (Player) sender;
        p.setMetadata("portalRename", new FixedMetadataValue(getPlugin(), name));
        
        new LambdaRunnable(() ->  {
            if(!p.hasMetadata("portalRename"))
                return;
            
            p.removeMetadata("portalRename", getPlugin());
            p.sendMessage("Portal rename timed out.");
        }).runTaskLater(getPlugin(), 1200L);
        
        return String.format("Please right click on the portal you want to name %s.", name);
    }
    
    @Override
    public void help(CommandSender sender) {
        // TODO Auto-generated method stub
        
    }
    
}
