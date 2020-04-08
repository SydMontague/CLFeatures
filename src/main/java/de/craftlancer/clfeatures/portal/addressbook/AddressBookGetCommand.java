package de.craftlancer.clfeatures.portal.addressbook;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.portal.PortalFeature;
import de.craftlancer.core.command.SubCommand;

public class AddressBookGetCommand extends SubCommand {
    public AddressBookGetCommand(Plugin plugin) {
        super("clfeature.portal.book.get", plugin, true);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        Player player;
        if(sender instanceof Player)
            player = (Player) sender;
        else
            player = args.length >= 2 ? Bukkit.getPlayerExact(args[1]) : null;
        
        if(player == null)
            return "Player doesn't exist";
            
        ItemStack item = AddressBookUtils.writeBook(new ItemStack(Material.WRITTEN_BOOK),
                                                    "Valgard",
                                                    ((PortalFeature) CLFeatures.getInstance().getFeature("portal")).getDefaultPortals());
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setAuthor("Server");
        meta.setTitle("Address Book");
        meta.setDisplayName("§aPortal Book");
        meta.setLore(Arrays.asList("§2This book contains your portal names.",
                                   "§2Use it to select your destination in a Portal Lectern.",
                                   "§2Type §a/pbook [add|remove|select] <name>"));
        item.setItemMeta(meta);
        
        player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
        return null;
    }
    
    @Override
    public void help(CommandSender sender) {
    }
}
