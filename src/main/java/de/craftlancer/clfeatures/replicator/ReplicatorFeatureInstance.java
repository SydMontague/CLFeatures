package de.craftlancer.clfeatures.replicator;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.Feature;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.structure.BlockStructure;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.DaylightDetector;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReplicatorFeatureInstance extends FeatureInstance {
    public static final String MOVE_METADATA = "replicatorMove";
    
    private ReplicatorFeature manager;
    
    private Location inputChest;
    private Location outputChest;
    private Location daylightCensor;
    private Inventory input;
    private Inventory output;
    
    private ReplicatorDisplayItem displayItem;
    
    private int tickId = 0;
    
    private Map<Material, Integer> recipe = new HashMap<>();
    private ItemStack product = null;
    
    public static boolean isChunkLoadedAtLocation(Location location) {
        return (location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4));
    }
    
    public ReplicatorFeatureInstance(ReplicatorFeature manager, UUID ownerId, BlockStructure blocks, Location origin) {
        super(ownerId, blocks, origin);
        
        this.manager = manager;
        
        outputChest = origin;
        inputChest = origin.clone().add(0, 2, 0);
        
        blocks.getBlocks().stream().filter(location -> location.getBlock().getType() == Material.BARRIER).forEach(location -> location.getBlock().setType(Material.AIR));
        daylightCensor = blocks.getBlocks().stream().filter(block -> block.getBlock().getType() == Material.DAYLIGHT_DETECTOR).findFirst().get();
        
    }
    
    public ReplicatorFeatureInstance(Map<String, Object> map) {
        super(map);
        
        outputChest = getInitialBlock();
        inputChest = getInitialBlock().clone().add(0, 2, 0);
        daylightCensor = (Location) map.get("daylightDetector");
        
        if (map.containsKey("recipe") && map.containsKey("product")) {
            List<ItemStack> list = (List<ItemStack>) map.get("recipe");
            list.forEach(item -> {
                if (recipe.containsKey(item.getType()))
                    recipe.put(item.getType(), recipe.get(item.getType()) + 1);
                else
                    recipe.put(item.getType(), 1);
            });
            
            product = (ItemStack) map.get("product");
            
            displayItem = new ReplicatorDisplayItem(this);
        }
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        if (recipe != null && product != null && !recipe.isEmpty()) {
            List<ItemStack> list = new ArrayList<>();
            recipe.forEach((key, value) -> {
                for (int i = 0; i < value; i++)
                    list.add(new ItemStack(key));
            });
            
            map.put("recipe", list);
            map.put("product", product);
            map.put("daylightDetector", daylightCensor);
        }
        
        displayItem.remove();
        
        return map;
    }
    
    @Override
    protected void tick() {
        tickId += 10;
        
        if (displayItem != null && displayItem.getItem() != null) {
            if (!displayItem.getItem().isValid() && isChunkLoadedAtLocation(daylightCensor))
                displayItem.spawn();
            else
                displayItem.teleport();
            displayItem.getItem().setPickupDelay(Integer.MAX_VALUE);
        }
        
        if (recipe == null || recipe.isEmpty() || product == null)
            return;
        
        if (input == null || output == null) {
            input = ((Chest) getInputChest().getBlock().getState()).getInventory();
            output = ((Chest) getOutputChest().getBlock().getState()).getInventory();
        }
        
        if (!isChunkLoadedAtLocation(inputChest) || !isChunkLoadedAtLocation(outputChest))
            return;
        
        DaylightDetector detector = (DaylightDetector) daylightCensor.getBlock().getBlockData();
        //Does the input chest contain all necessary materials?
        for (Map.Entry<Material, Integer> entry : recipe.entrySet()) {
            if (!input.contains(entry.getKey(), entry.getValue())) {
                if (!detector.isInverted()) {
                    detector.setInverted(true);
                    daylightCensor.getBlock().setBlockData(detector);
                }
                return;
            }
        }
        
        //Remove all items in the recipe from input chest
        if (tickId % 20 == 0) {
            removeFromInput();
            addToOutput();
        }
        daylightCensor.getWorld().playSound(daylightCensor, Sound.BLOCK_BEACON_AMBIENT, 0.2F, 1F);
        doParticles();
        
        detector.setInverted(false);
        daylightCensor.getBlock().setBlockData(detector);
    }
    
    private void doParticles() {
        if (!isChunkLoadedAtLocation(daylightCensor))
            return;
        Location centerSensor = daylightCensor.clone();
        Particle.DustOptions particle = new Particle.DustOptions(Color.WHITE, 1F);
        centerSensor.setX(centerSensor.getX() + 0.5);
        centerSensor.setZ(centerSensor.getZ() + 0.5);
        for (double i = daylightCensor.getY(); i < daylightCensor.getY() + 1; i += 0.05) {
            centerSensor.setY(i);
            
            centerSensor.getWorld().spawnParticle(Particle.REDSTONE, centerSensor, 1, particle);
        }
    }
    
    private void removeFromInput() {
        for (Map.Entry<Material, Integer> entry : recipe.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                int first = input.first(entry.getKey());
                
                if (first == -1)
                    return;
                
                ItemStack firstItem = input.getItem(first);
                firstItem.setAmount(firstItem.getAmount() - 1);
                input.setItem(first, firstItem);
            }
        }
    }
    
    private void addToOutput() {
        Location dropLocation = getOutputChest().clone();
        dropLocation.setY(dropLocation.getY() + 1);
        dropLocation.setX(dropLocation.getX() + 0.5);
        dropLocation.setZ(dropLocation.getZ() + 0.5);
        if (!output.addItem(product).isEmpty()) {
            World world = getInitialBlock().getWorld();
            world.dropItem(dropLocation, product);
            world.playSound(getInitialBlock(), Sound.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1, 1);
        }
    }
    
    @Override
    protected Feature<?> getManager() {
        if (manager == null)
            manager = (ReplicatorFeature) CLFeatures.getInstance().getFeature("replicator");
        
        return manager;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDaylightDetectInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        
        if (!block.getLocation().equals(daylightCensor))
            return;
        
        event.setCancelled(true);
        
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SCAFFOLDING_BREAK, 0.5F, 1F);
        player.openWorkbench(daylightCensor, true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onRecipeSet(CraftItemEvent event) {
        CraftingInventory inventory = event.getInventory();
        
        if (inventory.getLocation() == null || !inventory.getLocation().equals(daylightCensor))
            return;
        
        Player player = (Player) event.getWhoClicked();
        List<Material> blockedProducts = ((ReplicatorFeature) CLFeatures.getInstance().getFeature("replicator")).getBlockedProducts();
        
        if (blockedProducts.contains(inventory.getResult().getType())) {
            player.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "This item is too powerful for the replicator and cannot be crafted.");
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.5F, 1F);
            
            event.setCancelled(true);
            player.closeInventory();
            return;
        }
        
        recipe.clear();
        
        for (ItemStack item : inventory.getMatrix()) {
            if (item == null)
                continue;
            if (recipe.containsKey(item.getType()))
                recipe.put(item.getType(), recipe.get(item.getType()) + 1);
            else
                recipe.put(item.getType(), 1);
        }
        
        product = inventory.getResult();
        
        player.closeInventory();
        
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SCAFFOLDING_PLACE, 0.5F, 1F);
        player.sendMessage(CLFeatures.CC_PREFIX + "§eYou have set the replicator to craft §6" + product.getType().name() + "§e.");
        
        if (displayItem != null)
            displayItem.remove();
        displayItem = new ReplicatorDisplayItem(this);
        displayItem.spawn();
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
        getManager().giveFeatureItem(p);
        p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "Replicator successfully moved back to your inventory.");
        p.removeMetadata(MOVE_METADATA, getManager().getPlugin());
    }
    
    public ItemStack getProduct() {
        return product;
    }
    
    public Location getInputChest() {
        return inputChest;
    }
    
    public Location getOutputChest() {
        return outputChest;
    }
    
    public Location getDaylightCensor() {
        return daylightCensor;
    }
    
    public ReplicatorDisplayItem getDisplayItem() {
        return displayItem;
    }
}
