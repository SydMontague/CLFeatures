package de.craftlancer.clfeatures.portal.addressbook;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import de.craftlancer.core.command.SubCommand;

public class AddressBookSelectCommand extends SubCommand {
    public AddressBookSelectCommand(Plugin plugin) {
        super("clfeatures.portal.book.select", plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return "You can't use this command.";
        
        Player player = (Player) sender;
        
        ItemStack item;
        if (player.getOpenInventory().getType() == InventoryType.LECTERN)
            item = player.getOpenInventory().getItem(0);
        else
            item = player.getInventory().getItemInMainHand();
        
        if (!AddressBookUtils.isAddressBook(item))
            return "You must hold an address book in your hand. Use /kit book to get one.";
        if (args.length < 2)
            return "You must specify a name to select.";
        
        String name = args[1];
        
        if (name.length() > 20)
            return "The given name is too long.";
        
        AddressBookUtils.writeBook(item, name, AddressBookUtils.getAddresses(item));
        return "Address selected.";
    }
    
    @Override
    public void help(CommandSender sender) {
    }
}