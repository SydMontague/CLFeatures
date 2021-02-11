package de.craftlancer.clfeatures.trophydepositor.command;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorFeature;
import de.craftlancer.core.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TrophyDepositorClearBoostsCommand extends SubCommand {
    private TrophyDepositorFeature feature;
    
    public TrophyDepositorClearBoostsCommand(CLFeatures plugin, TrophyDepositorFeature feature) {
        super("clfeature.trophy.admin", plugin, true);
        
        this.feature = feature;
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + "You do not have access to this command.";
        
        feature.getBoosts().clear();
        return CLFeatures.CC_PREFIX + "Successfully cleared all boosts";
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
