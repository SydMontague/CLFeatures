package de.craftlancer.clfeatures.jukebox;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.menu.ConditionalMenu;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.resourcepack.ResourcePackManager;
import de.craftlancer.core.resourcepack.TranslateSpaceFont;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.Tuple;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JukeboxNote implements AbstractJukeboxNote {
    
    private static final ItemStack RETURN_BUTTON = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
            .setDisplayName("§6§lGo back").setCustomModelData(100)
            .setLore("", "§7Click to go to previous page.").build();
    
    private int note;
    private Instrument instrument;
    private ConditionalMenu menu;
    
    private int instrumentIndex = 0;
    
    public JukeboxNote() {
        this.instrument = Instrument.PIANO;
        this.note = 0;
    }
    
    public JukeboxNote(Map<String, Object> map) {
        this.note = (int) map.get("note");
        this.instrument = Instrument.valueOf((String) map.get("instrument"));
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("note", note);
        map.put("instrument", instrument.name());
        
        return map;
    }
    
    public int getNoteNumber() {
        return note;
    }
    
    public Note getNote() {
        return new Note(note);
    }
    
    public Instrument getInstrument() {
        return instrument;
    }
    
    public void display(Player player, JukeboxSong song, int page) {
        if (menu == null)
            createMenu(song, page);
        
        instrumentIndex = 0;
        
        player.openInventory(menu.getMenu(ResourcePackManager.getInstance().isFullyAccepted(player) ? "resource" : "default").getInventory());
    }
    
    @Override
    public JukeboxNote clone() {
        JukeboxNote note = new JukeboxNote();
        note.setNote(this.note);
        note.setInstrument(this.instrument);
        return note;
    }
    
    private void createMenu(JukeboxSong song, int page) {
        menu = new ConditionalMenu(CLFeatures.getInstance(), 3, Arrays.asList(new Tuple<>("default", "§8Note Editor"),
                new Tuple<>("resource", TranslateSpaceFont.TRANSLATE_NEGATIVE_8 + "§f\uE303" +
                        TranslateSpaceFont.getSpecificAmount(-169) + "§8Note Editor")));
        
        menu.fill(new MenuItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("").build()), true, "default");
        
        menu.set(12,
                new MenuItem(getInstrumentButton())
                        .addClickAction(click -> {
                            Instrument i = Instrument.values()[instrumentIndex];
                            setInstrument(i);
                            menu.replace(12, getInstrumentButton());
                            click.getPlayer().playNote(click.getPlayer().getLocation(), i, getNote());
                            
                            instrumentIndex = instrumentIndex + 1 > 15 ? 0 : instrumentIndex + 1;
                        }, ClickType.RIGHT)
                        .addClickAction(click -> {
                            Instrument i = Instrument.values()[instrumentIndex];
                            setInstrument(i);
                            menu.replace(12, getInstrumentButton());
                            click.getPlayer().playNote(click.getPlayer().getLocation(), i, getNote());
                            
                            instrumentIndex = instrumentIndex - 1 < 0 ? 15 : instrumentIndex - 1;
                        }, ClickType.LEFT));
        
        menu.set(13, new MenuItem(RETURN_BUTTON.clone()).addClickAction(click -> song.displayTickOverviewMenu(click.getPlayer(), page)));
        
        menu.set(14,
                new MenuItem(getNoteButton())
                        .addClickAction(click -> {
                            note = note + 1 > 24 ? 0 : note + 1;
                            menu.replace(14, getNoteButton());
                            
                            click.getPlayer().playNote(click.getPlayer().getLocation(), instrument, getNote());
                        }, ClickType.RIGHT)
                        .addClickAction(click -> {
                            note = note - 1 < 0 ? 24 : note - 1;
                            menu.replace(14, getNoteButton());
                            
                            click.getPlayer().playNote(click.getPlayer().getLocation(), instrument, getNote());
                        }, ClickType.LEFT));
        
        
    }
    
    private ItemStack getNoteButton() {
        return new ItemBuilder(Material.FEATHER).setDisplayName("§6§lChange note...")
                .setLore("", "§e§lLEFT CLICK §7to decrease.", "§e§lRIGHT CLICK §7to increase.", "", "§7Current: §e" + note).build();
    }
    
    private ItemStack getInstrumentButton() {
        return new ItemBuilder(getByInstrument(instrument)).setDisplayName("§6§lChange instrument...")
                .setLore("", "§e§lLEFT CLICK §7to decrease.", "§e§lRIGHT CLICK §7to increase.", "", "§7Current instrument: §e" + instrument.name().replace("_", " ").toLowerCase()).build();
    }
    
    private static Material getByInstrument(Instrument instrument) {
        switch (instrument) {
            case BANJO:
                return Material.HAY_BLOCK;
            case BASS_DRUM:
                return Material.GRAY_CONCRETE;
            case BASS_GUITAR:
                return Material.OAK_LOG;
            case BELL:
                return Material.GOLD_BLOCK;
            case BIT:
                return Material.EMERALD_BLOCK;
            case CHIME:
                return Material.PACKED_ICE;
            case COW_BELL:
                return Material.SOUL_SAND;
            case DIDGERIDOO:
                return Material.PUMPKIN;
            case FLUTE:
                return Material.BLUE_TERRACOTTA;
            case GUITAR:
                return Material.WHITE_WOOL;
            case IRON_XYLOPHONE:
                return Material.IRON_BLOCK;
            case PLING:
                return Material.GLOWSTONE;
            case SNARE_DRUM:
                return Material.SAND;
            case STICKS:
                return Material.GLASS;
            case XYLOPHONE:
                return Material.BONE_BLOCK;
            default:
                return Material.BARRIER;
        }
    }
    
    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }
    
    public void setNote(int note) {
        this.note = note;
    }
}
