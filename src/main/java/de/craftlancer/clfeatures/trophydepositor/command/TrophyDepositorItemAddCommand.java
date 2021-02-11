package de.craftlancer.clfeatures.trophydepositor.command;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorFeature;
import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class TrophyDepositorItemAddCommand extends SubCommand {
    
    private TrophyDepositorFeature feature;
    
    public TrophyDepositorItemAddCommand(CLFeatures plugin, TrophyDepositorFeature feature) {
        super("clfeature.trophy.item.add", plugin, false);
        this.feature = feature;
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + "You must be a player to use this.";
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand().clone();
        item.setAmount(1);
        
        if (item.getType().isAir())
            return CLFeatures.CC_PREFIX + "Please hold an Item in your hand.";
        if (args.length < 2)
            return CLFeatures.CC_PREFIX + "You must specify a value";
        
        int value = Utils.parseIntegerOrDefault(args[1], -1);
        if (value == -1)
            return CLFeatures.CC_PREFIX + "You must specify a value";
        
        return CLFeatures.CC_PREFIX + (feature.addTrophyItem(item, value) ? "Item replaced" : "Item added.");
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Collections.singletonList("value");
        return Collections.emptyList();
    }
    
    @Override
    public void help(CommandSender sender) {
        // not implemented
    }
    
}
