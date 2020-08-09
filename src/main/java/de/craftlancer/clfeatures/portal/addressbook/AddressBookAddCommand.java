package de.craftlancer.clfeatures.portal.addressbook;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import de.craftlancer.core.command.SubCommand;

public class AddressBookAddCommand extends SubCommand {
    public AddressBookAddCommand(Plugin plugin) {
        super("clfeature.portal.book.add", plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return "§f[§4Craft§fCitizen] §eYou can't use this command.";
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!AddressBookUtils.isAddressBook(item))
            return "§f[§4Craft§fCitizen] §eYou must hold an address book in your hand. Use /kit book to get one.";
        if (args.length < 2)
            return "§f[§4Craft§fCitizen] §eYou must specify a name to add.";
        
        String name = args[1];
        
        if (name.length() > 20)
            return "§f[§4Craft§fCitizen] §eThe given name is too long.";
        
        List<String> addresses = AddressBookUtils.getAddresses(item);
        addresses.add(name);
        
        AddressBookUtils.writeBook(item, AddressBookUtils.getCurrentTarget(item).orElse(""), addresses);
        return "§f[§4Craft§fCitizen] §eAddress added.";
    }
    
    @Override
    public void help(CommandSender sender) {
    }
}
