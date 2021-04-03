package de.craftlancer.clfeatures.stonecrusher;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.Feature;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class StoneCrusherFeature extends Feature<StoneCrusherFeatureInstance> {
    static final String MOVE_METADATA = "crusherMove";
    
    private static final Material CRUSHER_MATERIAL = Material.CHEST;
    private static final String CRUSHER_NAME = ChatColor.DARK_PURPLE + "StoneCrusher";
    
    private List<StoneCrusherFeatureInstance> instances = new ArrayList<>();
    
    private Map<Material, List<StoneCrusherResult>> lootTable = new EnumMap<>(Material.class);
    private int crushesPerTick = 1;
    
    public StoneCrusherFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "stonecrusher.limit"));
        
        crushesPerTick = config.getInt("stonesPerTick", 1);
        
        config.getMapList("lootTable").forEach(a -> {
            String matName = (String) a.get("input");
            Material input = matName != null ? Material.matchMaterial(matName) : null;
            
            if (input == null || input == Material.AIR)
                return;
            
            List<Map<?, ?>> output = (List<Map<?, ?>>) a.get("output");
            
            if (output == null)
                return;
            
            List<StoneCrusherResult> result = output.stream().map(b -> new StoneCrusherResult((String) b.get("item"), ((Number) b.get("chance")).doubleValue()))
                    .collect(Collectors.toList());
            
            lootTable.put(input, result);
        });
        
        instances = (List<StoneCrusherFeatureInstance>) YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/stonecrusher.yml"))
                .getList("stonecrusher", new ArrayList<>());
    }
    
    @Override
    public boolean isFeatureItem(ItemStack item) {
        if (item.getType() != CRUSHER_MATERIAL)
            return false;
        
        ItemMeta meta = item.getItemMeta();
        
        return (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(CRUSHER_NAME));
    }
    
    @Override
    public Collection<Block> checkEnvironment(Block initialBlock) {
        Chest chest = (Chest) initialBlock.getBlockData();
        BlockFace facing = chest.getFacing().getOppositeFace();
        
        List<Block> blocks = new ArrayList<>();
        blocks.add(initialBlock.getRelative(facing.getModZ(), 0, -facing.getModX()));
        blocks.add(initialBlock.getRelative(facing.getModZ(), 1, -facing.getModX()));
        blocks.add(initialBlock.getRelative(facing.getModZ(), 2, -facing.getModX()));
        blocks.add(initialBlock.getRelative(0, 1, 0));
        blocks.add(initialBlock.getRelative(0, 2, 0));
        blocks.add(initialBlock.getRelative(-facing.getModZ(), 0, facing.getModX()));
        blocks.add(initialBlock.getRelative(-facing.getModZ(), 1, facing.getModX()));
        blocks.add(initialBlock.getRelative(-facing.getModZ(), 2, facing.getModX()));
        
        return blocks.stream().filter(a -> !a.isEmpty()).collect(Collectors.toList());
    }
    
    @Override
    public boolean createInstance(Player creator, Block initialBlock, ItemStack hand) {
        Chest chest = (Chest) initialBlock.getBlockData();
        BlockFace facing = chest.getFacing().getOppositeFace();
        
        BlockData slabData = Material.QUARTZ_SLAB.createBlockData(a -> ((Slab) a).setType(Type.TOP));
        BlockData pistonData = Material.PISTON.createBlockData(a -> ((Piston) a).setFacing(BlockFace.DOWN));
        
        initialBlock.getRelative(facing.getModZ(), 0, -facing.getModX()).setBlockData(Material.CHEST.createBlockData(a -> {
            ((Chest) a).setType(Chest.Type.RIGHT);
            ((Chest) a).setFacing(facing.getOppositeFace());
        }));
        initialBlock.getRelative(facing.getModZ(), 1, -facing.getModX()).setBlockData(slabData);
        
        initialBlock.getRelative(facing.getModZ(), 2, -facing.getModX()).setBlockData(Material.TRAPPED_CHEST.createBlockData(a -> {
            ((Chest) a).setType(Chest.Type.RIGHT);
            ((Chest) a).setFacing(facing.getOppositeFace());
        }));
        initialBlock.getRelative(0, 1, 0).setBlockData(slabData);
        initialBlock.getRelative(0, 2, 0).setBlockData(Material.TRAPPED_CHEST.createBlockData(a -> {
            ((Chest) a).setType(Chest.Type.LEFT);
            ((Chest) a).setFacing(facing.getOppositeFace());
        }));
        initialBlock.getRelative(-facing.getModZ(), 0, facing.getModX()).setType(Material.END_PORTAL_FRAME);
        // initialBlock.getRelative(-facing.getModZ(), 1, -facing.getModX()); // air
        initialBlock.getRelative(-facing.getModZ(), 2, facing.getModX()).setBlockData(pistonData);
        
        List<Location> blocks = new ArrayList<>();
        blocks.add(initialBlock.getRelative(facing.getModZ(), 0, -facing.getModX()).getLocation());
        blocks.add(initialBlock.getRelative(facing.getModZ(), 1, -facing.getModX()).getLocation());
        blocks.add(initialBlock.getRelative(facing.getModZ(), 2, -facing.getModX()).getLocation());
        blocks.add(initialBlock.getLocation());
        blocks.add(initialBlock.getRelative(0, 1, 0).getLocation());
        blocks.add(initialBlock.getRelative(0, 2, 0).getLocation());
        blocks.add(initialBlock.getRelative(-facing.getModZ(), 0, facing.getModX()).getLocation());
        blocks.add(initialBlock.getRelative(-facing.getModZ(), 1, facing.getModX()).getLocation());
        blocks.add(initialBlock.getRelative(-facing.getModZ(), 2, facing.getModX()).getLocation());
        
        return createInstance(creator, initialBlock, blocks, null);
    }
    
    @Override
    public boolean createInstance(Player creator, Block initialLocation, List<Location> blocks, String usedSchematic) {
        return instances.add(new StoneCrusherFeatureInstance(this, creator.getUniqueId(), new BlockStructure(blocks), initialLocation.getLocation(),
                usedSchematic));
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/stonecrusher.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("stonecrusher", instances);
        
        BukkitRunnable saveTask = new LambdaRunnable(() -> {
            try {
                config.save(f);
            } catch (IOException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Error while saving Stonecrusher: ", e);
            }
        });
        
        if (getPlugin().isEnabled())
            saveTask.runTaskAsynchronously(getPlugin());
        else
            saveTask.run();
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new StoneCrusherCommandHandler(getPlugin(), this);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof StoneCrusherFeatureInstance)
            instances.remove(instance);
    }
    
    public int getCrushesPerTick() {
        return crushesPerTick;
    }
    
    public Map<Material, List<StoneCrusherResult>> getLootTable() {
        return lootTable;
    }
    
    @Override
    protected String getName() {
        return "Stonecrusher";
    }
    
    @Override
    public List<StoneCrusherFeatureInstance> getFeatures() {
        return instances;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteractMove(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (!event.hasBlock() || !p.hasMetadata(MOVE_METADATA))
            return;
        
        Optional<StoneCrusherFeatureInstance> feature = getFeatures().stream().filter(a -> a.getStructure().containsBlock(event.getClickedBlock())).findAny();
        
        if (!feature.isPresent())
            return;
        
        if (!feature.get().getOwnerId().equals(p.getUniqueId()))
            return;
        
        feature.get().destroy();
        giveFeatureItem(p, feature.get());
        p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "StoneCrusher successfully moved back to your inventory.");
        p.removeMetadata(MOVE_METADATA, getPlugin());
    }
}
