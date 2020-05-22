package de.craftlancer.clfeatures.portal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Lectern;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BoundingBox;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.portal.addressbook.AddressBookUtils;
import de.craftlancer.clfeatures.portal.event.PortalTeleportEvent;
import de.craftlancer.core.structure.BlockStructure;
import net.md_5.bungee.api.ChatColor;

// TODO transfer portal ownership
public class PortalFeatureInstance extends FeatureInstance {
    public static final String LOOP_METADATA = "portalLoop";
    public static final String RENAME_METADATA = "portalRename";
    public static final String MOVE_METADATA = "portalMove";
    
    // technical
    private PortalFeature manager;
    
    // persistent
    private String name = null;
    private long lastUsage = 0;
    
    // runtime
    private String currentTarget;
    private int ticksWithoutBook = 0;
    
    private List<Location> airBlocks;
    private BoundingBox box;
    private Location targetLocation;
    
    public PortalFeatureInstance(PortalFeature manager, Player owner, BlockStructure blocks, Block initialBlock) {
        super(owner.getUniqueId(), blocks, initialBlock.getLocation());
        
        this.manager = manager;
        this.lastUsage = Instant.now().getEpochSecond();
        
        calcInitialStuff();
    }
    
    private void calcInitialStuff() {
        airBlocks = getStructure().getBlocks().stream().filter(a -> a.getBlock().getType().isAir()).collect(Collectors.toList());
        int minX = airBlocks.stream().map(Location::getBlockX).min(Integer::compare).orElseGet(() -> 0);
        int minY = airBlocks.stream().map(Location::getBlockY).min(Integer::compare).orElseGet(() -> 0);
        int minZ = airBlocks.stream().map(Location::getBlockZ).min(Integer::compare).orElseGet(() -> 0);
        int maxX = airBlocks.stream().map(Location::getBlockX).max(Integer::compare).orElseGet(() -> 0);
        int maxY = airBlocks.stream().map(Location::getBlockY).max(Integer::compare).orElseGet(() -> 0);
        int maxZ = airBlocks.stream().map(Location::getBlockZ).max(Integer::compare).orElseGet(() -> 0);
        
        if (minX == 0 && minY == 0 && minZ == 0 && maxX == 0 && maxY == 0 && maxZ == 0)
            getManager().getPlugin().getLogger().warning("Invalid portal detected: " + this.getName() + " " + getInitialBlock());
        
        box = new BoundingBox(minX, minY, minZ, maxX + 1D, maxY + 1D, maxZ + 1D);
        BlockFace facing = ((Directional) getInitialBlock().getBlock().getBlockData()).getFacing().getOppositeFace();
        targetLocation = new Location(getInitialBlock().getWorld(), box.getCenterX(), box.getMinY(), box.getCenterZ()).setDirection(facing.getOppositeFace().getDirection());
    }
    
    private String getCurrentTarget(ItemStack item) {
        if (item == null || item.getType() != Material.WRITTEN_BOOK)
            return null;
        
        if (AddressBookUtils.isAddressBook(item))
            return AddressBookUtils.getCurrentTarget(item);
        
        BookMeta meta = ((BookMeta) item.getItemMeta());
        
        if (meta.getPageCount() == 0)
            return null;
        
        String[] lines = meta.getPage(1).split("\n");
        return lines.length > 0 ? lines[0].trim() : null;
    }
    
