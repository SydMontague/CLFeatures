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
import java.util.Optional;

public class TrophyDepositorItemGetCommand extends SubCommand {
    
    private TrophyDepositorFeature feature;
    
    public TrophyDepositorItemGetCommand(CLFeatures plugin, TrophyDepositorFeature feature) {
        super("clfeature.trophy.item.remove", plugin, false);
        this.feature = feature;
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + "You're not allowed to use this command.";
        
        int hash = Utils.parseIntegerOrDefault(args[1], -1);
        
        Player player = (Player) sender;
        
        Optional<ItemStack> optional = feature.getTrophyItems().keySet().stream().filter(i -> i.hashCode() == hash).findFirst();
        
        if (optional.isPresent()) {
            player.getInventory().addItem(optional.get());
            return CLFeatures.CC_PREFIX + "§aYou have received the item with hash: " + hash;
        }
        
        return CLFeatures.CC_PREFIX + "An item with this hash code does not exist. Use the /trophydepositor list to get a trophy.";
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
