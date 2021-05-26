package de.craftlancer.clfeatures;

import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureAddLimitCommand extends SubCommand {
    
    private Feature<?> feature;
    
    public FeatureAddLimitCommand(Plugin plugin, Feature<?> feature) {
        super("clfeatures.admin", plugin, false);
        
        this.feature = feature;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Utils.getMatches(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        
        if (args.length == 3)
            return Collections.singletonList("<limit>");
        
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't use this command.";
        
        if (args.length < 3)
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You must enter an online player's name and a limit.";
        
        Player player = Bukkit.getPlayer(args[1]);
        
        if (player == null)
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "The player name you entered is invalid or offline.";
        
        int limit;
        try {
            limit = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You must enter a valid number for the limit.";
        }
        
        if (feature.getMaxLimit() < limit)
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "The limit you entered is greater than the feature's maximum limit.";
        
        if (feature.getLimit(player) + limit < 0)
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "The value you entered will make the player's limit less than 0.";
        
        if (feature.getLimit(player) + limit > feature.getMaxLimit())
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "The value you entered will make the player's limit more than the maximum limit for this feature.";
        
        feature.addFeatureLimit(player, limit);
        return CLFeatures.CC_PREFIX + "Player feature limit has been updated.";
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}