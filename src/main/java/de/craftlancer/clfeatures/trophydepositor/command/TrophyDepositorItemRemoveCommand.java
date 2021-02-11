package de.craftlancer.clfeatures.trophydepositor.command;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorFeature;
import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class TrophyDepositorItemRemoveCommand extends SubCommand {
    
    private TrophyDepositorFeature feature;
    
    public TrophyDepositorItemRemoveCommand(CLFeatures plugin, TrophyDepositorFeature feature) {
        super("clfeature.trophy.item.remove", plugin, true);
        this.feature = feature;
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + "You're not allowed to use this command.";
        
        int hash = Utils.parseIntegerOrDefault(args[1], -1);
        
        return CLFeatures.CC_PREFIX + (feature.removeTrophyByHash(hash) ? "Item removed" : "No item with given hash found.");
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
    
    @Override
    public void help(CommandSender sender) {
        // not implemented
        
    }
    
}
