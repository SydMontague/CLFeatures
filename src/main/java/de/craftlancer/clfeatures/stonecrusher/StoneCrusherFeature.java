package de.craftlancer.clfeatures.stonecrusher;

import de.craftlancer.clfeatures.BlueprintFeature;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPostPasteEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class StoneCrusherFeature extends BlueprintFeature<StoneCrusherFeatureInstance> {
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
    public boolean createInstance(Player creator, BlueprintPostPasteEvent e) {
        return instances.add(new StoneCrusherFeatureInstance(this, creator.getUniqueId(),
                new BlockStructure(e.getBlocksPasted()), e.getFeatureLocation(), e.getSchematic()));
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
