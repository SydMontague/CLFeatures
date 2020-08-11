package de.craftlancer.clfeatures.trophychest;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class TrophyItemListCommand extends SubCommand {

    private TrophyChestFeature feature;
    
    public TrophyItemListCommand(CLFeatures plugin, TrophyChestFeature feature) {
        super("clfeature.trophy.item.list", plugin, true);
        this.feature = feature;
    }

    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if(!checkSender(sender))
            return "You're not allowed to use this command.";
        
        sender.sendMessage("ID - Item - Value - Action");
        
        int id = 0;
        for(Entry<ItemStack, Integer> a : feature.getTrophyItems().entrySet()) {
            BaseComponent delAction = new TextComponent("[Delete]");
            delAction.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/trophychest remove " + a.getKey().hashCode()));
            
            BaseComponent base = new TextComponent(Integer.toString(id));
            base.addExtra(" - ");
            base.addExtra(Utils.getItemComponent(a.getKey()));
            base.addExtra(" - ");
            base.addExtra(Integer.toString(a.getValue()));
            base.addExtra(" - ");
            base.addExtra(delAction);
            
            sender.spigot().sendMessage(base);
            id++;
        }
        
        return null;
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
