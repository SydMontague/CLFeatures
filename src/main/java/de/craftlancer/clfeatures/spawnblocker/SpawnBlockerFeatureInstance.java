package de.craftlancer.clfeatures.spawnblocker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.Utils;
import de.craftlancer.core.gui.GUIInventory;
import de.craftlancer.core.structure.BlockStructure;

public class SpawnBlockerFeatureInstance extends FeatureInstance {
    
    public static final String MOVE_METADATA = "spawnBlockerMove";
    private static final int GRID_SIZE = 5;
    
    private SpawnBlockerFeature manager;
    
    private boolean[] enabledChunks = new boolean[GRID_SIZE * GRID_SIZE];
    private Map<SpawnBlockGroupSlot, Boolean> enabledGroups;
    
    private GUIInventory inventory;
    
    private int centerChunkX;
    private int centerChunkZ;
    
    private Location interactLocation;
    
    public SpawnBlockerFeatureInstance(SpawnBlockerFeature manager, UUID ownerId, BlockStructure blocks, Location location, String usedSchematic) {
        super(ownerId, blocks, location, usedSchematic);
        this.manager = manager;
        
        centerChunkX = location.getBlockX() >> 4;
        centerChunkZ = location.getBlockZ() >> 4;
        interactLocation = location.clone().add(0, 1, 0);
        
        enabledGroups = manager.getBlockGroups().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, a -> a.getValue().getDefaultState()));
    }
    
    @SuppressWarnings("unchecked")
    public SpawnBlockerFeatureInstance(Map<String, Object> map) {
        super(map);
        centerChunkX = getInitialBlock().getBlockX() >> 4;
        centerChunkZ = getInitialBlock().getBlockZ() >> 4;
        interactLocation = getInitialBlock().clone().add(0, 1, 0);
        
        enabledGroups = ((Map<String, Boolean>) map.get("enabledGroups")).entrySet().stream()
                                                                         .collect(Collectors.toMap(a -> SpawnBlockGroupSlot.valueOf(a.getKey()),
                                                                                                   Map.Entry::getValue));
        
        List<Boolean> list = (List<Boolean>) map.get("enabledChunks");
        for (int i = 0; i < list.size(); i++)
            enabledChunks[i] = list.get(i);
    }
    
    private void setupInventory() {
        inventory = new GUIInventory(CLFeatures.getInstance(), "SpawnBlocker");
        inventory.fill(Utils.buildItemStack(Material.BLACK_STAINED_GLASS_PANE, ChatColor.BLACK + "", Collections.emptyList()));
        
        for (int x = 0; x < 5; x++)
            for (int z = 0; z < 5; z++) {
                int slotId = 9 + z * 9 + x;
                int chunkX = centerChunkX + x - 2;
                int chunkZ = centerChunkZ + z - 2;
                int arrayIndex = z * 5 + x;
                
                List<String> lore = new ArrayList<>();
                lore.add("Click to toggle");
                lore.add(String.format("Chunk %s, %s", chunkX, chunkZ));
                lore.add(String.format("%s %s to %s %s", chunkX * 16, chunkZ * 16, chunkX * 16 + 15, chunkZ * 16 + 15));
                
                ItemStack disabledItem = Utils.buildItemStack(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Spawns disabled", lore);
                ItemStack enabledItem = Utils.buildItemStack(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Spawns enabled", lore);
                
                inventory.setItem(slotId, enabledChunks[arrayIndex] ? enabledItem : disabledItem);
                inventory.setClickAction(slotId, () -> {
                    enabledChunks[arrayIndex] = !enabledChunks[arrayIndex];
                    inventory.setItem(slotId, enabledChunks[arrayIndex] ? enabledItem : disabledItem);
                });
            }
        
        ItemStack onItem = Utils.buildItemStack(Material.REDSTONE_TORCH, ChatColor.RED + "§lGroup spawns blocked", Arrays.asList(ChatColor.GREEN + "Click to toggle."));
        ItemStack offItem = Utils.buildItemStack(Material.LEVER, ChatColor.DARK_GREEN + "§lGroup spawns enabled", Arrays.asList(ChatColor.GREEN + "Click to toggle."));
        
        getManager().getBlockGroups().forEach((a, b) -> {
            inventory.setItem(15 + a.ordinal() * 9, b.getItem());
            inventory.setItem(16 + a.ordinal() * 9, enabledGroups.getOrDefault(a, b.getDefaultState()).booleanValue() ? onItem : offItem);
            inventory.setClickAction(16 + a.ordinal() * 9, () -> {
                enabledGroups.compute(a, (c, d) -> d == null ? !b.getDefaultState() : !d.booleanValue());
                inventory.setItem(16 + a.ordinal() * 9, enabledGroups.getOrDefault(a, b.getDefaultState()).booleanValue() ? onItem : offItem);
            });
        });
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("enabledGroups", enabledGroups.entrySet().stream().collect(Collectors.toMap(a -> a.getKey().name(), Map.Entry::getValue)));
        map.put("enabledChunks", enabledChunks);
        
        return map;
    }
    
    @Override
    protected void tick() {
        if (inventory == null)
            setupInventory();
        
        if (isActive())
            spawnParticles();
        
    }
    
    private boolean isActive() {
        if (enabledGroups.values().stream().noneMatch(Boolean::booleanValue))
            return false;
        
        for (boolean b : enabledChunks)
            if (!b)
                return true;
            
        return false;
    }
    
    private void spawnParticles() {
        if (!Utils.isChunkLoaded(interactLocation))
            return;
        Location centerSensor = interactLocation.clone().add(0.5, 0, 0.5);
        Particle.DustOptions particle = new Particle.DustOptions(Color.WHITE, 1F);
        for (double i = interactLocation.getY(); i < interactLocation.getY() + 1; i += 0.05) {
            centerSensor.setY(i);
            centerSensor.getWorld().spawnParticle(Particle.REDSTONE, centerSensor, 1, particle);
        }
    }
    
    @Override
    protected SpawnBlockerFeature getManager() {
        if (manager == null)
            manager = (SpawnBlockerFeature) CLFeatures.getInstance().getFeature("spawnBlocker");
        
        return manager;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasBlock() || event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        Block block = event.getClickedBlock();
        
        if (!block.getLocation().equals(interactLocation))
            return;
        
        Player player = event.getPlayer();
        
        player.openInventory(inventory.getInventory());
        event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        int chunkX = (event.getLocation().getBlockX() >> 4) - centerChunkX;
        int chunkZ = (event.getLocation().getBlockZ() >> 4) - centerChunkZ;
        
        if (!event.getEntityType().isAlive())
            return;
        
        // TODO only enable specified reasons
        switch (event.getSpawnReason()) {
            case CUSTOM:
            case BEEHIVE:
            case BREEDING:
            case BUILD_IRONGOLEM:
            case BUILD_SNOWMAN:
            case BUILD_WITHER:
            case DISPENSE_EGG:
            case DROWNED:
            case EGG:
            case INFECTION:
            case LIGHTNING:
            case SILVERFISH_BLOCK:
            case SHOULDER_ENTITY:
            case SPAWNER_EGG:
                return;
            default:
                break;
        }
        
        if (Math.abs(chunkX) > 2 || Math.abs(chunkZ) > 2)
            return;
        
        if (enabledChunks[(chunkZ + 2) * GRID_SIZE + (chunkX + 2)])
            return;
        
        if (isEntityTypeEnabled(event.getEntityType()))
            event.setCancelled(true);
    }
    
    private boolean isEntityTypeEnabled(EntityType entityType) {
        for (Map.Entry<SpawnBlockGroupSlot, SpawnBlockGroup> a : getManager().getBlockGroups().entrySet()) {
            if (!enabledGroups.getOrDefault(a.getKey(), false).booleanValue())
                continue;
            
            if (a.getValue().containsType(entityType))
                return true;
        }
        
        return false;
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
        
        destroy();
        getManager().giveFeatureItem(p, this);
        p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "SpawnBlocker successfully moved back to your inventory.");
        p.removeMetadata(MOVE_METADATA, getManager().getPlugin());
    }
}
