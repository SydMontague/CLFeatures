package de.craftlancer.clfeatures.jukebox;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.menu.ConditionalPagedMenu;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.resourcepack.ResourcePackManager;
import de.craftlancer.core.resourcepack.TranslateSpaceFont;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.Tuple;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JukeboxSong implements ConfigurationSerializable {
    
    // tick -> notes (every 20 ticks per second, 1 repeater tick = 2 MC ticks)
    private Map<Integer, List<AbstractJukeboxNote>> notes = new HashMap<>();
    private ConditionalPagedMenu tickOverviewMenu;
    private UUID uuid;
    
    public JukeboxSong(UUID uuid) {
        this.uuid = uuid;
    }
    
    public JukeboxSong(Map<String, Object> map) {
        
        this.uuid = UUID.fromString((String) map.get("uuid"));
        
        map.remove("uuid");
        map.remove("==");
        
        for (Map.Entry<String, Object> entry : map.entrySet())
            notes.put(Integer.parseInt(entry.getKey()), (List<AbstractJukeboxNote>) entry.getValue());
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("uuid", uuid.toString());
        notes.forEach((tick, list) -> map.put(String.valueOf(tick), list));
        
        return map;
    }
    
    public Map<Integer, List<AbstractJukeboxNote>> getNotes() {
        return notes;
    }
    
    public UUID getUniqueID() {
        return uuid;
    }
    
    /**
     * @return returns the next tick to be played
     */
    public void play(List<Player> players, int tick) {
        for (AbstractJukeboxNote note : notes.get(tick))
            if (note instanceof JukeboxNote)
                for (Player player : players)
                    player.playNote(player.getLocation(), ((JukeboxNote) note).getInstrument(), ((JukeboxNote) note).getNote());
    }
    
    public boolean isComplete(int tick) {
        return !notes.containsKey(tick);
    }
    
    public void displayTickOverviewMenu(Player player) {
        if (tickOverviewMenu == null)
            createTickOverviewMenu();
        
        tickOverviewMenu.display(player, ResourcePackManager.getInstance().isFullyAccepted(player) ? "resource" : "default");
    }
    
    public void displayTickOverviewMenu(Player player, int page) {
        if (tickOverviewMenu == null)
            createTickOverviewMenu();
        
        tickOverviewMenu.display(player, page, ResourcePackManager.getInstance().isFullyAccepted(player) ? "resource" : "default");
    }
    
    private void createTickOverviewMenu() {
        this.tickOverviewMenu = new ConditionalPagedMenu(CLFeatures.getInstance(), 6, getTickOverviewPageItems(), true, true,
                Arrays.asList(new Tuple<>("default", "§8Song Overview"),
                        new Tuple<>("resource", "§f" + TranslateSpaceFont.TRANSLATE_NEGATIVE_8 + "\uE300" + TranslateSpaceFont.getSpecificAmount(-169) + "§8Song Overview")));
        
        tickOverviewMenu.setInventoryUpdateHandler(menu -> menu.fillBorders(new MenuItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("").build()), true, "default"));
        
        tickOverviewMenu.setInfoItem(new MenuItem(new ItemBuilder(Material.DIAMOND_BLOCK).setDisplayName("§6§lEdit ticks...")
                .setLore("", "§e§lLEFT CLICK §7to add a note.", "§e§lRIGHT CLICK §7to add a delay.", "§e§lSHIFT RIGHT CLICK §7to clear all ticks.").build())
                .addClickAction(click -> {
                    addJukeboxNote(new JukeboxNote());
                    tickOverviewMenu.setPageItems(getTickOverviewPageItems());
                    displayTickOverviewMenu(click.getPlayer(), notes.size() / tickOverviewMenu.getItemsPerPage());
                }, ClickType.LEFT)
                .addClickAction(click -> {
                    addJukeboxNote(new JukeboxSkipTick());
                    tickOverviewMenu.setPageItems(getTickOverviewPageItems());
                    displayTickOverviewMenu(click.getPlayer(), notes.size() / tickOverviewMenu.getItemsPerPage());
                }, ClickType.RIGHT)
                .addClickAction(click -> {
                    notes.clear();
                    tickOverviewMenu.setPageItems(getTickOverviewPageItems());
                    displayTickOverviewMenu(click.getPlayer(), notes.size() / tickOverviewMenu.getItemsPerPage());
                }, ClickType.SHIFT_RIGHT));
    }
    
    private List<MenuItem> getTickOverviewPageItems() {
        List<MenuItem> items = new ArrayList<>();
        
        notes.forEach((tick, list) -> list.forEach(note -> {
            boolean isJukeboxNote = note instanceof JukeboxNote;
            MenuItem item = new MenuItem(new ItemBuilder(isJukeboxNote ? Material.NOTE_BLOCK : Material.REPEATER)
                    .setDisplayName(isJukeboxNote ? "§6§lJukebox Note" : "§6§lSkip Tick")
                    .setLore("", isJukeboxNote ? "§e§lLEFT CLICK §7to edit" : "§e§lSHIFT RIGHT CLICK §7to remove",
                            isJukeboxNote ? "§e§lSHIFT RIGHT CLICK §7to remove" : "")
                    .build());
            
            if (isJukeboxNote)
                item.addClickAction(click -> ((JukeboxNote) note).display(click.getPlayer(), this, tick / tickOverviewMenu.getItemsPerPage()), ClickType.LEFT);
            
            item.addClickAction(click -> {
                removeNote(note, tick);
                
                tickOverviewMenu.setPageItems(getTickOverviewPageItems());
                displayTickOverviewMenu(click.getPlayer());
            }, ClickType.SHIFT_RIGHT);
            
            items.add(item);
        }));
        
        return items;
    }
    
    private void addJukeboxNote(AbstractJukeboxNote note) {
        if (notes.size() == 0)
            notes.put(0, new ArrayList<>(Collections.singletonList(note)));
        else {
            List<AbstractJukeboxNote> list = notes.get(notes.size() - 1);
            
            if (note instanceof JukeboxSkipTick) {
                if (list.isEmpty())
                    list.add(note);
                else
                    notes.put(notes.size(), new ArrayList<>(Collections.singletonList(note)));
            } else {
                if (list.isEmpty() || list.get(0) instanceof JukeboxNote)
                    list.add(note);
                else
                    notes.put(notes.size(), new ArrayList<>(Collections.singletonList(note)));
            }
        }
        
    }
    
    private void removeNote(AbstractJukeboxNote note, int tick) {
        List<AbstractJukeboxNote> list = notes.get(tick);
        
        list.remove(note);
        
        if (list.size() == 0)
            for (int i = tick; i < notes.size(); i++) {
                if (i == notes.size() - 1)
                    notes.remove(i);
                else
                    notes.put(i, notes.get(i + 1));
            }
        
    }
}
