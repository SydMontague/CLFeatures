package de.craftlancer.clfeatures;

import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.SubCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class FeatureMoveCommand extends SubCommand {
    
    private Feature feature;
    
    public FeatureMoveCommand(Plugin plugin, Feature feature) {
        super("", plugin, false);
        
        this.feature = feature;
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't use this command.";
        
        Player p = (Player) sender;
        p.setMetadata(feature.getMoveMetaData(), new FixedMetadataValue(getPlugin(), ""));
        
        new LambdaRunnable(() -> {
            if (!p.hasMetadata(feature.getMoveMetaData()))
                return;
            
            p.removeMetadata(feature.getMoveMetaData(), getPlugin());
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + feature.getName() + " move timed out.");
        }).runTaskLater(getPlugin(), 1200L);
        
        return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "Right click the " + feature.getName() + " you want to move.";
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
