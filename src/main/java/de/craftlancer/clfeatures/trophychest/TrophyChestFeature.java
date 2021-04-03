package de.craftlancer.clfeatures.trophychest;

import de.craftlancer.clfeatures.BlueprintFeature;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Deprecated
public class TrophyChestFeature extends BlueprintFeature<TrophyChestFeatureInstance> {
    private List<TrophyChestFeatureInstance> instances = new ArrayList<>();
    private Map<UUID, TrophyChestFeatureInstance> playerLookupTable = new HashMap<>();
    
    private Map<ItemStack, Integer> trophies = new HashMap<>();
    // TODO trophy collections for extra points
    
    public TrophyChestFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "trophyChest.limit"));
        
        instances = (List<TrophyChestFeatureInstance>) YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/trophy.yml"))
                .getList("trophies", new ArrayList<>());
        playerLookupTable = instances.stream().collect(Collectors.toMap(TrophyChestFeatureInstance::getOwnerId, a -> a));
        
        trophies = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "trophyItems.yml")).getMapList("trophyItems").stream()
                .collect(Collectors.toMap(a -> (ItemStack) a.get("item"), a -> (Integer) a.get("value")));
    }
    
    /*
     * Only one TrophyChest per player allowed at all times
     */
    @Override
    public int getLimit(Player player) {
        return 1;
    }
    
    @Override
    public boolean checkFeatureLimit(Player player) {
        return instances.stream().filter(a -> a.isOwner(player)).count() < 1;
    }
    
    @Override
    public boolean createInstance(Player creator, Block initialBlock, List<Location> blocks, String usedSchematic) {
        TrophyChestFeatureInstance instance = new TrophyChestFeatureInstance(this, creator.getUniqueId(), new BlockStructure(blocks),
                initialBlock.getLocation(), usedSchematic);
        
        if (instances.add(instance)) {
            playerLookupTable.put(creator.getUniqueId(), instance);
            return true;
        }
        return false;
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/trophy.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("trophies", instances);
        
        File trophyFile = new File(getPlugin().getDataFolder(), "trophyItems.yml");
        YamlConfiguration config2 = new YamlConfiguration();
        
        List<Map<String, Object>> mapList = new ArrayList<>();
        trophies.forEach((a, b) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("item", a);
            map.put("value", b);
            mapList.add(map);
        });
        config2.set("trophyItems", mapList);
        
        BukkitRunnable saveTask = new LambdaRunnable(() -> {
            try {
                config.save(f);
                config2.save(trophyFile);
            } catch (IOException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Error while saving Trophies: ", e);
            }
        });
        
        if (getPlugin().isEnabled())
            saveTask.runTaskAsynchronously(getPlugin());
        else
            saveTask.run();
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new TrophyCommandHandler(getPlugin(), this);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof TrophyChestFeatureInstance) {
            instances.remove(instance);
            playerLookupTable.remove(instance.getOwnerId());
        }
    }
    
    @Override
    protected String getName() {
        return "TrophyChest";
    }
    
    public int getItemValue(ItemStack a) {
        if (a == null || a.getType().isAir())
            return 0;
        
        ItemStack e = a.clone();
        e.setAmount(1);
        
        return trophies.getOrDefault(e, 0) * a.getAmount();
    }
    
    public boolean addTrophyItem(ItemStack item, int value) {
        return trophies.put(item, value) != null;
    }
    
    public Map<ItemStack, Integer> getTrophyItems() {
        return Collections.unmodifiableMap(trophies);
    }
    
    public boolean removeTrophyByHash(int hash) {
        return trophies.entrySet().removeIf(a -> a.getKey().hashCode() == hash);
    }
    
    public double getScore(OfflinePlayer player) {
        return getScore(player.getUniqueId());
    }
    
    public double getScore(UUID uuid) {
        return playerLookupTable.containsKey(uuid) ? playerLookupTable.get(uuid).getScore() : 0;
    }
    
    @Override
    public List<TrophyChestFeatureInstance> getFeatures() {
        return instances;
    }
}
