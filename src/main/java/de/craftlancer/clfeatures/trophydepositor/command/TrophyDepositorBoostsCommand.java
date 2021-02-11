package de.craftlancer.clfeatures.trophydepositor.command;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorBoost;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorFeature;
import de.craftlancer.core.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TrophyDepositorBoostsCommand extends SubCommand {
    
    private TrophyDepositorFeature feature;
    
    public TrophyDepositorBoostsCommand(CLFeatures plugin, TrophyDepositorFeature feature) {
        super("", plugin, false);
        
        this.feature = feature;
    }
    
    @Override
    protected String execute(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!checkSender(commandSender))
            return CLFeatures.CC_PREFIX + "You cannot use this command.";
        
        commandSender.sendMessage("§8§m-----------------------------");
        if (feature.getBoosts().size() > 0)
            commandSender.sendMessage(" §7Current trophy boosts:");
        else
            commandSender.sendMessage(" §7There are currently no trophy boosts.");
        for (TrophyDepositorBoost boost : feature.getBoosts())
            commandSender.sendMessage("  §7- §dx" + boost.getBoost() + " §7| §5" + boost.getTrophiesLeft() + " §dtrophies remaining.");
        commandSender.sendMessage("§8§m-----------------------------");
        
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
