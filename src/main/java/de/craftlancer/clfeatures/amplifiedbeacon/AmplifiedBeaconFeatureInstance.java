package de.craftlancer.clfeatures.amplifiedbeacon;

import de.craftlancer.clapi.LazyService;
import de.craftlancer.clapi.clclans.PluginClans;
import de.craftlancer.clfeatures.BlueprintFeatureInstance;
import de.craftlancer.clfeatures.Feature;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Map;
import java.util.UUID;

public class AmplifiedBeaconFeatureInstance extends BlueprintFeatureInstance {
    
    private static final LazyService<PluginClans> CLANS = new LazyService<>(PluginClans.class);
    
    private PotionEffect buff1;
    private PotionEffect buff2;
    private PotionEffect debuff;
    private long expireTime;
    private long tickId = 0;
    
    public AmplifiedBeaconFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, String usedSchematic) {
        super(ownerId, blocks, location, usedSchematic);
    }
    
    public AmplifiedBeaconFeatureInstance(Map<String, Object> map) {
        super(map);
    }
    
    @Override
    protected void tick() {
        tickId += 10;
        
        if (tickId % 160 != 0)
            return;
        
        if (!isTimeExpired() && !isActive())
            return;
        
        double range = getRange();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().getWorld() != getInitialBlock().getWorld() || getHorizontalDistanceSquared(player.getLocation(), getInitialBlock()) >= range)
                continue;
            
            if (isClanMember(getOwnerId(), player.getUniqueId())) {
                if (buff1 != null)
                    player.addPotionEffect(buff1);
                if (buff2 != null)
                    player.addPotionEffect(buff2);
            }
            if (isEnemy(getOwnerId(), player.getUniqueId())) {
                if (debuff != null)
                    player.addPotionEffect(debuff);
            }
            
        }
    }
    
    private boolean isClanMember(UUID uuid, UUID playerUUID) {
        if (!CLANS.isPresent())
            return false;
        
        if (uuid.equals(playerUUID))
            return true;
        if (CLANS.get().getClan(Bukkit.getOfflinePlayer(playerUUID)) == null || CLANS.get().getClan(Bukkit.getOfflinePlayer(uuid)) == null)
            return false;
        return CLANS.get().getClan(Bukkit.getOfflinePlayer(uuid)).equals(CLANS.get().getClan(Bukkit.getOfflinePlayer(playerUUID)));
    }
    
    private boolean isEnemy(UUID uuid, UUID playerUUID) {
        PluginClans clans = Bukkit.getServicesManager().load(PluginClans.class);
        
        if (clans == null)
            return false;
        if (clans.getClan(Bukkit.getOfflinePlayer(playerUUID)) == null || clans.getClan(Bukkit.getOfflinePlayer(uuid)) == null)
            return false;
        return clans.getClan(Bukkit.getOfflinePlayer(uuid)).hasRival(clans.getClan(Bukkit.getOfflinePlayer(playerUUID)));
    }
    
    private double getHorizontalDistanceSquared(Location loc1, Location loc2) {
        return Math.pow(Math.abs(loc1.getX() - loc2.getX()), 2) + Math.pow(Math.abs(loc1.getZ() - loc2.getZ()), 2);
    }
    
    @Override
    protected Feature<FeatureInstance> getManager() {
        return null;
    }
    
    public boolean isTimeExpired() {
        return expireTime < System.currentTimeMillis();
    }
    
    public double getRange() {
        Location location = getInitialBlock();
        
        if (location.getBlock().getType() != Material.BEACON)
            return 0;
        
        Beacon beacon = (Beacon) location.getBlock();
        
        return Math.pow(20 + beacon.getTier() * 20, 2);
    }
    
    public boolean isActive() {
        
        Location beaconLocation = getInitialBlock();
        
        World world = beaconLocation.getWorld();
        if (world == null || !world.isChunkLoaded(beaconLocation.getBlockX() >> 4, beaconLocation.getBlockZ() >> 4))
            return false;
        
        if (beaconLocation.getBlock().getType() != Material.BEACON)
            return false;
        
        Beacon beacon = (Beacon) beaconLocation.getBlock().getBlockData();
        
        return beacon.getTier() > 0;
       /* for (int i = (int) y + 1; i <= 255; i++) {
            Material material = (new Location(world, x, i, z).getBlock().getType());
            if (material.isAir())
                continue;
            if (occludingPassThrough(material).equals("true"))
                continue;
            if (occludingPassThrough(material).equals("false"))
                return false;
            if (material.isOccluding())
                return false;
            
        }*/
    }
}
