package de.craftlancer.clfeatures.trophydepositor.command;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorFeature;
import de.craftlancer.core.CLCore;
import de.craftlancer.core.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class TrophyDepositorAddBoostCommand extends SubCommand {
    private TrophyDepositorFeature feature;
    
    public TrophyDepositorAddBoostCommand(CLFeatures plugin, TrophyDepositorFeature feature) {
        super("clfeature.trophy.admin", plugin, true);
        
        this.feature = feature;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Collections.singletonList("boostAmount");
        if (args.length == 3)
            return Collections.singletonList("numberTrophies");
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender))
            return CLFeatures.CC_PREFIX + "You do not have access to this command.";
        
        if (args.length < 2)
            return CLFeatures.CC_PREFIX + "You must enter the boost amount!";
        
        if (args.length < 3)
            return CLFeatures.CC_PREFIX + "You must enter how many times this boost can be used!";
        
        double boost;
        int size;
        try {
            boost = Double.parseDouble(args[1]);
            size = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return CLFeatures.CC_PREFIX + "You must enter a valid number!";
        }
        
        feature.addBoost(boost, size);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            CLCore.getInstance().getPlayerTaskScheduler().schedule(player, () -> {
                player.sendTitle("§ax" + boost + " Trophy boost activated!",
                        "§dApplies for the next " + size + " trophies!", 10, 70, 20);
                player.sendMessage(CLFeatures.CC_PREFIX + "§ax" + boost + " Trophy boost activated!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1F);
            }, 100);
        }
        
        return CLFeatures.CC_PREFIX + "Successfully added trophy boost.";
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
