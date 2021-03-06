package de.craftlancer.clfeatures.portal;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.command.SubCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class PortalListCommand extends SubCommand {
    
    private PortalFeature feature;
    
    public PortalListCommand(Plugin plugin, PortalFeature feature) {
        super("", plugin, false);
        this.feature = feature;
    }

    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if(!checkSender(sender))
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't use this command.";

        Player player = args.length >= 2 && sender.hasPermission("clfeature.portal.list.others") ? Bukkit.getPlayer(args[1]) : (Player) sender;
        
        if(player == null)
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You must specify a player.";
        
        sender.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + String.format("Your current portal limit: §2%d/%s", feature.getPortalsByPlayer(player).size(), feature.getLimit(player)));
        sender.sendMessage(ChatColor.YELLOW + "      Name | Location");
        feature.getPortalsByPlayer(player).forEach(a -> {
            Location loc = a.getInitialBlock();
            
            BaseComponent component = new TextComponent(ChatColor.YELLOW + String.format("      %s | %d, %d, %d   ", Optional.ofNullable(a.getName()).orElse("<no name>"), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            BaseComponent pbookAdd = new TextComponent("[Add]");
            pbookAdd.setColor(ChatColor.DARK_GREEN);
            pbookAdd.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pbook add " + a.getName()));
            pbookAdd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Click while holding an Address Book to add.").color(ChatColor.DARK_GRAY).create())));
            
            component.addExtra(pbookAdd);
            sender.spigot().sendMessage(component);
        });
        
        return null;
    }
    
    @Override
    public void help(CommandSender sender) {
        // not implemented        
    }
    
}
