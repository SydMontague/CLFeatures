package de.craftlancer.clfeatures;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import de.craftlancer.core.CLCore;
import de.craftlancer.core.command.CommandHandler;

public abstract class Feature {
    
    private final CLFeatures plugin;
    private int defaultLimit;
    private int maxLimit;
    private Map<String, Integer> limitMap = new HashMap<>();
    
    private final NamespacedKey limitKey;
    private String featureItem;
    private final String limitToken;
    
    public Feature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        this.plugin = plugin;
        this.limitKey = limitKey;
        this.limitToken = config.getString("limitToken", "");
        

        featureItem = config.getString("featureItem");
        
        defaultLimit = config.getInt("defaultLimit", -1);
        maxLimit = config.getInt("maxLimit", -1);
        ConfigurationSection limitConfig = config.getConfigurationSection("limits");
        if(limitConfig != null)
            limitConfig.getKeys(false).forEach(a -> limitMap.put(a, limitConfig.getInt(a)));
    }
    
    public int getLimit(Player player) {
        int groupLimit = limitMap.entrySet().stream().filter(a -> plugin.getPermissions().playerInGroup(player, a.getKey())).map(Entry::getValue).max(Integer::compare)
                .orElseGet(() -> defaultLimit);
        int individualLimit = player.getPersistentDataContainer().getOrDefault(limitKey, PersistentDataType.INTEGER, 0).intValue();
        
        return groupLimit < 0 ? -1 : groupLimit + individualLimit;
    }
    
    public void addFeatureLimit(Player player, int amount) {
        int individualLimit = player.getPersistentDataContainer().getOrDefault(limitKey, PersistentDataType.INTEGER, 0).intValue();
        player.getPersistentDataContainer().set(limitKey, PersistentDataType.INTEGER, individualLimit + amount);
    }

    public boolean isLimitToken(@Nonnull ItemStack item) {
        if(!CLCore.getInstance().getItemRegistry().hasItem(limitToken) || item.getType().isAir())
            return false;
        
        return item.isSimilar(CLCore.getInstance().getItemRegistry().getItem(limitToken));
    }

    public int getMaxLimit() {
        return maxLimit;
    }
    
    public String getFeatureItem() {
        return featureItem;
    }
    
    public CLFeatures getPlugin() {
        return plugin;
    }
    
    public void giveFeatureItem(Player player) {
        ItemStack item = CLCore.getInstance().getItemRegistry().getItem(getFeatureItem());
        
        if (item != null)
            player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
    }
    
    public abstract boolean isFeatureItem(ItemStack item);
    
    public abstract boolean checkFeatureLimit(Player player);
    
    public abstract Collection<Block> checkEnvironment(Block initialBlock);
    
    public abstract boolean createInstance(Player creator, Block initialBlock);

    public abstract boolean createInstance(Player creator, Block initialBlock, List<Location> blocks);
    
    public abstract void save();
    
    public abstract CommandHandler getCommandHandler();
    
    public abstract void remove(FeatureInstance instance);

    @Nonnull
    protected abstract String getName();
}
