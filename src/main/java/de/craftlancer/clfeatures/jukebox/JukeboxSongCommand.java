package de.craftlancer.clfeatures.jukebox;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.command.SubCommandHandler;
import org.bukkit.command.CommandSender;

public class JukeboxSongCommand extends SubCommandHandler {
    public JukeboxSongCommand(CLFeatures plugin, JukeboxFeature feature) {
        super("", plugin, false, 1);
        
        registerSubCommand("create", new JukeboxSongCreateCommand(plugin, feature));
        registerSubCommand("edit", new JukeboxSongEditCommand(plugin, feature));
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
