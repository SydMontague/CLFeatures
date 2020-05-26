package de.craftlancer.clfeatures.replicator;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.command.SubCommand;
import net.md_5.bungee.api.ChatColor;

public class ReplicatorListCommand extends SubCommand {
    private ReplicatorFeature feature;
    
    public ReplicatorListCommand(Plugin plugin, ReplicatorFeature feature) {
        super("", plugin, false);
        this.feature = feature;
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't use this command.";
        
        Player p = (Player) sender;
        
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 1.2F);
        p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + " Your placed replicators:");
        p.sendMessage("      " + ChatColor.GOLD + "# | Location");
        
        int counter = 1;
        for (ReplicatorFeatureInstance featureInstance : feature.getReplicatorsByUUID(p.getUniqueId())) {
            Location location = featureInstance.getInitialBlock();
            p.sendMessage("      " + ChatColor.YELLOW + counter + " | " + location.getWorld().getName().toUpperCase() + ", " + (int) location.getX() + ", " + (int) location.getY() + ", " + (int) location.getZ());
            counter++;
        }
        
        
        return null;
    }
    
    @Override
    public void help(CommandSender sender) {
        // TODO Auto-generated method stub
        
    }
}
