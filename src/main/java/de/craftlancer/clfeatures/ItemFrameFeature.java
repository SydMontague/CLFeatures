package de.craftlancer.clfeatures;

import de.craftlancer.core.LambdaRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public abstract class ItemFrameFeature<T extends ItemFrameFeatureInstance> extends Feature<T> {
    
    public ItemFrameFeature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        super(plugin, config, limitKey);
    }
    
    @Override
    public boolean isFeatureItem(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().getKeys().stream()
                .anyMatch(k -> k.getKey().equals(getPlugin().getFeatureItemKey().getKey())
                        && item.getItemMeta().getPersistentDataContainer().get(k, PersistentDataType.STRING).equals("jukebox"));
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
        List<Block> environment = checkEnvironment(initialBlock);
        
        if (checkEnvironment(initialBlock).size() > 0) {
            environment.forEach(b -> {
                creator.sendBlockChange(b.getLocation(), Material.RED_CONCRETE.createBlockData());
                new LambdaRunnable(() -> creator.sendBlockChange(b.getLocation(), b.getBlockData())).runTaskLater(CLFeatures.getInstance(), 100);
            });
            return false;
        }
        
        initialBlock.setType(Material.AIR);
        
        ItemFrame item = initialBlock.getWorld().spawn(initialBlock.getLocation(), ItemFrame.class);
        item.setFacingDirection(BlockFace.UP, true);
        item.setItem(hand.clone());
        item.setVisible(false);
        item.setRotation(getRotation(creator));
        
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
    
    @Override
    public boolean createInstance(Player creator, Block initialBlock, List blocks, String schematic) {
        return true;
    }
    
    public Rotation getRotation(Player player) {
        double rotation = (player.getLocation().getYaw() + 180) % 360.0F;
        if (rotation < 0.0D) {
            rotation += 360.0D;
        }
        if ((0.0D <= rotation) && (rotation < 22.5D)) {
            return Rotation.NONE;
        }
        if ((22.5D <= rotation) && (rotation < 67.5D)) {
            return Rotation.CLOCKWISE_45;
        }
        if ((67.5D <= rotation) && (rotation < 112.5D)) {
            return Rotation.CLOCKWISE;
        }
        if ((112.5D <= rotation) && (rotation < 157.5D)) {
            return Rotation.CLOCKWISE_135;
        }
        if ((157.5D <= rotation) && (rotation < 202.5D)) {
            return Rotation.FLIPPED;
        }
        if ((202.5D <= rotation) && (rotation < 247.5D)) {
            return Rotation.FLIPPED_45;
        }
        if ((247.5D <= rotation) && (rotation < 292.5D)) {
            return Rotation.COUNTER_CLOCKWISE;
        }
        if ((292.5D <= rotation) && (rotation < 337.5D)) {
            return Rotation.COUNTER_CLOCKWISE_45;
        }
        if ((337.5D <= rotation) && (rotation < 360.0D)) {
            return Rotation.NONE;
        }
        return Rotation.NONE;
    }
}
