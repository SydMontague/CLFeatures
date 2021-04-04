package de.craftlancer.clfeatures;

import de.craftlancer.core.CLCore;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPostPasteEvent;
import me.sizzlemcgrizzle.blueprints.util.SchematicUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public abstract class BlueprintFeature<T extends BlueprintFeatureInstance> extends Feature<T> {
    
    public BlueprintFeature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        super(plugin, config, limitKey);
    }
    
    public abstract boolean createInstance(Player creator, BlueprintPostPasteEvent event);
    
    @Override
    public void giveFeatureItem(Player player, T instance) {
        List<ItemStack> items = instance != null ? SchematicUtil.getBlueprint(instance.getUsedSchematic()) : Collections.emptyList();
        ItemStack item = items.isEmpty() ? CLCore.getInstance().getItemRegistry().getItem(getFeatureItem()).orElseGet(() -> new ItemStack(Material.AIR)) : items.get(0);
        
        if (item != null)
            player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
        
    }
}
