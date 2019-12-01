package de.craftlancer.clfeatures.portal;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Lectern;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.structure.BlockStructure;

public class PortalFeatureInstance extends FeatureInstance implements ConfigurationSerializable {
    private static final String RENAME_METADATA = "portalRename";
    
    // technical
    private PortalFeature manager;
    
    // persistent
    private String name = null;
    private long lastUsage = 0;
    
    // runtime
    private String currentTarget;
    private int ticksWithoutBook = 0;
    
    public PortalFeatureInstance(PortalFeature manager, Player owner, BlockStructure blocks, Block initialBlock) {
        super(owner.getUniqueId(), blocks, initialBlock.getLocation());
        
        this.manager = manager;
        this.lastUsage = Instant.now().getEpochSecond();
    }
    
    @Override
    protected void tick() {
        if (Instant.now().getEpochSecond() - lastUsage > getManager().getInactivityTimeout()) {
            destroy();
            return;
        }
        
        Lectern l = (Lectern) getInitialBlock().getBlock().getState();
        ItemStack item = l.getInventory().getItem(0);
        
        if (item != null && item.getType() == Material.WRITTEN_BOOK) {
            currentTarget = ((BookMeta) item.getItemMeta()).getPage(1);
            ticksWithoutBook = 0;
        }
        
        if (++ticksWithoutBook > getManager().getBooklessTicks())
            currentTarget = null;
        
        PortalFeatureInstance target = getManager().getPortal(currentTarget);
        
        if (target == null || this == target)
            return;
        
        World w = getInitialBlock().getWorld();
        getStructure().forEach(a -> {
            if(a.getBlock().getType().isAir()) {
                w.spawnParticle(Particle.SPELL_WITCH, a.clone().add(Math.random(), Math.random(), Math.random()), 3);
                w.spawnParticle(Particle.PORTAL, a.clone().add(Math.random(), Math.random(), Math.random()), 3);
            }
        });
        
        //getInitialBlock().getWorld().playEffect(location, effect, data);
        
        Bukkit.getOnlinePlayers().stream().filter(a -> a.getPortalCooldown() == 0)
              .filter(a -> !a.getLocation().getBlock().getLocation().equals(getInitialBlock()))
              .filter(a -> getStructure().containsBlock(a.getLocation().getBlock())).forEach(a -> {
                  a.teleport(target.getTargetLocation(), TeleportCause.PLUGIN);
                  a.setPortalCooldown(manager.getPortalCooldown());
              });
    }
    
    private Location getTargetLocation() {
        BlockFace facing = ((Directional) getInitialBlock().getBlock().getBlockData()).getFacing().getOppositeFace();
        return getInitialBlock().clone().add(0.5 + 1.5 * facing.getModZ(), 0, 0.5 - 1.5 * facing.getModX()).add(facing.getDirection())
                                .setDirection(facing.getOppositeFace().getDirection());
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (!event.hasBlock())
            return;
        
        if (!p.hasMetadata(RENAME_METADATA))
            return;
        
        if (!getStructure().containsBlock(event.getClickedBlock()))
            return;
        
        // TODO admin permission
        if (!getOwnerId().equals(p.getUniqueId()))
            return;
        
        String newName = p.getMetadata(RENAME_METADATA).get(0).asString();
        
        boolean isFirstName = name == null || name.isEmpty();
        
        if (isFirstName || getManager().checkRenameCosts(p)) {
            setName(newName);
            
            if (!isFirstName)
                getManager().deductRenameCosts(p);
            
            p.sendMessage(String.format("Portal successfully renamed to %s.", newName));
        }
        else
            p.sendMessage("You can't afford to rename this portal.");
        
        p.removeMetadata(RENAME_METADATA, CLFeatures.getInstance());
    }
    
    @Override
    protected PortalFeature getManager() {
        if (manager == null)
            manager = (PortalFeature) CLFeatures.getInstance().getFeature("portal");
        
        return manager;
    }
    
    public PortalFeatureInstance(Map<String, Object> map) {
        super(map);
        
        this.name = (String) map.getOrDefault("name", "");
        this.lastUsage = ((Number) map.get("lastUsed")).longValue();
    }
    
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        
        map.put("owner", getOwnerId().toString());
        map.put("name", name);
        map.put("lecternLoc", getInitialBlock());
        map.put("structure", getStructure());
        map.put("lastUsed", lastUsage);
        
        return map;
    }
}
