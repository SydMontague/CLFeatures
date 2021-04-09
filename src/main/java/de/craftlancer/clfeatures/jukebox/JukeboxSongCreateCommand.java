package de.craftlancer.clfeatures.jukebox;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class JukeboxSongCreateCommand extends SubCommand {
    
    private CLFeatures plugin;
    private JukeboxFeature feature;
    
    public JukeboxSongCreateCommand(CLFeatures plugin, JukeboxFeature feature) {
        super("", plugin, false);
        
        this.plugin = plugin;
        this.feature = feature;
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        
        player.getPassengers().forEach(Entity::remove);
        
        ArmorStand a = (ArmorStand) player.getWorld().spawnEntity(player.getLocation().add(0, 1, 0), EntityType.ARMOR_STAND);
        ArmorStand temp = (ArmorStand) player.getWorld().spawnEntity(player.getLocation().add(0, 1, 0), EntityType.ARMOR_STAND);
        
        player.addPassenger(temp);
        temp.addPassenger(a);
        
        temp.setSmall(true);
        temp.setGravity(false);
        temp.setAI(false);
        temp.setCustomNameVisible(false);
        a.setGravity(false);
        a.setMarker(true);
        a.setAI(false);
        a.setCustomNameVisible(true);
        a.setCustomName("\uE002");
        
        if (!item.getType().name().contains("MUSIC_DISC")) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must hold a music disc.");
            return null;
        }
        
        if (feature.isSongItem(item)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "This music disc is already a jukebox song.");
            return null;
        }
        
        
        feature.createSong(item);
        
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Successfully created a song with this music disc.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
