package de.craftlancer.clfeatures.trophydepositor.command;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorFeature;
import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class TrophyDepositorResetAllCommand extends SubCommand {
    private TrophyDepositorFeature feature;
    
    public TrophyDepositorResetAllCommand(CLFeatures plugin, TrophyDepositorFeature feature) {
        super("clfeature.trophy.admin", plugin, true);
        
        this.feature = feature;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Utils.getMatches(args[1], Collections.singletonList("ConfirmDelete"));
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + "You do not have access to this command.";
        
        if (args.length < 2 || !args[1].equals("ConfirmDelete"))
            return CLFeatures.CC_PREFIX + "You must type 'ConfirmDelete' as a safeguard measure to confirm you want to remove all scores.";
        
        
        feature.clearScores();
        
        return CLFeatures.CC_PREFIX + "Successfully reset all scores.";
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
