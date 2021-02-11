package de.craftlancer.clfeatures.trophydepositor.command;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorFeature;
import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TrophyDepositorSetScoreCommand extends SubCommand {
    
    private TrophyDepositorFeature feature;
    
    public TrophyDepositorSetScoreCommand(CLFeatures plugin, TrophyDepositorFeature feature) {
        super("trophydepositor.admin", plugin, true);
        
        this.feature = feature;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Utils.getMatches(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        if (args.length == 3)
            return Collections.singletonList("score");
        
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + "You do not have access to this command.";
        
        if (args.length < 2)
            return CLFeatures.CC_PREFIX + "You must enter a player name.";
        if (args.length < 3)
            return CLFeatures.CC_PREFIX + "You must enter a score.";
        
        Player player = Bukkit.getPlayer(args[1]);
        
        if (player == null)
            return CLFeatures.CC_PREFIX + "You must enter a valid player name.";
        
        double score;
        try {
            score = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            return CLFeatures.CC_PREFIX + "You must enter a valid score.";
        }
        
        feature.setScore(player.getUniqueId(), score);
        
        return CLFeatures.CC_PREFIX + "§aYou have set the score for " + player.getName() + " to " + String.format("%.2f", score) + ".";
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
