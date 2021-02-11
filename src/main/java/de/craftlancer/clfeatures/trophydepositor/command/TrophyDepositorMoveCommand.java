package de.craftlancer.clfeatures.trophydepositor.command;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorFeatureInstance;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.SubCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class TrophyDepositorMoveCommand extends SubCommand {
    
    public TrophyDepositorMoveCommand(Plugin plugin) {
        super("", plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't use this command.";
        
        Player p = (Player) sender;
        p.setMetadata(TrophyDepositorFeatureInstance.MOVE_METADATA, new FixedMetadataValue(getPlugin(), ""));
        
        new LambdaRunnable(() -> {
            if (!p.hasMetadata(TrophyDepositorFeatureInstance.MOVE_METADATA))
                return;
            
            p.removeMetadata(TrophyDepositorFeatureInstance.MOVE_METADATA, getPlugin());
            p.sendMessage(CLFeatures.CC_PREFIX + "TrophyDepositor move timed out.");
        }).runTaskLater(getPlugin(), 1200L);
        
        return CLFeatures.CC_PREFIX + "Right click the TrophyDepositor you want to move.";
    }
    
    @Override
    public void help(CommandSender sender) {
        // not implemented
    }
    
}
