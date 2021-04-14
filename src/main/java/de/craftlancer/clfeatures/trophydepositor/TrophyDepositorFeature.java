package de.craftlancer.clfeatures.trophydepositor;

import de.craftlancer.clfeatures.BlueprintFeature;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.trophydepositor.command.TrophyDepositorCommandHandler;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPostPasteEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
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
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TrophyDepositorFeature extends BlueprintFeature<TrophyDepositorFeatureInstance> {
    private List<TrophyDepositorFeatureInstance> instances;
    private List<TrophyDepositorBoost> boosts;
    private Map<UUID, TrophyEntry> playerLookupTable;
    
    private Map<ItemStack, Integer> trophies;
    // TODO trophy collections for extra points
    
    public TrophyDepositorFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "trophyDepositor.limit"));
        
        trophies = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "trophyItems.yml")).getMapList("trophyItems").stream()
                .collect(Collectors.toMap(a -> (ItemStack) a.get("item"), a -> (Integer) a.get("value")));
        
        YamlConfiguration trophyDepositorConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/trophyDepositor.yml"));
        
        instances = (List<TrophyDepositorFeatureInstance>) trophyDepositorConfig.getList("trophies", new ArrayList<>());
        playerLookupTable = ((List<TrophyEntry>) trophyDepositorConfig.getList("trophyEntries", new ArrayList<>()))
                .stream().collect(Collectors.toMap(TrophyEntry::getPlayer, e -> e));
        boosts = (List<TrophyDepositorBoost>) trophyDepositorConfig.getList("boosts", new ArrayList<>());
    }
    
    @Override
    public boolean createInstance(Player creator, BlueprintPostPasteEvent e) {
        TrophyDepositorFeatureInstance instance = new TrophyDepositorFeatureInstance(this, creator.getUniqueId(), new BlockStructure(e.getBlocksPasted()),
                e.getFeatureLocation(), e.getSchematic());
        
        return instances.add(instance);
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/trophyDepositor.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("trophies", instances);
        config.set("trophyEntries", new ArrayList<>(playerLookupTable.values()));
        config.set("boosts", boosts);
        
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
        return new TrophyDepositorCommandHandler(getPlugin(), this);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        if (instance instanceof TrophyDepositorFeatureInstance)
            instances.remove(instance);
    }
    
    @Override
    public String getName() {
        return "TrophyDepositor";
    }
    
    public void addBoost(double boost, int size) {
        Optional<TrophyDepositorBoost> optional = boosts.stream().filter(b -> b.getBoost() == boost).findFirst();
        
        if (optional.isPresent())
            optional.get().addTrophiesLeft(size);
        else
            boosts.add(new TrophyDepositorBoost(boost, size));
    }
    
    public double getBaseItemValue(ItemStack a) {
        ItemStack e = a.clone();
        e.setAmount(1);
        return trophies.getOrDefault(e, 0) * a.getAmount();
    }
    
    private double getBoostedItemValue(ItemStack a) {
        if (a == null || a.getType().isAir())
            return 0;
        
        ItemStack e = a.clone();
        e.setAmount(1);
        
        int originalValue = trophies.getOrDefault(e, 0);
        double boostedValue = 0;
        
        for (int i = 0; i < a.getAmount(); i++)
            for (TrophyDepositorBoost boost : boosts)
                if (boost.getTrophiesLeft() > 0)
                    boostedValue += boost.apply(originalValue);
        
        boosts.removeIf(b -> b.getTrophiesLeft() <= 0);
        
        return a.getAmount() * originalValue + boostedValue;
    }
    
    public boolean addTrophyItem(ItemStack item, int value) {
        return trophies.put(item, value) != null;
    }
    
    public Map<ItemStack, Integer> getTrophyItems() {
        return Collections.unmodifiableMap(trophies);
    }
    
    public List<TrophyDepositorBoost> getBoosts() {
        return boosts;
    }
    
    public void clearScores() {
        playerLookupTable.values().forEach(v -> v.setScore(0));
    }
    
    public boolean removeTrophyByHash(int hash) {
        return trophies.entrySet().removeIf(a -> a.getKey().hashCode() == hash);
    }
    
    public double deposit(UUID uuid, ItemStack item) {
        TrophyEntry entry;
        if (playerLookupTable.containsKey(uuid))
            entry = playerLookupTable.get(uuid);
        else {
            entry = new TrophyEntry(uuid);
            playerLookupTable.put(uuid, entry);
        }
        
        double value = getBoostedItemValue(item);
        entry.deposit(value);
        return value;
    }
    
    public void setScore(UUID uuid, double score) {
        playerLookupTable.get(uuid).setScore(score);
    }
    
    public double getScore(OfflinePlayer player) {
        return getScore(player.getUniqueId());
    }
    
    public double getScore(UUID uuid) {
        return playerLookupTable.containsKey(uuid) ? playerLookupTable.get(uuid).getScore() : 0;
    }
    
    @Override
    public List<TrophyDepositorFeatureInstance> getFeatures() {
        return instances;
    }
    
    public static class TrophyEntry implements ConfigurationSerializable {
        private UUID player;
        private double score;
        
        public TrophyEntry(UUID player) {
            this.player = player;
            this.score = 0;
        }
        
        public TrophyEntry(Map<String, Object> map) {
            this.player = UUID.fromString((String) map.get("player"));
            this.score = (double) map.get("score");
        }
        
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            
            map.put("player", player.toString());
            map.put("score", score);
            
            return map;
        }
        
        public UUID getPlayer() {
            return player;
        }
        
        public double getScore() {
            return score;
        }
        
        public void setScore(double score) {
            this.score = score;
        }
        
        public void deposit(double score) {
            this.score += score;
        }
    }
}
