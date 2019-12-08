package de.craftlancer.clfeatures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import de.craftlancer.clfeatures.portal.PortalFeature;
import de.craftlancer.clfeatures.portal.PortalFeatureInstance;
import de.craftlancer.clfeatures.stonecrusher.StoneCrusherFeature;
import de.craftlancer.clfeatures.stonecrusher.StoneCrusherFeatureInstance;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

// TODO make /portal command available

/*
 * feature
 *   feature list
 *   feature buy
 *   feature portal
 *      feature portal list
 *      feature portal name
 *      
 * 
 * 
 * 
 */

public class CLFeatures extends JavaPlugin implements Listener {
    
    private static CLFeatures instance;
    
    private Map<String, Feature> features = new HashMap<>();
    private Economy econ = null;
    private Permission perms = null;
    
    public static CLFeatures getInstance() {
        return instance;
    }
    
    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(PortalFeatureInstance.class);
        ConfigurationSerialization.registerClass(StoneCrusherFeatureInstance.class);
        
        instance = this;
        setupEconomy();
        setupPermissions();
        
        getServer().getPluginManager().registerEvents(this, this);
        
        registerFeature("portal", new PortalFeature(this, getConfig().getConfigurationSection("portal")));
        registerFeature("stonecrusher", new StoneCrusherFeature(this, getConfig().getConfigurationSection("stonecrusher")));
        
    }
    
    @Override
    public void onDisable() {
        features.forEach((a, b) -> b.save());
    }
    
    public Economy getEconomy() {
        return econ;
    }
    
    public Permission getPermissions() {
        return perms;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent event) {
        Optional<Feature> feature = features.values().stream().filter(a -> a.isFeatureItem(event.getItemInHand())).findFirst();
        
        if (!feature.isPresent())
            return;
        
        if (!feature.get().checkFeatureLimit(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You've reached your limit for this feature.");
            event.setCancelled(true);
        }
        
        if (!feature.get().checkEnvironment(event.getBlock())) {
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "This location isn't suited for this feature. Make sure you have enough space.");
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlaceFinal(BlockPlaceEvent event) {
        Optional<Feature> feature = features.values().stream().filter(a -> a.isFeatureItem(event.getItemInHand())).findFirst();
        
        if (!feature.isPresent())
            return;
        
        feature.get().createInstance(event.getPlayer(), event.getBlock());
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    
    private void registerFeature(String name, Feature feature) {
        features.put(name, feature);
        getCommand(name).setExecutor(feature.getCommandHandler());
    }

    public Feature getFeature(String string) {
        return features.get(string);
    }
    
}
