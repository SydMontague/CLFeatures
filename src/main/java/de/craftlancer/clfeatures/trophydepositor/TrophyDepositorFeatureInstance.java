package de.craftlancer.clfeatures.trophydepositor;

import de.craftlancer.clfeatures.BlueprintFeatureInstance;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.structure.BlockStructure;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TrophyDepositorFeatureInstance extends BlueprintFeatureInstance {
    
    public static final String MOVE_METADATA = "trophyDepositor.move";
    private TrophyDepositorFeature manager;
    private String messagePrefix = "§f[§4Craft§fCitizen]§e ";
    private List<UUID> cooldown = new ArrayList<>();
    
    public TrophyDepositorFeatureInstance(TrophyDepositorFeature manager, UUID ownerId, BlockStructure blocks, Location location, String usedSchematic) {
        super(ownerId, blocks, location, usedSchematic);
        this.manager = manager;
    }
    
    public TrophyDepositorFeatureInstance(Map<String, Object> map) {
        super(map);
        
        this.manager = getManager();
    }
    
    @Override
    protected void tick() {
        World w = getInitialBlock().getWorld();
        
        if (!w.isChunkLoaded(getInitialBlock().getBlockX() >> 4, getInitialBlock().getBlockZ() >> 4))
            return;
        
        w.spawnParticle(Particle.ENCHANTMENT_TABLE, getInitialBlock().clone().add(0.5, 1.5, 0.5), 10);
    }
    
    @Override
    protected TrophyDepositorFeature getManager() {
        if (manager == null)
            manager = (TrophyDepositorFeature) CLFeatures.getInstance().getFeature("trophyDepositor");
        
        return manager;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteractMove(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (!event.hasBlock())
            return;
        
        if (!getStructure().containsBlock(event.getClickedBlock()))
            return;
        
        if (cooldown.contains(p.getUniqueId()))
            return;
        else {
            cooldown.add(p.getUniqueId());
            new LambdaRunnable(() -> cooldown.remove(p.getUniqueId())).runTaskLater(CLFeatures.getInstance(), 5);
        }
        
        if (p.hasMetadata(MOVE_METADATA)) {
            destroy();
            getManager().giveFeatureItem(p, this);
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "TrophyDepositor successfully moved back to your inventory.");
            p.removeMetadata(MOVE_METADATA, getManager().getPlugin());
            return;
        }
        
        if (!p.isSneaking() || event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;
        ItemStack item = p.getInventory().getItemInMainHand();
        
        double score = getManager().getBaseItemValue(item);
        if (score == 0) {
            p.sendMessage(messagePrefix + "The item you are holding is not a trophy.");
            return;
        }
        
        if (manager.getBoosts().size() > 0)
            p.sendMessage(messagePrefix + "§aThere are " + manager.getBoosts().size() + " active boosts that have been applied to your score!");
        
        double finalValue = getManager().deposit(p.getUniqueId(), item);
        p.sendMessage(messagePrefix + "§aItem deposited for " + String.format("%.2f", finalValue) + " points.");
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 1F);
        p.getInventory().setItemInMainHand(null);
        p.updateInventory();
    }
    
    // override destroy listener
    @Override
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInitialDestroy(BlockBreakEvent event) {
        if (!event.getBlock().getLocation().equals(getInitialBlock()))
            return;
        
        event.getPlayer().sendMessage(messagePrefix + "§4The TrophyDepositor cannot be destroyed! §eUse §2/trophydepositor move §einstead!");
        event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        //Stop the chance that a trophy is placed when clicking the dispenser
        if (getStructure().containsBlock(event.getBlockAgainst()))
            event.setCancelled(true);
    }
}
