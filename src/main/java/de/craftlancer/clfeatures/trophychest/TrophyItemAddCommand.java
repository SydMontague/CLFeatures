package de.craftlancer.clfeatures.trophychest;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;

public class TrophyItemAddCommand extends SubCommand {
    
    private TrophyChestFeature feature;
    
    public TrophyItemAddCommand(CLFeatures plugin, TrophyChestFeature feature) {
        super("clfeature.trophy.item.add", plugin, false);
        this.feature = feature;
    }

    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return "You must be a player to use this.";
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand().clone();
        item.setAmount(1);
        
        if(item.getType().isAir())
            return "Please hold an Item in your hand.";
        if (args.length < 2)
            return "You must specify a value";
        
        int value = Utils.parseIntegerOrDefault(args[1], -1);
        if(value == -1)
            return "You must specify a value";
        
        return feature.addTrophyItem(item, value) ? "Item replaced" : "Item added.";
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
