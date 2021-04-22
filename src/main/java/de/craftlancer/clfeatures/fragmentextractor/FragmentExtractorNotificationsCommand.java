package de.craftlancer.clfeatures.fragmentextractor;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FragmentExtractorNotificationsCommand extends SubCommand {
    private FragmentExtractorFeature feature;
    
    public FragmentExtractorNotificationsCommand(Plugin plugin, FragmentExtractorFeature feature) {
        super("", plugin, false);
        this.feature = feature;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Utils.getMatches(args[1], Arrays.asList("true", "false"));
        
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't use this command.";
        
        Player p = (Player) sender;
        
        if (args.length < 2)
            return CLFeatures.CC_PREFIX + "You must enter true or false.";
        
        boolean bool = Boolean.parseBoolean(args[1]);
        
        if (feature.setNotifications(p, bool))
            return CLFeatures.CC_PREFIX + ChatColor.GREEN + "Notification settings have been updated.";
        else
            return CLFeatures.CC_PREFIX + "You do not have any placed fragment extractors.";
    }
    
    @Override
    public void help(CommandSender sender) {
        // not implemented        
    }
}
