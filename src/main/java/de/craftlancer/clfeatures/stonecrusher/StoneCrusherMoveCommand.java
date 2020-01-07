package de.craftlancer.clfeatures.stonecrusher;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.SubCommand;
import net.md_5.bungee.api.ChatColor;

public class StoneCrusherMoveCommand extends SubCommand {
    
    public StoneCrusherMoveCommand(Plugin plugin) {
        super("", plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if(!checkSender(sender))
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't use this command.";

        Player p = (Player) sender;
        p.setMetadata(StoneCrusherFeatureInstance.MOVE_METADATA, new FixedMetadataValue(getPlugin(), ""));

        new LambdaRunnable(() ->  {
            if(!p.hasMetadata(StoneCrusherFeatureInstance.MOVE_METADATA))
                return;
            
            p.removeMetadata(StoneCrusherFeatureInstance.MOVE_METADATA, getPlugin());
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "StoneCrusher move timed out.");
        }).runTaskLater(getPlugin(), 1200L);
        
        return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "Right click your StoneCrusher you want to move.";
    }
    
    @Override
    public void help(CommandSender sender) {
        // TODO Auto-generated method stub
        
    }
    
}
