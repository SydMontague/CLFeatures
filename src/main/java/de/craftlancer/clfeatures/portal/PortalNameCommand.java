package de.craftlancer.clfeatures.portal;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.SubCommand;
import net.md_5.bungee.api.ChatColor;

public class PortalNameCommand extends SubCommand {
    private PortalFeature feature;
    
    public PortalNameCommand(Plugin plugin, PortalFeature feature) {
        super("", plugin, false);
        this.feature = feature;
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if(!checkSender(sender))
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't use this command.";
        
        if(args.length < 2)
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You must specify a name.";
        
        String name = args[1];
        
        if(feature.getPortal(name) != null)
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "A portal with that name already exists.";
        if(name.length() > 20)
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "The name is too long, 20 characters max.";
        
        Player p = (Player) sender;
        p.setMetadata(PortalFeatureInstance.RENAME_METADATA, new FixedMetadataValue(getPlugin(), name));
        
        new LambdaRunnable(() ->  {
            if(!p.hasMetadata(PortalFeatureInstance.RENAME_METADATA))
                return;
            
            p.removeMetadata(PortalFeatureInstance.RENAME_METADATA, getPlugin());
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "Portal rename timed out.");
        }).runTaskLater(getPlugin(), 1200L);
        
        return CLFeatures.CC_PREFIX + ChatColor.YELLOW + String.format("Please right click on the portal you want to name %s.", name);
    }
    
    @Override
    public void help(CommandSender sender) {
        // TODO Auto-generated method stub
        
    }
    
}
