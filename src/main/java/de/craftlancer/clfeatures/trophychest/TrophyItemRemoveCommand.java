package de.craftlancer.clfeatures.trophychest;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;

public class TrophyItemRemoveCommand extends SubCommand {
    
    private TrophyChestFeature feature;

    public TrophyItemRemoveCommand(CLFeatures plugin, TrophyChestFeature feature) {
        super("clfeature.trophy.item.remove", plugin, true);
        this.feature = feature;
    }

    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if(!checkSender(sender))
            return "You're not allowed to use this command.";
        
        int hash = Utils.parseIntegerOrDefault(args[1], -1);
        
        return feature.removeTrophyByHash(hash) ? "Item removed" : "No item with given hash found.";
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
