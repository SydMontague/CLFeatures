package de.craftlancer.clfeatures.stonecrusher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.Feature;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;

public class StoneCrusherFeature extends Feature {
    private static final Material CRUSHER_MATERIAL = Material.CHEST;
    private static final String CRUSHER_NAME = ChatColor.DARK_PURPLE + "StoneCrusher";

    private List<StoneCrusherFeatureInstance> instances = new ArrayList<>();
    
    private List<CrusherResult> lootTableStone = new ArrayList<>();
    private List<CrusherResult> lootTableCobble = new ArrayList<>();
    private List<CrusherResult> lootTableGravel = new ArrayList<>();
    
    private int crushesPerTick = 1;
    
    @SuppressWarnings("unchecked")
    public StoneCrusherFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config);
        
        crushesPerTick = config.getInt("stonesPerTick", 1);
        
        ConfigurationSection stoneConfig = config.getConfigurationSection("lootTableStone");
        ConfigurationSection cobbleConfig = config.getConfigurationSection("lootTableCobble");
        ConfigurationSection gravelConfig = config.getConfigurationSection("lootTableGravel");
        
        stoneConfig.getKeys(false).forEach(a -> lootTableStone.add(new CrusherResult(stoneConfig.getItemStack(a + ".item"), stoneConfig.getDouble(a + ".chance"))));
        cobbleConfig.getKeys(false).forEach(a -> lootTableCobble.add(new CrusherResult(cobbleConfig.getItemStack(a + ".item"), cobbleConfig.getDouble(a + ".chance"))));
        gravelConfig.getKeys(false).forEach(a -> lootTableGravel.add(new CrusherResult(gravelConfig.getItemStack(a + ".item"), gravelConfig.getDouble(a + ".chance"))));
                                                                   
        instances = (List<StoneCrusherFeatureInstance>) YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/stonecrusher.yml"))
                                                                   .getList("stonecrusher", new ArrayList<>());
    }
    
    class CrusherResult {
        private final ItemStack result;
        private final double chance;
        
        public CrusherResult(ItemStack result, double chance) {
            this.chance = chance;
            this.result = result;
        }
        
        public double getChance() {
            return chance;
        }
        
        public ItemStack getResult() {
            return result;
        }
    }
    
    @Override
    public void giveFeatureItem(Player player) {
        ItemStack item = new ItemStack(CRUSHER_MATERIAL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(CRUSHER_NAME);
        item.setItemMeta(meta);
        
        player.getInventory().addItem(item);
    }
    
    @Override
    public boolean isFeatureItem(ItemStack item) {
        if (item.getType() != CRUSHER_MATERIAL)
            return false;
        
        ItemMeta meta = item.getItemMeta();
        
        return (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(CRUSHER_NAME));
    }
    
    @Override
    public boolean checkFeatureLimit(Player player) {
        if (player.hasPermission("clfeature.portal.ignoreLimit"))
            return true;
        
        int limit = getLimit(player);
        
        if (limit < 0)
            return true;
        
        long current = instances.stream().filter(a -> a.isOwner(player)).count();
        
        return current < limit;
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
    public boolean createInstance(Player creator, Block initialBlock) {
        Chest chest = (Chest) initialBlock.getBlockData();
        BlockFace facing = chest.getFacing().getOppositeFace();
        
        BlockData slabData = Material.QUARTZ_SLAB.createBlockData(a -> ((Slab) a).setType(Type.TOP));
        BlockData pistonData = Material.PISTON.createBlockData(a -> ((Piston) a).setFacing(BlockFace.DOWN));
        
        //new BlockData
        
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
        //initialBlock.getRelative(-facing.getModZ(), 1, -facing.getModX()); // air 
        initialBlock.getRelative(-facing.getModZ(), 2, facing.getModX()).setBlockData(pistonData);
        
        BlockStructure blocks = new BlockStructure();
        blocks.addBlock(initialBlock.getRelative(facing.getModZ(), 0, -facing.getModX()));
        blocks.addBlock(initialBlock.getRelative(facing.getModZ(), 1, -facing.getModX()));
        blocks.addBlock(initialBlock.getRelative(facing.getModZ(), 2, -facing.getModX()));
        blocks.addBlock(initialBlock);
        blocks.addBlock(initialBlock.getRelative(0, 1, 0));
        blocks.addBlock(initialBlock.getRelative(0, 2, 0));
        blocks.addBlock(initialBlock.getRelative(-facing.getModZ(), 0, facing.getModX()));
        blocks.addBlock(initialBlock.getRelative(-facing.getModZ(), 1, facing.getModX()));
        blocks.addBlock(initialBlock.getRelative(-facing.getModZ(), 2, facing.getModX()));
        
        return instances.add(new StoneCrusherFeatureInstance(this, creator.getUniqueId(), blocks, initialBlock.getLocation()));
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/stonecrusher.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("stonecrusher", instances);
        try {
            config.save(f);
        }
        catch (IOException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Error while saving Portals: ", e);
        }
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
    
    public List<CrusherResult> getLootTableStone() {
        return Collections.unmodifiableList(lootTableStone);
    }
    
    public List<CrusherResult> getLootTableCobble() {
        return Collections.unmodifiableList(lootTableCobble);
    }
    
    public List<CrusherResult> getLootTableGravel() {
        return Collections.unmodifiableList(lootTableGravel);
    }
}
