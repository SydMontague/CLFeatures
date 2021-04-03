package de.craftlancer.clfeatures.jukebox;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.ItemFrameFeature;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JukeboxFeature extends ItemFrameFeature<JukeboxFeatureInstance> {
    
    private List<JukeboxFeatureInstance> instances;
    
    // Item persistent data UUID -> song
    private Map<UUID, JukeboxSong> songs = new HashMap<>();
    protected static NamespacedKey SONG_KEY;
    
    public JukeboxFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "jukebox.limit"));
        
        ConfigurationSerialization.registerClass(JukeboxSkipTick.class);
        ConfigurationSerialization.registerClass(AbstractJukeboxNote.class);
        ConfigurationSerialization.registerClass(JukeboxNote.class);
        ConfigurationSerialization.registerClass(JukeboxSong.class);
        
        SONG_KEY = new NamespacedKey(plugin, "jukebox_feature_song");
        
        YamlConfiguration jukeboxConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data/jukebox.yml"));
        
        instances = (List<JukeboxFeatureInstance>) jukeboxConfig.getList("jukebox", new ArrayList<>());
        
        List<JukeboxSong> list = (List<JukeboxSong>) jukeboxConfig.get("songs", new ArrayList<>());
        
        list.forEach(song -> songs.put(song.getUniqueID(), song));
        
    }
    
    @Override
    public boolean isFeatureItem(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().getKeys().stream()
                .anyMatch(k -> k.getKey().equals(getPlugin().getFeatureItemKey().getKey())
                        && item.getItemMeta().getPersistentDataContainer().get(k, PersistentDataType.STRING).equals("jukebox"));
    }
    
    //Unused with blueprints
    @Override
    public List<Block> checkEnvironment(Block initialBlock) {
        return Stream.of(initialBlock.getRelative(0, 1, 0))
                .filter(block -> !block.getType().isAir()).collect(Collectors.toList());
    }
    
    @Override
    public long getTickFrequency() {
        return 1;
    }
    
    @Override
    public void onInstanceCreate(Player creator, Block initialBlock, ItemStack hand, UUID itemFrame) {
        initialBlock.getRelative(0, 1, 0).setType(Material.BARRIER);
        
        instances.add(new JukeboxFeatureInstance(creator.getUniqueId(),
                new BlockStructure(initialBlock.getLocation(), initialBlock.getLocation().add(0, 1, 0)),
                initialBlock.getLocation(), hand, itemFrame));
    }
    
    @Override
    public void save() {
        File f = new File(getPlugin().getDataFolder(), "data/jukebox.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("jukebox", instances);
        config.set("songs", new ArrayList<>(songs.values()));
        
        BukkitRunnable saveTask = new LambdaRunnable(() -> {
            try {
                config.save(f);
            } catch (IOException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Error while saving Jukebox: ", e);
            }
        });
        
        if (getPlugin().isEnabled())
            saveTask.runTaskAsynchronously(getPlugin());
        else
            saveTask.run();
    }
    
    @Override
    public CommandHandler getCommandHandler() {
        return new JukeboxCommandHandler(getPlugin(), this);
    }
    
    @Override
    public void remove(FeatureInstance instance) {
        super.remove(instance);
        if (instance instanceof JukeboxFeatureInstance)
            instances.remove(instance);
    }
    
    @Nonnull
    @Override
    protected String getName() {
        return "Jukebox";
    }
    
    @Override
    public List<JukeboxFeatureInstance> getFeatures() {
        return instances;
    }
    
    public boolean isSongItem(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().has(SONG_KEY, PersistentDataType.STRING);
    }
    
    public JukeboxSong getSong(ItemStack item) {
        UUID uuid = UUID.fromString(item.getItemMeta().getPersistentDataContainer().get(SONG_KEY, PersistentDataType.STRING));
        
        return songs.get(uuid);
    }
    
    public void createSong(ItemStack item) {
        UUID uuid = UUID.randomUUID();
        
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(SONG_KEY, PersistentDataType.STRING, uuid.toString());
        item.setItemMeta(meta);
        
        songs.put(uuid, new JukeboxSong(uuid));
    }
}
