package de.craftlancer.clfeatures.portal.addressbook;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.command.CommandHandler;

public class AddressBookCommandHandler extends CommandHandler {
    
    public AddressBookCommandHandler(CLFeatures plugin) {
        super(plugin);
        
        registerSubCommand("add", new AddressBookAddCommand(getPlugin()));
        registerSubCommand("select", new AddressBookSelectCommand(getPlugin()));
        registerSubCommand("remove", new AddressBookRemoveCommand(getPlugin()));
        registerSubCommand("get", new AddressBookGetCommand(getPlugin()));
    }
}
