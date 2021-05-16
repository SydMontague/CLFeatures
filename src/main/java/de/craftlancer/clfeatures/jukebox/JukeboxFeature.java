package de.craftlancer.clfeatures.jukebox;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.ItemFrameFeature;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.structure.BlockStructure;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPostPasteEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class JukeboxFeature extends ItemFrameFeature<JukeboxFeatureInstance> {
    
    private List<JukeboxFeatureInstance> instances;
    
    // Item persistent data UUID -> song
    private Map<UUID, JukeboxSong> songs;
    protected static NamespacedKey SONG_KEY;
    
    static {
        ConfigurationSerialization.registerClass(JukeboxSkipTick.class);
        ConfigurationSerialization.registerClass(AbstractJukeboxNote.class);
        ConfigurationSerialization.registerClass(JukeboxNote.class);
        ConfigurationSerialization.registerClass(JukeboxSong.class);
    }
    
    public JukeboxFeature(CLFeatures plugin, ConfigurationSection config) {
        super(plugin, config, new NamespacedKey(plugin, "jukebox.limit"));
        
        SONG_KEY = new NamespacedKey(plugin, "jukebox_feature_song");
    }
    
    @Override
    public long getTickFrequency() {
        return 2;
    }
    
    @Override
    public boolean createInstance(Player creator, BlueprintPostPasteEvent event) {
        return instances.add(new JukeboxFeatureInstance(creator.getUniqueId(),
                new BlockStructure(event.getBlocksPasted()),
                event.getFeatureLocation(), event.getSchematic(), event.getPastedEntities()));
    }
    
    @Override
    protected void deserialize(Configuration config) {
        instances = (List<JukeboxFeatureInstance>) config.getList("jukebox", new ArrayList<>());
        songs = ((List<JukeboxSong>) config.get("songs", new ArrayList<>())).stream().collect(Collectors.toMap(JukeboxSong::getUniqueID, a -> a));
    }
    
    @Override
    protected Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("jukebox", instances);
        map.put("songs", new ArrayList<>(songs.values()));
        return map;
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
    public String getName() {
        return "Jukebox";
    }
    
    @Override
    public List<JukeboxFeatureInstance> getFeatures() {
        return instances;
    }
    
    public boolean isSongItem(ItemStack item) {
        return item != null &&
                item.hasItemMeta() &&
                item.getItemMeta().getPersistentDataContainer().has(SONG_KEY, PersistentDataType.STRING);
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
