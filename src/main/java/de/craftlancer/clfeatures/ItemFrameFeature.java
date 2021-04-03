package de.craftlancer.clfeatures;

import de.craftlancer.core.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public abstract class ItemFrameFeature<T extends ItemFrameFeatureInstance> extends ManualPlacementFeature<T> {
    
    public ItemFrameFeature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        super(plugin, config, limitKey);
    }
    
    @Override
    public abstract List<Block> checkEnvironment(Block initialBlock);
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof ItemFrameFeatureInstance) {
            Entity entity = Bukkit.getEntity(((ItemFrameFeatureInstance) instance).getItemFrame());
            
            if (entity != null)
                entity.remove();
        }
    }
    
    @Override
    public final boolean createInstance(Player creator, Block initialBlock, ItemStack hand) {
        initialBlock.setType(Material.AIR);
        
        ItemFrame item = initialBlock.getWorld().spawn(initialBlock.getLocation(), ItemFrame.class);
        item.setFacingDirection(BlockFace.UP, true);
        item.setItem(hand.clone());
        item.setVisible(false);
        item.setRotation(Utils.getRotationFromYaw(creator.getLocation().getYaw()));
        
        initialBlock.setType(Material.BARRIER);
        
        onInstanceCreate(creator, initialBlock, hand, item.getUniqueId());
        return true;
    }
    
    public abstract void onInstanceCreate(Player creator, Block initialBlock, ItemStack hand, UUID itemFrame);
    
    @Override
    public void giveFeatureItem(Player player, T feature) {
        ItemStack item = feature.getUsedItem();
        
        if (item != null)
            player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
    }
}
