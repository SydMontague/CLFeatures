package de.craftlancer.clfeatures.portal;

import de.craftlancer.clfeatures.BlueprintFeature;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.portal.addressbook.AddressBookCommandHandler;
import de.craftlancer.clfeatures.portal.addressbook.AddressBookUtils;
import de.craftlancer.clfeatures.portal.event.PortalTeleportEvent;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPostPasteEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PortalFeature extends BlueprintFeature<PortalFeatureInstance> {
    static final String LOOP_METADATA = "portalLoop";
    static final String RENAME_METADATA = "portalRename";
    static final String MOVE_METADATA = "portalMove";
    private static final String COOLDOWN_METADATA = "hasPortalCooldown";
    
    private static final Material LECTERN_MATERIAL = Material.LECTERN;
    private static final String LECTERN_NAME = ChatColor.DARK_PURPLE + "Portal";
    private static final Material PORTAL_MATERIAL = Material.CHISELED_QUARTZ_BLOCK;
    
    private Map<String, PortalFeatureInstance> lookupTable = new HashMap<>();
    private List<PortalFeatureInstance> instances = new ArrayList<>();
    
    private long inactivityTimeout;
    private long booklessTicks;
    private long newBookDelay;
    private double renameMoney;
    private int portalCooldown;
    private List<ItemStack> renameItems;
    private List<ItemStack> moveItems;
    
    private List<String> defaultPortals;
    private String defaultPortal;
    
    public PortalFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "portal.limit"));
        
        // local stuff
        renameMoney = config.getDouble("renameMoney", 0D);
        renameItems = (List<ItemStack>) config.getList("renameItems", new ArrayList<>());
        moveItems = (List<ItemStack>) config.getList("moveItems", new ArrayList<>());
        inactivityTimeout = config.getLong("inactivityTimeout", 155520000L);
        booklessTicks = config.getLong("booklessTicks", 30L);
        portalCooldown = config.getInt("portalCooldown", 600);
        defaultPortals = config.getStringList("defaultPortals");
        defaultPortal = config.getString("defaultPortal", "valgard");
        newBookDelay = config.getLong("newBookDelay", 100L);
        
        plugin.getCommand("pbook").setExecutor(new AddressBookCommandHandler(plugin));
        
        instances = (List<PortalFeatureInstance>) YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/portals.yml"))
                .getList("portals", new ArrayList<>());
        
        instances.stream().filter(a -> a.getName() != null && !a.getName().isEmpty()).forEach(a -> lookupTable.put(a.getName().toLowerCase(), a));
        
        new LambdaRunnable(() -> Bukkit.getOnlinePlayers().forEach(a -> {
            if (a.hasMetadata(LOOP_METADATA)) {
                Location loc = (Location) a.getMetadata(LOOP_METADATA).get(0).value();
                if (!loc.getWorld().equals(a.getWorld()) || loc.distanceSquared(a.getLocation()) > 2)
                    a.removeMetadata(LOOP_METADATA, plugin);
            }
        })).runTaskTimer(plugin, 5, 5);
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
    public boolean createInstance(Player creator, BlueprintPostPasteEvent e) {
        e.getBlocksPasted().stream().map(Location::getBlock).filter(a -> a.getType() == Material.BARRIER).forEach(a -> a.setType(Material.AIR));
        BlockStructure blocks = new BlockStructure(e.getBlocksPasted());
        
        creator.sendMessage("[§4Craft§fCitizen]" + ChatColor.YELLOW + "Portal placed, use " + ChatColor.GREEN + "/portal name <name>" + ChatColor.YELLOW
                + " to give your portal an address!");
        return instances.add(new PortalFeatureInstance(this, creator, blocks, e.getFeatureLocation().getBlock(), e.getSchematic()));
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
            } catch (IOException e) {
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
    
    @EventHandler
    public void onBookTake(PlayerTakeLecternBookEvent event) {
        getFeatures().stream().filter(a -> a.getStructure().containsBlock(event.getLectern().getBlock())).findAny()
                .ifPresent(a -> a.setNewBookDelay(newBookDelay / 10));
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteractMove(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (!event.hasBlock() || !p.hasMetadata(MOVE_METADATA))
            return;
        
        Optional<PortalFeatureInstance> portal = getFeatures().stream().filter(a -> a.getStructure().containsBlock(event.getClickedBlock())).findAny();
        
        if (!portal.isPresent())
            return;
        
        if (!portal.get().getOwnerId().equals(p.getUniqueId()))
            return;
        
        if (portal.get().getName() == null || checkMoveCost(p)) {
            if (portal.get().getName() != null)
                deductMoveCost(p);
            portal.get().destroy();
            giveFeatureItem(p, portal.get());
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "Portal successfully moved back to your inventory.");
        } else
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't afford to move this portal. You need 3 Lesser Fragments.");
        
        p.removeMetadata(MOVE_METADATA, getPlugin());
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (!event.hasBlock() || !p.hasMetadata(RENAME_METADATA))
            return;
        
        Optional<PortalFeatureInstance> portal = getFeatures().stream().filter(a -> a.getStructure().containsBlock(event.getClickedBlock())).findAny();
        
        if (!portal.isPresent())
            return;
        
        if (!portal.get().getOwnerId().equals(p.getUniqueId()))
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
        } else
            p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "You can't afford to rename this portal. You need a Lesser Fragment.");
        
        p.removeMetadata(RENAME_METADATA, getPlugin());
    }
    
    /*
     * Portal Cooldown on death
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        player.setMetadata(COOLDOWN_METADATA, new FixedMetadataValue(getPlugin(), ""));
        new PortalCooldown(getPlugin(), player, getPortalCooldown() / 20).runTaskTimer(getPlugin(), 0, 20);
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPortal(PortalTeleportEvent event) {
        if (event.getPlayer().hasMetadata(COOLDOWN_METADATA))
            event.setCancelled(true);
    }
    
    private class PortalCooldown extends BukkitRunnable {
        private final Plugin plugin;
        private final Player player;
        private final int timer;
        private int currentTimer = 0;
        
        private BossBar bar;
        
        public PortalCooldown(Plugin plugin, Player p, int timer) {
            this.timer = timer <= 0 ? 1 : timer;
            this.player = p;
            this.plugin = plugin;
            
            bar = Bukkit.createBossBar("Portal Cooldown", BarColor.PURPLE, BarStyle.SOLID);
            bar.addPlayer(player);
        }
        
        @Override
        public void run() {
            bar.setProgress(1D - (double) currentTimer / timer);
            bar.setTitle(ChatColor.YELLOW + "Portal Cooldown " + ChatColor.GRAY + " - " + ChatColor.GOLD + " " + (timer - currentTimer) + " " + ChatColor.YELLOW
                    + "seconds");
            
            if (currentTimer++ >= timer) {
                bar.removeAll();
                player.removeMetadata(COOLDOWN_METADATA, plugin);
                cancel();
            }
        }
    }
}
