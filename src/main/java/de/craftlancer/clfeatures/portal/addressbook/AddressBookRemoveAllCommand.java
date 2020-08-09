package de.craftlancer.clfeatures.portal.addressbook;

import java.util.Collections;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import de.craftlancer.core.command.SubCommand;

public class AddressBookRemoveAllCommand extends SubCommand {
    public AddressBookRemoveAllCommand(Plugin plugin) {
        super("clfeature.portal.book.remove", plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkSender(sender))
            return "§f[§4Craft§fCitizen] §eYou can't use this command.";
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!AddressBookUtils.isAddressBook(item))
            return "§f[§4Craft§fCitizen] §eYou must hold an address book in your hand. Use /kit book to get one.";
        
        AddressBookUtils.writeBook(item, AddressBookUtils.getCurrentTarget(item).orElse(""), Collections.emptyList());
        return "§f[§4Craft§fCitizen] §eAddresses removed.";
    }
    
    @Override
    public void help(CommandSender sender) {
    }
}
