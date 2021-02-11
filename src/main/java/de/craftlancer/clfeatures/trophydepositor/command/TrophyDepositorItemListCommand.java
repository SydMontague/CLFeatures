package de.craftlancer.clfeatures.trophydepositor.command;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorFeature;
import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

public class TrophyDepositorItemListCommand extends SubCommand {
    
    private TrophyDepositorFeature feature;
    
    public TrophyDepositorItemListCommand(CLFeatures plugin, TrophyDepositorFeature feature) {
        super("clfeature.trophy.item.list", plugin, true);
        this.feature = feature;
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + "You're not allowed to use this command.";
        
        if (feature.getTrophyItems().size() == 0)
            sender.sendMessage(CLFeatures.CC_PREFIX + "There are currently no trophies.");
        else
            sender.sendMessage("§7ID | Item | Value | Action");
        
        int id = 0;
        for (Entry<ItemStack, Integer> a : feature.getTrophyItems().entrySet()) {
            BaseComponent delAction = new TextComponent("[Delete]");
            delAction.setColor(ChatColor.RED);
            delAction.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/trophydepositor removeTrophy " + a.getKey().hashCode()));
            delAction.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to delete trophy.").color(ChatColor.GRAY).create()));
            BaseComponent itemAction = Utils.getItemComponent(a.getKey());
            itemAction.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/trophydepositor getTrophy " + a.getKey().hashCode()));
            
            BaseComponent base = new TextComponent(Integer.toString(id));
            base.addExtra(" | ");
            base.addExtra(itemAction);
            base.addExtra(" | ");
            base.addExtra(Integer.toString(a.getValue()));
            base.addExtra(" | ");
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
