package de.craftlancer.clfeatures.portal.addressbook;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import de.craftlancer.core.command.SubCommand;

public class AddressBookRemoveCommand extends SubCommand {
    public AddressBookRemoveCommand(Plugin plugin) {
        super("clfeatures.portal.book.remove", plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return "You can't use this command.";
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!AddressBookUtils.isAddressBook(item))
            return "You must hold an address book in your hand. Use /kit book to get one.";
        if (args.length < 2)
            return "You must specify a name to add.";
        
        String name = args[1];
        
        if (name.length() > 20)
            return "The given name is too long.";
        
        List<String> addresses = AddressBookUtils.getAddresses(item);
        
        if (addresses.removeIf(a -> a.equalsIgnoreCase(name))) {
            AddressBookUtils.writeBook(item, AddressBookUtils.getCurrentTarget(item), addresses);
            return "Address removed.";
        }
        else
            return "Address not found in book.";
    }
    
    @Override
    public void help(CommandSender sender) {
    }
}
