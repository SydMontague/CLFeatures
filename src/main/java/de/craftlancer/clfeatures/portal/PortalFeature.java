package de.craftlancer.clfeatures.portal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Lectern;
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

public class PortalFeature extends Feature {
    
    private static final Material LECTERN_MATERIAL = Material.LECTERN;
    private static final String LECTERN_NAME = ChatColor.DARK_PURPLE + "Portal";
    private static final Material PORTAL_MATERIAL = Material.CHISELED_QUARTZ_BLOCK;
    
    private Map<String, PortalFeatureInstance> lookupTable = new HashMap<>();
    private List<PortalFeatureInstance> instances = new ArrayList<>();
    
    private long inactivityTimeout;
    private long booklessTicks;
    private double renameMoney;
    private int portalCooldown;
    private List<ItemStack> renameItems;
    private List<ItemStack> moveItems;
    
    @SuppressWarnings("unchecked")
    public PortalFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config);
        
        // local stuff
        renameMoney = config.getDouble("renameMoney", 0D);
        renameItems = (List<ItemStack>) config.getList("renameItems", new ArrayList<>());
        moveItems = (List<ItemStack>) config.getList("moveItems", new ArrayList<>());
        inactivityTimeout = config.getLong("inactivityTimeout", 155520000L);
        booklessTicks = config.getLong("booklessTicks", 30L);
        portalCooldown = config.getInt("portalCooldown", 300);
        
        instances = (List<PortalFeatureInstance>) YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/portals.yml"))
                                                                   .getList("portals", new ArrayList<>());
        
        instances.stream().filter(a -> a.getName() != null && !a.getName().isEmpty()).forEach(a -> lookupTable.put(a.getName().toLowerCase(), a));
    }
    
    @Override
    public void giveFeatureItem(Player player) {
        ItemStack item = new ItemStack(LECTERN_MATERIAL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(LECTERN_NAME);
        item.setItemMeta(meta);
        
        player.getInventory().addItem(item);
    }
    
    @Override
    public boolean isFeatureItem(ItemStack item) {
        if (item.getType() != LECTERN_MATERIAL)
            return false;
        
        ItemMeta meta = item.getItemMeta();
        
        return (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(LECTERN_NAME));
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
    public boolean checkEnvironment(Block initialBlock) {
        Lectern lectern = (Lectern) initialBlock.getBlockData();
        BlockFace facing = lectern.getFacing().getOppositeFace();
        
        Block firstPortalBlock = initialBlock.getRelative(facing);
        List<Block> blocks = new ArrayList<>();
        
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 1, -1, facing.getModX() * -1));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 2, -1, facing.getModX() * -2));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 0, 0, facing.getModX() * -0));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 1, 0, facing.getModX() * -1));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 2, 0, facing.getModX() * -2));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 3, 0, facing.getModX() * -3));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 0, 1, facing.getModX() * -0));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 1, 1, facing.getModX() * -1));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 2, 1, facing.getModX() * -2));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 3, 1, facing.getModX() * -3));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 0, 2, facing.getModX() * -0));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 1, 2, facing.getModX() * -1));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 2, 2, facing.getModX() * -2));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 3, 2, facing.getModX() * -3));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 1, 3, facing.getModX() * -1));
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 2, 3, facing.getModX() * -2));
        
        return blocks.stream().allMatch(Block::isEmpty);
    }
    
    public boolean checkRenameCosts(Player player) {
        boolean money = getPlugin().getEconomy() == null || getPlugin().getEconomy().has(player, renameMoney);
        boolean items = renameItems.stream().allMatch(a -> player.getInventory().containsAtLeast(a, a.getAmount()));
        
        return money && items;
    }
    
    public boolean deductRenameCosts(Player player) {
        boolean moneySuccess = getPlugin().getEconomy() == null || getPlugin().getEconomy().withdrawPlayer(player, renameMoney).transactionSuccess();
        boolean itemSuccess = player.getInventory().removeItem(renameItems.toArray(new ItemStack[0])).isEmpty();
        
        return moneySuccess && itemSuccess;
    }
    
    public boolean checkMoveCost(Player player) {
        return moveItems.stream().allMatch(a -> player.getInventory().containsAtLeast(a, a.getAmount()));
    }
    
    public boolean deductMoveCost(Player player) {
        return player.getInventory().removeItem(moveItems.toArray(new ItemStack[0])).isEmpty();
    }
    
    @Override
    public boolean createInstance(Player creator, Block initialBlock) {
        Lectern lectern = (Lectern) initialBlock.getBlockData();
        BlockFace facing = lectern.getFacing().getOppositeFace();
        Block firstPortalBlock = initialBlock.getRelative(facing);
        
        firstPortalBlock.getRelative(facing.getModZ() * 1, -1, facing.getModX() * -1).setType(PORTAL_MATERIAL); // Quarz
        firstPortalBlock.getRelative(facing.getModZ() * 2, -1, facing.getModX() * -2).setType(PORTAL_MATERIAL); // Quarz
        firstPortalBlock.getRelative(facing.getModZ() * 0, 0, facing.getModX() * -0).setType(PORTAL_MATERIAL); // Quarz
        firstPortalBlock.getRelative(facing.getModZ() * 3, 0, facing.getModX() * -3).setType(PORTAL_MATERIAL); // Quarz
        firstPortalBlock.getRelative(facing.getModZ() * 0, 1, facing.getModX() * -0).setType(PORTAL_MATERIAL); // Quarz
        firstPortalBlock.getRelative(facing.getModZ() * 3, 1, facing.getModX() * -3).setType(PORTAL_MATERIAL); // Quarz
        firstPortalBlock.getRelative(facing.getModZ() * 0, 2, facing.getModX() * -0).setType(PORTAL_MATERIAL); // Quarz
        firstPortalBlock.getRelative(facing.getModZ() * 3, 2, facing.getModX() * -3).setType(PORTAL_MATERIAL); // Quarz
        firstPortalBlock.getRelative(facing.getModZ() * 1, 3, facing.getModX() * -1).setType(PORTAL_MATERIAL); // Quarz
        firstPortalBlock.getRelative(facing.getModZ() * 2, 3, facing.getModX() * -2).setType(PORTAL_MATERIAL); // Quarz
        
        BlockStructure blocks = new BlockStructure(initialBlock.getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 1, -1, facing.getModX() * -1).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 2, -1, facing.getModX() * -2).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 0, 0, facing.getModX() * -0).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 1, 0, facing.getModX() * -1).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 2, 0, facing.getModX() * -2).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 3, 0, facing.getModX() * -3).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 0, 1, facing.getModX() * -0).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 1, 1, facing.getModX() * -1).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 2, 1, facing.getModX() * -2).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 3, 1, facing.getModX() * -3).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 0, 2, facing.getModX() * -0).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 1, 2, facing.getModX() * -1).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 2, 2, facing.getModX() * -2).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 3, 2, facing.getModX() * -3).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 1, 3, facing.getModX() * -1).getLocation());
        blocks.addBlock(firstPortalBlock.getRelative(facing.getModZ() * 2, 3, facing.getModX() * -2).getLocation());
        
        return instances.add(new PortalFeatureInstance(this, creator, blocks, initialBlock));
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof PortalFeatureInstance) {
            instances.remove(instance);
            if(((PortalFeatureInstance) instance).getName() != null)
                lookupTable.remove(((PortalFeatureInstance) instance).getName().toLowerCase(), instance);
        }
    }
    
    public PortalFeatureInstance getPortal(String name) {
        if(name == null || name.isEmpty())
            return null;
            
        return lookupTable.get(name.toLowerCase());
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/portals.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("portals", instances);
        try {
            config.save(f);
        }
        catch (IOException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Error while saving Portals: ", e);
        }
    }
    
    public List<PortalFeatureInstance> getPortalsByPlayer(Player p) {
        return instances.stream().filter(a -> a.isOwner(p)).collect(Collectors.toList());
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new PortalFeatureSubCommandHandler(getPlugin(), this);
    }
    
    public long getInactivityTimeout() {
        return inactivityTimeout;
    }
    
    public long getBooklessTicks() {
        return booklessTicks;
    }
    
    public int getPortalCooldown() {
        return portalCooldown;
    }

    public void updatedName(PortalFeatureInstance portalFeatureInstance, String oldName, String newName) {
        if(oldName != null)
            lookupTable.remove(oldName.toLowerCase());
        lookupTable.put(newName.toLowerCase(), portalFeatureInstance);
    }
    
}
