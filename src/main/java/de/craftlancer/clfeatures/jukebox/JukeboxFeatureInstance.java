package de.craftlancer.clfeatures.jukebox;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.ItemFrameFeatureInstance;
import de.craftlancer.core.Utils;
import de.craftlancer.core.menu.ConditionalMenu;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.resourcepack.ResourcePackManager;
import de.craftlancer.core.resourcepack.TranslateSpaceFont;
import de.craftlancer.core.structure.BlockStructure;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.Tuple;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class JukeboxFeatureInstance extends ItemFrameFeatureInstance {
    
    public static final String MOVE_METADATA = "jukeboxMove";
    
    private static final ItemStack PLAY_BUTTON = new ItemBuilder(Material.EMERALD_BLOCK)
            .setDisplayName("§a§lPlay Song").setLore("", "§7Click to play song.").build();
    private static final ItemStack PAUSE_BUTTON = new ItemBuilder(Material.REDSTONE_BLOCK)
            .setDisplayName("§c§lPause Song").setLore("", "§7Click to pause song.").build();
    private static final ItemStack TURN_REPEAT_OFF_BUTTON = new ItemBuilder(Material.DIAMOND_BLOCK)
            .setDisplayName("§b§lRepeat Enabled").setLore("", "§7Click to §cdisable §7repeat mode.").build();
    private static final ItemStack TURN_REPEAT_ON_BUTTON = new ItemBuilder(Material.LIGHT_GRAY_CONCRETE)
            .setDisplayName("§8§lRepeat Disabled").setLore("", "§7Click to §benable §7repeat mode.").build();
    
    private JukeboxFeature manager;
    private ConditionalMenu menu;
    
    private ItemStack songItem;
    private int songTick = 0;
    private boolean paused;
    private boolean repeat;
    private boolean hasPlayed;
    
    public JukeboxFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, String usedSchematic, List<Entity> entities) {
        super(ownerId, blocks, location, usedSchematic, entities);
        
        this.songItem = new ItemStack(Material.AIR);
    }
    
    public JukeboxFeatureInstance(Map<String, Object> map) {
        super(map);
        
        this.songItem = (ItemStack) map.get("songItem");
        this.paused = (boolean) map.get("paused");
        this.repeat = (boolean) map.get("repeat");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("songItem", getSongItem());
        map.put("paused", paused);
        map.put("repeat", repeat);
        
        return map;
    }
    
    @Override
    protected void tick() {
        
        if (isPaused())
            return;
        
        if (!isRepeat() && hasPlayed)
            return;
        
        if (songItem == null || songItem.getType() == Material.AIR)
            return;
        
        if (!Utils.isChunkLoaded(getInitialBlock()))
            return;
        
        if (!getManager().isSongItem(songItem))
            return;
        
        JukeboxSong song = getManager().getSong(songItem);
        
        if (song == null)
            return;
        
        if (song.isComplete(songTick)) {
            songTick = 0;
            hasPlayed = true;
            return;
        }
        
        List<Player> nearby = getInitialBlock().getWorld()
                .getNearbyEntities(getInitialBlock(), 32, 32, 32, e -> e instanceof Player)
                .stream().map(e -> (Player) e).collect(Collectors.toList());
        
        getInitialBlock().getWorld().spawnParticle(Particle.NOTE, getInitialBlock().add(Math.random(), 1 + Math.random(), Math.random()), 1);
        
        song.play(nearby, songTick);
        
        songTick++;
    }
    
    @Override
    protected JukeboxFeature getManager() {
        if (manager == null)
            manager = (JukeboxFeature) CLFeatures.getInstance().getFeature("jukebox");
        
        return manager;
    }
    
    private void createMenu() {
        this.menu = new ConditionalMenu(CLFeatures.getInstance(), 6,
                Arrays.asList(new Tuple<>("default", "§5Jukebox"),
                        new Tuple<>("resource", TranslateSpaceFont.TRANSLATE_NEGATIVE_8 + "§f\uE302")));
        
        menu.fill(new MenuItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("").build()), true, "default");
        
        menu.set(28,
                new MenuItem(isPaused() ? PLAY_BUTTON : PAUSE_BUTTON)
                        .addClickAction(click -> {
                            setPaused(!isPaused());
                            if (!isPaused() && hasPlayed) {
                                hasPlayed = false;
                                songTick = 0;
                            }
                            menu.replace(28, isPaused() ? PLAY_BUTTON : PAUSE_BUTTON);
                        }));
        
        menu.set(31,
                new MenuItem(getSongItem())
                        .addClickAction(click -> {
                            ItemStack cursor = click.getCursor();
                            MenuItem item = click.getItem();
                            
                            if (cursor == null || (cursor.getType() != Material.AIR && !getManager().isSongItem(cursor)))
                                return;
                            
                            setSongItem(cursor.clone());
                            menu.replace(31, cursor.clone());
                            click.getPlayer().setItemOnCursor(new ItemStack(item.getItem()));
                        }));
        
        menu.set(34,
                new MenuItem(isRepeat() ? TURN_REPEAT_OFF_BUTTON : TURN_REPEAT_ON_BUTTON)
                        .addClickAction(click -> {
                            setRepeat(!isRepeat());
                            menu.replace(34, isRepeat() ? TURN_REPEAT_OFF_BUTTON : TURN_REPEAT_ON_BUTTON);
                        }));
    }
    
    public void display(Player player) {
        if (menu == null)
            createMenu();
        
        player.openInventory(menu.getMenu(ResourcePackManager.getInstance().isFullyAccepted(player) ? "resource" : "default").getInventory());
    }
    
    public ItemStack getSongItem() {
        return songItem == null ? new ItemStack(Material.AIR) : songItem;
    }
    
    public void setSongItem(ItemStack songItem) {
        this.songItem = songItem;
        this.songTick = 0;
        this.hasPlayed = false;
    }
    
    public boolean isPaused() {
        return paused;
    }
    
    public boolean isRepeat() {
        return repeat;
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
        this.hasPlayed = false;
    }
    
    @Override
    public void interact(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            display(event.getPlayer());
            event.setCancelled(true);
        }
    }
}