    @Override
    protected void tick() {
        if (Instant.now().getEpochSecond() - lastUsage > getManager().getInactivityTimeout()) {
            destroy();
            getManager().getPlugin().getLogger().info(() -> String.format("Portal \"%s\" timed out and got removed.", name));
            getManager().getPlugin().getLogger().info("Location: " + getInitialBlock() + " | " + getOwnerId());
            return;
        }
        
        World w = getInitialBlock().getWorld();

        if (++ticksWithoutBook > getManager().getBooklessTicks())
            currentTarget = null;
        
        if (!w.isChunkLoaded(getInitialBlock().getBlockX() >> 4, getInitialBlock().getBlockZ() >> 4))
            return;
        
        if (!(getInitialBlock().getBlock().getState() instanceof Lectern)) {
            getManager().getPlugin().getLogger().warning(() -> String.format("Portal \"%s\" is missing it's Lectern, did it get removed somehow?", name));
            getManager().getPlugin().getLogger().warning("Location: " + getInitialBlock() + " | " + getOwnerId());
            getManager().getPlugin().getLogger().warning("Removing the portal to prevent further errors.");
            destroy();
            return;
        }
        
        // don't tick portals with a player more than 32 blocks away
        if(Bukkit.getOnlinePlayers().stream().noneMatch(a -> a.getWorld().equals(w) && a.getLocation().distanceSquared(getInitialBlock()) < 1024))
            return; 
        
        Lectern l = (Lectern) getInitialBlock().getBlock().getState();
        ItemStack item = l.getInventory().getItem(0);
        
        if (item != null && item.getType() == Material.WRITTEN_BOOK) {
            currentTarget = getCurrentTarget(item);
            ticksWithoutBook = 0;
        }
        
        PortalFeatureInstance target = getManager().getPortal(currentTarget);
        
        if (target == null || this == target)
            return;
        
        airBlocks.forEach(a -> {
            w.spawnParticle(Particle.SPELL_WITCH, a.clone().add(Math.random(), Math.random(), Math.random()), 3);
            w.spawnParticle(Particle.PORTAL, a.clone().add(Math.random(), Math.random(), Math.random()), 3);
        });
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getPortalCooldown() != 0 || p.hasMetadata(LOOP_METADATA) || !box.contains(p.getLocation().toVector()))
                continue;
            
            PortalTeleportEvent event = new PortalTeleportEvent(p, this, target);
            Bukkit.getPluginManager().callEvent(event);
            
            if (!event.isCancelled()) {
                p.teleport(target.getTargetLocation(), TeleportCause.PLUGIN);
                p.setPortalCooldown(manager.getPortalCooldown());
                p.setMetadata(LOOP_METADATA, new FixedMetadataValue(getManager().getPlugin(), target.getTargetLocation()));
            }
        }
    }
    
    private Location getTargetLocation() {
        return targetLocation;
    }
    
    public String getName() {
        return name == null ? "<no name>" : name;
    }
    
    public void setName(String name) {
        manager.updatedName(this, this.name, name);
        this.name = name;
    }
    
    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        event.getPlayer().removeMetadata(LOOP_METADATA, getManager().getPlugin());
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteractMove(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (!event.hasBlock() || !p.hasMetadata(MOVE_METADATA))
            return;
        
        if (!getStructure().containsBlock(event.getClickedBlock()))
            return;
        
        if (!getOwnerId().equals(p.getUniqueId()))
            return;
        
        if (getName() == null || getManager().checkMoveCost(p)) {
            if(name != null)
                getManager().deductMoveCost(p);
            destroy();
            manager.giveFeatureItem(p);
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "Portal successfully moved back to your inventory.");
        }
        else
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't afford to move this portal. You need 3 Lesser Fragments.");
        
        p.removeMetadata(MOVE_METADATA, getManager().getPlugin());
        p.removeMetadata(RENAME_METADATA, getManager().getPlugin());
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
            else {
                // give first time books
                List<String> addressList = new ArrayList<>();
                addressList.add(newName);
                addressList.addAll(manager.getDefaultPortals());
                
                ItemStack homeBook = AddressBookUtils.writeBook(new ItemStack(Material.WRITTEN_BOOK), manager.getDefaultPortal(), addressList);
                BookMeta homeMeta = (BookMeta) homeBook.getItemMeta();
                homeMeta.setDisplayName(ChatColor.GREEN + p.getName() + "'s Portal Book");
                homeMeta.setTitle("Address Book");
                homeMeta.setAuthor("Server");
                homeMeta.setLore(Arrays.asList(ChatColor.DARK_GREEN + "This book contains your portal names.",
                                               ChatColor.DARK_GREEN + "Use it to select your destination in a Portal Lectern.",
                                               ChatColor.DARK_GREEN + "Type " + ChatColor.GREEN + "/pbook [add|remove|select] <name>"));
                homeBook.setItemMeta(homeMeta);
                
                p.getInventory().addItem(homeBook).forEach((a, b) -> p.getWorld().dropItem(p.getLocation(), b));
            }
            
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + String.format("Portal successfully renamed to %s.", newName));
        }
        else
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't afford to rename this portal. You need a Lesser Fragment.");
        
        p.removeMetadata(RENAME_METADATA, getManager().getPlugin());
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
        calcInitialStuff();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("name", name);
        map.put("lastUsed", lastUsage);
        
        return map;
    }
}
