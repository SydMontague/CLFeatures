package de.craftlancer.clfeatures.trophychest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.SubCommand;
import net.md_5.bungee.api.ChatColor;

public class TrophyChestMoveCommand extends SubCommand {
    
    public TrophyChestMoveCommand(Plugin plugin) {
        super("", plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if(!checkSender(sender))
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't use this command.";

        Player p = (Player) sender;
        p.setMetadata(TrophyChestFeatureInstance.MOVE_METADATA, new FixedMetadataValue(getPlugin(), ""));

        new LambdaRunnable(() ->  {
            if(!p.hasMetadata(TrophyChestFeatureInstance.MOVE_METADATA))
                return;
            
            p.removeMetadata(TrophyChestFeatureInstance.MOVE_METADATA, getPlugin());
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "TrophyChest move timed out.");
        }).runTaskLater(getPlugin(), 1200L);
        
        return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "Right click your TrophyChest you want to move.";
    }
    
    @Override
    public void help(CommandSender sender) {
        // TODO Auto-generated method stub
        
    }
    
}