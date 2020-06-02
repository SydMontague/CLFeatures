package de.craftlancer.clfeatures.portal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Lectern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.Feature;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.portal.addressbook.AddressBookCommandHandler;
import de.craftlancer.clfeatures.portal.addressbook.AddressBookUtils;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;

public class PortalFeature extends Feature<PortalFeatureInstance> {
    static final String LOOP_METADATA = "portalLoop";
    static final String RENAME_METADATA = "portalRename";
    static final String MOVE_METADATA = "portalMove";
    
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
    
    private List<String> defaultPortals;
    private String defaultPortal;
    
    @SuppressWarnings("unchecked")
    public PortalFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "portal.limit"));
        
        // local stuff
        renameMoney = config.getDouble("renameMoney", 0D);
        renameItems = (List<ItemStack>) config.getList("renameItems", new ArrayList<>());
        moveItems = (List<ItemStack>) config.getList("moveItems", new ArrayList<>());
        inactivityTimeout = config.getLong("inactivityTimeout", 155520000L);
        booklessTicks = config.getLong("booklessTicks", 30L);
        portalCooldown = config.getInt("portalCooldown", 300);
        defaultPortals = config.getStringList("defaultPortals");
        defaultPortal = config.getString("defaultPortal", "valgard");
        
        plugin.getCommand("pbook").setExecutor(new AddressBookCommandHandler(plugin));
        
        instances = (List<PortalFeatureInstance>) YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/portals.yml"))
                                                                   .getList("portals", new ArrayList<>());
        
        instances.stream().filter(a -> a.getName() != null && !a.getName().isEmpty()).forEach(a -> lookupTable.put(a.getName().toLowerCase(), a));
        
        new LambdaRunnable(() -> 
            Bukkit.getOnlinePlayers().forEach(a -> {
                if(a.hasMetadata(LOOP_METADATA)) {
                    Location loc = (Location) a.getMetadata(LOOP_METADATA).get(0).value();
                    if (!loc.getWorld().equals(a.getWorld()) || loc.distanceSquared(a.getLocation()) > 2)
                        a.removeMetadata(LOOP_METADATA, plugin);
                }
            })
        ).runTaskTimer(plugin, 5, 5);
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
    public Collection<Block> checkEnvironment(Block initialBlock) {
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
        
        return blocks.stream().filter(a -> !a.isEmpty()).collect(Collectors.toList());
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
        
        List<Location> blocks = new ArrayList<>();
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 1, -1, facing.getModX() * -1).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 2, -1, facing.getModX() * -2).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 0, 0, facing.getModX() * -0).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 1, 0, facing.getModX() * -1).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 2, 0, facing.getModX() * -2).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 3, 0, facing.getModX() * -3).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 0, 1, facing.getModX() * -0).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 1, 1, facing.getModX() * -1).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 2, 1, facing.getModX() * -2).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 3, 1, facing.getModX() * -3).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 0, 2, facing.getModX() * -0).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 1, 2, facing.getModX() * -1).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 2, 2, facing.getModX() * -2).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 3, 2, facing.getModX() * -3).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 1, 3, facing.getModX() * -1).getLocation());
        blocks.add(firstPortalBlock.getRelative(facing.getModZ() * 2, 3, facing.getModX() * -2).getLocation());
        
        return createInstance(creator, initialBlock, blocks, null);
    }

    @Override
    public boolean createInstance(Player creator, Block initialBlock, List<Location> blocksPasted, String schematic) {
        blocksPasted.stream().map(Location::getBlock).filter(a -> a.getType() == Material.BARRIER).forEach(a -> a.setType(Material.AIR));
        BlockStructure blocks = new BlockStructure(blocksPasted);

        creator.sendMessage("[§4Craft§fCitizen]" + ChatColor.YELLOW + "Portal placed, use " + ChatColor.GREEN + "/portal name <name>" + ChatColor.YELLOW
                + " to give your portal an address!");
        return instances.add(new PortalFeatureInstance(this, creator, blocks, initialBlock, schematic));
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof PortalFeatureInstance) {
            instances.remove(instance);
            if (((PortalFeatureInstance) instance).getName() != null)
                lookupTable.remove(((PortalFeatureInstance) instance).getName().toLowerCase(), instance);
        }
    }
    
    public PortalFeatureInstance getPortal(String name) {
        if (name == null || name.isEmpty())
            return null;
        
        return lookupTable.get(name.toLowerCase());
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/portals.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("portals", instances);
        
        BukkitRunnable saveTask = new LambdaRunnable(() -> {
            try {
                config.save(f);
            }
            catch (IOException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Error while saving Portals: ", e);
            }
        });

        if (getPlugin().isEnabled())
            saveTask.runTaskAsynchronously(getPlugin());
        else
            saveTask.run();
    }
    
    public List<PortalFeatureInstance> getPortalsByPlayer(OfflinePlayer p) {
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
        if (oldName != null)
            lookupTable.remove(oldName.toLowerCase());
        lookupTable.put(newName.toLowerCase(), portalFeatureInstance);
    }
    
    @Override
    protected String getName() {
        return "Portal";
    }
    
    public List<String> getDefaultPortals() {
        return defaultPortals;
    }
    
    public String getDefaultPortal() {
        return defaultPortal;
    }

    @Override
    public List<PortalFeatureInstance> getFeatures() {
        return this.instances;
    }
    
    /* Listener */
    
    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        event.getPlayer().removeMetadata(LOOP_METADATA, getPlugin());
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteractMove(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (!event.hasBlock() || !p.hasMetadata(MOVE_METADATA))
            return;
        
        Optional<PortalFeatureInstance> portal = getFeatures().stream().filter(a -> a.getStructure().containsBlock(event.getClickedBlock())).findAny();
        
        if(!portal.isPresent())
            return;
        
        if(!portal.get().getOwnerId().equals(p.getUniqueId()))
            return;
        
        if(portal.get().getName() == null || checkMoveCost(p)) {
            if(portal.get().getName() != null)
                deductMoveCost(p);
            portal.get().destroy();
            giveFeatureItem(p, portal.get());
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "Portal successfully moved back to your inventory.");
        }
        else
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't afford to move this portal. You need 3 Lesser Fragments.");
        
        p.removeMetadata(MOVE_METADATA, getPlugin());
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (!event.hasBlock() || !p.hasMetadata(RENAME_METADATA))
            return;

        Optional<PortalFeatureInstance> portal = getFeatures().stream().filter(a -> a.getStructure().containsBlock(event.getClickedBlock())).findAny();
        
        if(!portal.isPresent())
            return;

        if(!portal.get().getOwnerId().equals(p.getUniqueId()))
            return;
        
        String newName = p.getMetadata(RENAME_METADATA).get(0).asString();
        
        boolean isFirstName = portal.get().getName() == null || portal.get().getName().isEmpty();
        
        if (isFirstName || checkRenameCosts(p)) {
            portal.get().setName(newName);
            
            if (!isFirstName)
                deductRenameCosts(p);
            else {
                // give first time books
                List<String> addressList = new ArrayList<>();
                addressList.add(newName);
                addressList.addAll(getDefaultPortals());
                
                ItemStack homeBook = AddressBookUtils.writeBook(new ItemStack(Material.WRITTEN_BOOK), getDefaultPortal(), addressList);
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
        
        p.removeMetadata(RENAME_METADATA, getPlugin());
    }
    
}
