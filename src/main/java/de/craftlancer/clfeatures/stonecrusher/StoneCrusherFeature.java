package de.craftlancer.clfeatures.stonecrusher;

import de.craftlancer.clfeatures.BlueprintFeature;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureCommandHandler;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPostPasteEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StoneCrusherFeature extends BlueprintFeature<StoneCrusherFeatureInstance> {
    private List<StoneCrusherFeatureInstance> instances;
    
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
            
            @SuppressWarnings("unchecked")
            List<Map<?, ?>> output = (List<Map<?, ?>>) a.get("output");
            
            if (output == null)
                return;
            
            List<StoneCrusherResult> result = output.stream().map(b -> new StoneCrusherResult((String) b.get("item"), ((Number) b.get("chance")).doubleValue()))
                    .collect(Collectors.toList());
            
            lootTable.put(input, result);
        });
        
    }
    
    @Override
    public boolean createInstance(Player creator, BlueprintPostPasteEvent e) {
        return instances.add(new StoneCrusherFeatureInstance(this, creator.getUniqueId(),
                new BlockStructure(e.getBlocksPasted()), e.getFeatureLocation(), e.getSchematic()));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void deserialize(Configuration config) {
        instances = (List<StoneCrusherFeatureInstance>) config.getList("stonecrusher", new ArrayList<>());
    }
    
    @Override
    protected Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("stonecrusher", instances);
        return map;
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new FeatureCommandHandler(getPlugin(), this);
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
    public String getName() {
        return "Stonecrusher";
    }
    
    @Override
    public List<StoneCrusherFeatureInstance> getFeatures() {
        return instances;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteractMove(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (!event.hasBlock() || !p.hasMetadata(getMoveMetaData()))
            return;
        
        Optional<StoneCrusherFeatureInstance> feature = getFeatures().stream().filter(a -> a.getStructure().containsBlock(event.getClickedBlock())).findAny();
        
        if (!feature.isPresent())
            return;
        
        if (!feature.get().getOwnerId().equals(p.getUniqueId()))
            return;
        
        feature.get().destroy();
        giveFeatureItem(p, feature.get());
        p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + "StoneCrusher successfully moved back to your inventory.");
        p.removeMetadata(getMoveMetaData(), getPlugin());
    }
}
