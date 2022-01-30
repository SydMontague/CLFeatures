package de.craftlancer.clfeatures.replicator;

import de.craftlancer.clfeatures.BlueprintFeatureInstance;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.Utils;
import de.craftlancer.core.structure.BlockStructure;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Chunk;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReplicatorFeatureInstance extends BlueprintFeatureInstance {
    public static final String MOVE_METADATA = "replicatorMove";
    
    private ReplicatorFeature manager;
    
    private Location inputChest;
    private Location outputChest;
    private Location daylightSensor;
    private Inventory input;
    private Inventory output;
    
    private ReplicatorDisplayItem displayItem;
    
    private int tickId = 0;
    
    private Map<Material, Integer> recipe = new EnumMap<>(Material.class);
    private ItemStack product = null;
    
    public ReplicatorFeatureInstance(ReplicatorFeature manager, UUID ownerId, BlockStructure blocks, Location origin, String usedSchematic) {
        super(ownerId, blocks, origin, usedSchematic);
        
        this.manager = manager;
        
        outputChest = origin;
        inputChest = origin.clone().add(0, 2, 0);
        
        blocks.getBlocks().stream().filter(location -> location.getBlock().getType() == Material.BARRIER).forEach(location -> location.getBlock().setType(Material.AIR));
        daylightSensor = blocks.getBlocks().stream().filter(block -> block.getBlock().getType() == Material.DAYLIGHT_DETECTOR).findFirst().orElse(null);
        this.displayItem = new ReplicatorDisplayItem(this);
    }
    
    public ReplicatorFeatureInstance(Map<String, Object> map) {
        super(map);
        
        outputChest = getInitialBlock();
        inputChest = getInitialBlock().clone().add(0, 2, 0);
        daylightSensor = (Location) map.get("daylightDetector");
        
        if (map.containsKey("recipe") && map.containsKey("product")) {
            @SuppressWarnings("unchecked")
            List<ItemStack> list = (List<ItemStack>) map.get("recipe");
            list.forEach(item -> {
                if (recipe.containsKey(item.getType()))
                    recipe.put(item.getType(), recipe.get(item.getType()) + 1);
                else
                    recipe.put(item.getType(), 1);
            });
            
            product = (ItemStack) map.get("product");
        }
        displayItem = new ReplicatorDisplayItem(this);
    }
    
    @Override
    public void destroy() {
        displayItem.remove();
        super.destroy();
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
        }
        
        map.put("daylightDetector", daylightSensor);
        new LambdaRunnable(() -> displayItem.remove()).runTask(getManager().getPlugin());
        
        return map;
    }
    
    @Override
    protected void tick() {
        tickId += 10;
        
        if (daylightSensor == null) {
            getManager().getPlugin().getLogger().severe("Tried ticking Replicator without Daylight Sensor at " + getInitialBlock());
            return;
        }
        
        if (recipe == null || recipe.isEmpty() || product == null)
            return;
        
        if (input == null || output == null) {
            input = ((Chest) getInputChest().getBlock().getState()).getInventory();
            output = ((Chest) getOutputChest().getBlock().getState()).getInventory();
        }
        
        if (!Utils.isChunkLoaded(inputChest) || !Utils.isChunkLoaded(outputChest) || !Utils.isChunkLoaded(daylightSensor))
            return;
        
        displayItem.tick();
        
        DaylightDetector detector = (DaylightDetector) daylightSensor.getBlock().getBlockData();
        //Does the input chest contain all necessary materials?
        for (Map.Entry<Material, Integer> entry : recipe.entrySet()) {
            if (!input.contains(entry.getKey(), entry.getValue())) {
                if (!detector.isInverted()) {
                    detector.setInverted(true);
                    daylightSensor.getBlock().setBlockData(detector);
                }
                return;
            }
        }
        
        //Remove all items in the recipe from input chest
        if (tickId % 20 == 0) {
            craft();
        }
        daylightSensor.getWorld().playSound(daylightSensor, Sound.BLOCK_BEACON_AMBIENT, 0.2F, 1F);
        spawnParticles();
        
        detector.setInverted(false);
        daylightSensor.getBlock().setBlockData(detector);
    }
    
    private void spawnParticles() {
        if (!Utils.isChunkLoaded(daylightSensor))
            return;
        Location centerSensor = daylightSensor.clone().add(0.5, 0, 0.5);
        Particle.DustOptions particle = new Particle.DustOptions(Color.WHITE, 1F);
        for (double i = daylightSensor.getY(); i < daylightSensor.getY() + 1; i += 0.05) {
            centerSensor.setY(i);
            centerSensor.getWorld().spawnParticle(Particle.REDSTONE, centerSensor, 1, particle);
        }
    }
    
    private void craft() {
        //Removing from input
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
        
        //Adding to output
        Location dropLocation = getOutputChest().clone();
        dropLocation.setY(dropLocation.getY() + 1);
        dropLocation.setX(dropLocation.getX() + 0.5);
        dropLocation.setZ(dropLocation.getZ() + 0.5);
        
        ItemStack[] items;
        if (product.getType() == Material.HONEY_BLOCK)
            items = new ItemStack[]{product, new ItemStack(Material.GLASS_BOTTLE, 4)};
        else
            items = new ItemStack[]{product};
        
        World world = getInitialBlock().getWorld();
        output.addItem(items).forEach((k, v) -> {
            world.dropItem(dropLocation, v);
            world.playSound(getInitialBlock(), Sound.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1, 1);
        });
    }
    
    @Override
    protected ReplicatorFeature getManager() {
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
        
        if (!block.getLocation().equals(daylightSensor))
            return;
        
        event.setCancelled(true);
        
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SCAFFOLDING_BREAK, 0.5F, 1F);
        player.openWorkbench(daylightSensor, true);
    }
    
    @EventHandler
    public void onPreRecipeSet(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        
        if (inventory.getLocation() == null || !inventory.getLocation().equals(daylightSensor))
            return;
        
        List<Material> materials = Arrays.asList(inventory.getMatrix()).stream().filter(Objects::nonNull).map(ItemStack::getType).collect(Collectors.toList());
        
        if (materials.size() < 9)
            return;
        
        if (materials.stream().anyMatch(mat -> !mat.name().contains("CONCRETE_POWDER")))
            return;
        
        Material color = materials.get(0);
        
        if (materials.stream().anyMatch(mat -> mat != color))
            return;
        
        inventory.setResult(new ItemStack(Material.valueOf(color.name().replace("CONCRETE_POWDER", "CONCRETE")), 9));
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onRecipeSet(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof CraftingInventory) || event.getSlotType() != InventoryType.SlotType.RESULT)
            return;
        
        CraftingInventory inventory = (CraftingInventory) event.getInventory();
        
        if (inventory.getResult() == null || inventory.getResult().getType() == Material.AIR)
            return;
        
        if (inventory.getLocation() == null || !inventory.getLocation().equals(daylightSensor))
            return;
        
        Player player = (Player) event.getWhoClicked();
        List<Material> blockedProducts = getManager().getBlockedProducts();
        
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
        
        displayItem.setItemStack(product);
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
    
    public Location getDaylightSensor() {
        return daylightSensor;
    }
    
    public ReplicatorDisplayItem getDisplayItem() {
        return displayItem;
    }
    
    @EventHandler()
    public void onChunkLoad(ChunkLoadEvent e) {
        Chunk c = e.getChunk();
        
        if (c.getX() == inputChest.getBlockX() >> 4 && c.getZ() == inputChest.getBlockZ() >> 4) {
            input = ((Chest) getInputChest().getBlock().getState()).getInventory();
            output = ((Chest) getOutputChest().getBlock().getState()).getInventory();
            displayItem.tick();
        }
    }
    
    @EventHandler()
    public void onChunkUnload(ChunkUnloadEvent e) {
        Chunk c = e.getChunk();
        if (c.getX() == inputChest.getBlockX() >> 4 && c.getZ() == inputChest.getBlockZ() >> 4)
            displayItem.remove();
    }
}
