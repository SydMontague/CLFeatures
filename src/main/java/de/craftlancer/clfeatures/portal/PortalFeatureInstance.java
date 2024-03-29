package de.craftlancer.clfeatures.portal;

import de.craftlancer.clapi.clfeatures.portal.AbstractPortalFeatureInstance;
import de.craftlancer.clapi.clfeatures.portal.event.PortalTeleportEvent;
import de.craftlancer.clfeatures.BlueprintFeatureInstance;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.portal.addressbook.AddressBookUtils;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Lectern;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BoundingBox;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PortalFeatureInstance extends BlueprintFeatureInstance implements AbstractPortalFeatureInstance {
    
    // technical
    private PortalFeature manager;
    
    // persistent
    private String name = null;
    private long lastUsage = 0;
    
    // runtime
    private String currentTarget;
    private int ticksWithoutBook = 0;
    private long newBookDelay = 0;
    
    private BoundingBox box;
    private Location targetLocation;
    
    private boolean isValid = true;
    
    public PortalFeatureInstance(PortalFeature manager, Player owner, BlockStructure blocks, Block initialBlock, String usedSchematic) {
        super(owner.getUniqueId(), blocks, initialBlock.getLocation(), usedSchematic);
        
        this.manager = manager;
        this.lastUsage = Instant.now().getEpochSecond();
        
        this.box = calculateBoundingBox(blocks);
        if(this.box == null)
            this.isValid = false;
        else
            this.targetLocation = calculateTargetLocation(initialBlock, this.box);

        if(!this.isValid)
            CLFeatures.getInstance().getLogger().warning("Invalid portal detected: " + this.getName() + " " + getInitialBlock());
    }
    
    private static Location calculateTargetLocation(Block block, BoundingBox box) {
        BlockFace facing = block.getType() == Material.LECTERN ? ((Directional) block.getBlockData()).getFacing().getOppositeFace() : BlockFace.NORTH;
        return new Location(block.getWorld(), box.getCenterX(), box.getMinY(), box.getCenterZ()).setDirection(facing.getOppositeFace().getDirection());
    }
    
    private static BoundingBox calculateBoundingBox(BlockStructure blocks) {
        List<Location> airBlocks = blocks.getBlocks().stream().filter(a -> a.getBlock().getType().isAir()).toList();
        int minX = airBlocks.stream().map(Location::getBlockX).min(Integer::compare).orElseGet(() -> 0);
        int minY = airBlocks.stream().map(Location::getBlockY).min(Integer::compare).orElseGet(() -> 0);
        int minZ = airBlocks.stream().map(Location::getBlockZ).min(Integer::compare).orElseGet(() -> 0);
        int maxX = airBlocks.stream().map(Location::getBlockX).max(Integer::compare).orElseGet(() -> 0);
        int maxY = airBlocks.stream().map(Location::getBlockY).max(Integer::compare).orElseGet(() -> 0);
        int maxZ = airBlocks.stream().map(Location::getBlockZ).max(Integer::compare).orElseGet(() -> 0);
        
        if (minX == 0 && minY == 0 && minZ == 0 && maxX == 0 && maxY == 0 && maxZ == 0) {
            return null;
        }
        
        return new BoundingBox(minX, minY, minZ, maxX + 1D, maxY + 1D, maxZ + 1D);
    }
    
    @Override
    protected void interact(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (!p.hasMetadata(PortalFeature.RENAME_METADATA))
            return;
        
        if (!getOwnerId().equals(p.getUniqueId()))
            return;
        
        String newName = p.getMetadata(PortalFeature.RENAME_METADATA).get(0).asString();
        
        boolean isFirstName = getName() == null || getName().isEmpty();
        
        if (isFirstName || getManager().checkRenameCosts(p)) {
            setName(newName);
            
            if (!isFirstName)
                getManager().deductRenameCosts(p);
            else {
                // give first time books
                List<String> addressList = new ArrayList<>();
                addressList.add(newName);
                addressList.addAll(getManager().getDefaultPortals());
                
                ItemStack homeBook = AddressBookUtils.writeBook(new ItemStack(Material.WRITTEN_BOOK), getManager().getDefaultPortal(), addressList);
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
        } else
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't afford to rename this portal. You need a Lesser Fragment.");
        
        p.removeMetadata(PortalFeature.RENAME_METADATA, getManager().getPlugin());
    }
    
    private Optional<String> getCurrentTarget(ItemStack item) {
        if (item == null || item.getType() != Material.WRITTEN_BOOK)
            return Optional.empty();
        
        Optional<String> bookTarget = AddressBookUtils.getCurrentTarget(item);
        if (bookTarget.isPresent())
            return bookTarget;
        
        BookMeta meta = ((BookMeta) item.getItemMeta());
        
        if (meta.getPageCount() == 0)
            return Optional.empty();
        
        String[] lines = meta.getPage(1).split("\n");
        return lines.length > 0 ? Optional.ofNullable(lines[0].trim()) : Optional.empty();
    }
    
    @Override
    protected void tick() {
        /* Disable inactivity check
        if (Instant.now().getEpochSecond() - lastUsage > getManager().getInactivityTimeout()) {
            destroy();
            getManager().getPlugin().getLogger().info(() -> String.format("Portal \"%s\" timed out and got removed.", name));
            getManager().getPlugin().getLogger().info("Location: " + getInitialBlock() + " | " + getOwnerId());
            return;
        }
        */
        
        World w = getInitialBlock().getWorld();
        
        if (++ticksWithoutBook > getManager().getBooklessTicks())
            currentTarget = null;
        
        if (!w.isChunkLoaded(getInitialBlock().getBlockX() >> 4, getInitialBlock().getBlockZ() >> 4))
            return;
        
        if (getInitialBlock().getBlock().getType() != Material.LECTERN) {
            getManager().getPlugin().getLogger().warning(() -> String.format("Portal \"%s\" is missing it's Lectern, did it get removed somehow?", name));
            getManager().getPlugin().getLogger().warning("Location: " + getInitialBlock() + " | " + getOwnerId());
            getManager().getPlugin().getLogger().warning("Removing the portal to prevent further errors.");
            new LambdaRunnable(this::destroy).runTask(getManager().getPlugin());
            return;
        }
        
        // don't tick portals with a player more than 32 blocks away
        if (Bukkit.getOnlinePlayers().stream().noneMatch(a -> a.getWorld().equals(w) && a.getLocation().distanceSquared(getInitialBlock()) < 1024))
            return;
        
        Lectern l = (Lectern) getInitialBlock().getBlock().getState();
        ItemStack item = l.getInventory().getItem(0);
        
        // set target null if a book is put into the portal shortly after one got taken out
        if (--newBookDelay > 0 && item != null)
            currentTarget = null;
        else if (item != null && item.getType() == Material.WRITTEN_BOOK) {
            Optional<String> newTarget = getCurrentTarget(item);
            
            newTarget.filter(a -> !a.equalsIgnoreCase(name) && !a.equalsIgnoreCase(currentTarget)).ifPresent(a -> w.playSound(getInitialBlock(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1));
            
            currentTarget = newTarget.orElse(null);
            ticksWithoutBook = 0;
        }
        
        PortalFeatureInstance target = getManager().getPortal(currentTarget);
        
        if (target == null || this == target || !target.isValid())
            return;
        
        int volume = (int) box.getVolume();
        for (int i = 0; i < volume; i++) {
            w.spawnParticle(Particle.SPELL_WITCH, box.getCenterX(), box.getCenterY(), box.getCenterZ(), 3, box.getWidthX() / 4, box.getHeight() / 4, box.getWidthZ() / 4);
            w.spawnParticle(Particle.PORTAL, box.getCenterX(), box.getCenterY(), box.getCenterZ(), 3, box.getWidthX() / 4, box.getHeight() / 4, box.getWidthZ() / 4);
        }
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasMetadata(PortalFeature.LOOP_METADATA) || !box.contains(p.getLocation().toVector()))
                continue;
            
            PortalTeleportEvent event = new PortalTeleportEvent(p, this, target);
            Bukkit.getPluginManager().callEvent(event);
            
            if (!event.isCancelled()) {
                p.teleport(target.getTargetLocation(), TeleportCause.PLUGIN);
                p.setMetadata(PortalFeature.LOOP_METADATA, new FixedMetadataValue(getManager().getPlugin(), target.getTargetLocation()));
            }
        }
    }
    
    @Override
    public long getNewBookDelay() {
        return newBookDelay;
    }
    
    @Override
    public void setNewBookDelay(long newBookDelay) {
        this.newBookDelay = newBookDelay;
    }
    
    @Override
    public boolean isValid() {
        return isValid;
    }
    
    private Location getTargetLocation() {
        return targetLocation;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        manager.updatedName(this, this.name, name);
        this.name = name;
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
        
        this.box = (BoundingBox) map.getOrDefault("boundingBox", calculateBoundingBox(getStructure()));
        if(this.box == null)
            this.isValid = false;
        else
            this.targetLocation = (Location) map.getOrDefault("targetLocation", calculateTargetLocation(getInitialBlock().getBlock(), box));
        
        if(!this.isValid)
            CLFeatures.getInstance().getLogger().warning("Invalid portal detected: " + this.getName() + " " + getInitialBlock());
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("name", name);
        map.put("lastUsed", lastUsage);
        map.put("boundingBox", box);
        map.put("targetLocation", targetLocation);
        
        return map;
    }
}
