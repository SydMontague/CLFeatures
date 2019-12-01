package de.craftlancer.clfeatures;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.craftlancer.core.command.CommandHandler;

public abstract class Feature {
    
    private CLFeatures plugin;
    private int defaultLimit;
    private Map<String, Integer> limitMap = new HashMap<>();
    
    public Feature(CLFeatures plugin, ConfigurationSection config) {
        this.plugin = plugin;
        
        defaultLimit = config.getInt("defaultLimit", -1);
        ConfigurationSection limitConfig = config.getConfigurationSection("limits");
        limitConfig.getKeys(false).forEach(a -> limitMap.put(a, limitConfig.getInt(a)));
    }
    
    public int getLimit(Player player) {
        return limitMap.entrySet().stream().filter(a -> plugin.getPermissions().playerInGroup(player, a.getKey())).map(Entry::getValue).max(Integer::compare)
                       .orElseGet(() -> defaultLimit);
    }
    
    public CLFeatures getPlugin() {
        return plugin;
    }
    
    public abstract void giveFeatureItem(Player player);
    
    public abstract boolean isFeatureItem(ItemStack item);
    
    public abstract boolean checkFeatureLimit(Player player);
    
    public abstract boolean checkEnvironment(Block initialBlock);
    
    public abstract boolean createInstance(Player creator, Block initialBlock);
    
    public abstract void save();
    
    public abstract CommandHandler getCommandHandler();
    
    public abstract void remove(FeatureInstance instance);
}
