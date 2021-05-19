package de.craftlancer.clfeatures.jukebox;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.menu.ConditionalMenu;
import de.craftlancer.core.menu.ConditionalPagedMenu;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.resourcepack.ResourcePackManager;
import de.craftlancer.core.resourcepack.TranslateSpaceFont;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.Tuple;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

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
    private EditMode mode = EditMode.NORMAL;
    private AbstractJukeboxNote clipboard;
    private boolean legacyTiming = false;
    
    public JukeboxSong(UUID uuid) {
        this.uuid = uuid;
    }
    
    public JukeboxSong(Map<String, Object> map) {
        this.legacyTiming = (boolean) map.getOrDefault("legacyTiming", false);
        this.uuid = UUID.fromString((String) map.get("uuid"));
        this.notes = (Map<Integer, List<AbstractJukeboxNote>>) map.get("notes");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("uuid", uuid.toString());
        map.put("notes", notes);
        map.put("legacyTiming", legacyTiming);
        
        return map;
    }
    
    public Map<Integer, List<AbstractJukeboxNote>> getNotes() {
        return notes;
    }
    
    public UUID getUniqueID() {
        return uuid;
    }
    
    /**
     * @returns returns true if a note was played, false if a delay is present
     */
    public boolean play(List<Player> players, int tick) {
        boolean hasNote = false;
        for (AbstractJukeboxNote note : notes.get(tick))
            if (note instanceof JukeboxNote) {
                for (Player player : players)
                    player.playNote(player.getLocation(), ((JukeboxNote) note).getInstrument(), ((JukeboxNote) note).getNote());
                hasNote = true;
            }
        return !legacyTiming && hasNote;
    }
    
    public boolean isComplete(int tick) {
        return !notes.containsKey(tick);
    }
    
    public void displayTickOverviewMenu(Player player) {
        if (tickOverviewMenu == null)
            createTickOverviewMenu();
        else
            tickOverviewMenu.setPageItems(getTickOverviewPageItems());
        
        tickOverviewMenu.display(player, ResourcePackManager.getInstance().isFullyAccepted(player) ? "resource" : "default");
    }
    
    public void displayTickOverviewMenu(Player player, int page) {
        if (tickOverviewMenu == null)
            createTickOverviewMenu();
        else
            tickOverviewMenu.setPageItems(getTickOverviewPageItems());
        
        tickOverviewMenu.display(player, page, ResourcePackManager.getInstance().isFullyAccepted(player) ? "resource" : "default");
    }
    
    private void createTickOverviewMenu() {
        this.tickOverviewMenu = new ConditionalPagedMenu(CLFeatures.getInstance(), 6, getTickOverviewPageItems(), true, true,
                Arrays.asList(new Tuple<>("default", "§8Song Overview"),
                        new Tuple<>("resource", "§f" + TranslateSpaceFont.TRANSLATE_NEGATIVE_8 + "\uE300" + TranslateSpaceFont.getSpecificAmount(-169) + "§8Song Overview")));
        
        tickOverviewMenu.setInventoryUpdateHandler(menu -> menu.fillBorders(new MenuItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("").build()), false, "default"));
        tickOverviewMenu.setInventoryCompleteUpdateHandler(list -> {
            setTools();
            ConditionalMenu menu = list.get(list.size() - 1);
            for (int i = 10; i < 45; i++) {
                if (i % 9 == 0 || (i + 1) % 9 == 0)
                    continue;
                if (menu.getMenuItem(i).values().stream().anyMatch(e -> !e.getItem().getType().isAir()))
                    continue;
                menu.set(i, new MenuItem(new ItemStack(Material.AIR))
                        .addClickAction(click -> {
                            if (mode == EditMode.INSERT || mode == EditMode.PASTE) {
                                addJukeboxNote(mode == EditMode.PASTE ? clipboard.clone() : new JukeboxNote());
                                displayTickOverviewMenu(click.getPlayer(), getNumNotesUpToTick(notes.size() - 2) / tickOverviewMenu.getItemsPerPage());
                            }
                        }, ClickType.LEFT)
                        .addClickAction(click -> {
                            if (mode == EditMode.INSERT) {
                                addJukeboxNote(new JukeboxSkipTick());
                                displayTickOverviewMenu(click.getPlayer(), getNumNotesUpToTick(notes.size() - 2) / tickOverviewMenu.getItemsPerPage());
                            }
                        }, ClickType.RIGHT));
            }
        });
        
        tickOverviewMenu.setInfoItem(new MenuItem(new ItemBuilder(Material.DIAMOND_BLOCK).setDisplayName("§6§lEdit ticks...")
                .setLore("", "§e§lLEFT CLICK §7to add a note.",
                        "§e§lRIGHT CLICK §7to add a delay.",
                        "§e§lSHIFT LEFT CLICK §7to change timing to legacy.",
                        "§e§lSHIFT RIGHT CLICK §7to clear all ticks.").build())
                .addClickAction(click -> {
                    addJukeboxNote(new JukeboxNote());
                    displayTickOverviewMenu(click.getPlayer(), getNumNotesUpToTick(notes.size() - 2) / tickOverviewMenu.getItemsPerPage());
                }, ClickType.LEFT)
                .addClickAction(click -> {
                    addJukeboxNote(new JukeboxSkipTick());
                    displayTickOverviewMenu(click.getPlayer(), getNumNotesUpToTick(notes.size() - 2) / tickOverviewMenu.getItemsPerPage());
                }, ClickType.RIGHT)
                .addClickAction(click -> {
                    legacyTiming = !legacyTiming;
                    displayTickOverviewMenu(click.getPlayer(), 0);
                })
                .addClickAction(click -> {
                    notes.clear();
                    displayTickOverviewMenu(click.getPlayer(), getNumNotesUpToTick(notes.size() - 2) / tickOverviewMenu.getItemsPerPage());
                }, ClickType.SHIFT_RIGHT));
    }
    
    private void setTools() {
        MenuItem normalButton = new MenuItem(new ItemBuilder(Material.IRON_BLOCK).setCustomModelData(1).setDisplayName("§6§lEDIT MODE: §e§lNORMAL")
                .setEnchantmentGlow(mode == EditMode.NORMAL).build());
        MenuItem copyButton = new MenuItem(new ItemBuilder(Material.IRON_BLOCK).setCustomModelData(2).setDisplayName("§6§lEDIT MODE: §e§lCOPY")
                .setEnchantmentGlow(mode == EditMode.COPY).build());
        MenuItem pasteButton = new MenuItem(new ItemBuilder(Material.IRON_BLOCK).setCustomModelData(3).setDisplayName("§6§lEDIT MODE: §e§lPASTE")
                .setEnchantmentGlow(mode == EditMode.PASTE).build());
        MenuItem insertButton = new MenuItem(new ItemBuilder(Material.IRON_BLOCK).setCustomModelData(4).setDisplayName("§6§lEDIT MODE: §e§lINSERT")
                .setEnchantmentGlow(mode == EditMode.INSERT).build());
        
        normalButton.addClickAction(click -> {
            mode = EditMode.NORMAL;
            click.getPlayer().playSound(click.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1F);
            setTools();
        });
        
        copyButton.addClickAction(click -> {
            mode = EditMode.COPY;
            click.getPlayer().playSound(click.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1F);
            setTools();
        });
        
        pasteButton.addClickAction(click -> {
            if (clipboard == null) {
                click.getPlayer().sendMessage(CLFeatures.CC_PREFIX + "You must first copy a note.");
                return;
            }
            mode = EditMode.PASTE;
            click.getPlayer().playSound(click.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1F);
            setTools();
        });
        
        insertButton.addClickAction(click -> {
            mode = EditMode.INSERT;
            click.getPlayer().playSound(click.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1F);
            setTools();
        });
        
        tickOverviewMenu.getInventories().forEach(menu -> {
            menu.set(17, normalButton);
            menu.set(26, copyButton);
            menu.set(35, pasteButton);
            menu.set(44, insertButton);
        });
    }
    
    private List<MenuItem> getTickOverviewPageItems() {
        List<MenuItem> items = new ArrayList<>();
        
        notes.forEach((tick, list) -> list.forEach(note -> {
            boolean isJukeboxNote = note instanceof JukeboxNote;
            MenuItem item = new MenuItem(new ItemBuilder(isJukeboxNote ? Material.NOTE_BLOCK : Material.REPEATER)
                    .setDisplayName(isJukeboxNote ? "§6§lJukebox Note" : "§6§lSkip Tick")
                    .setLore("",
                            "§7§oIf mode is §f§lNORMAL...",
                            isJukeboxNote ? "§e§lLEFT CLICK §7to edit" : "§e§lSHIFT RIGHT CLICK §7to remove",
                            isJukeboxNote ? "§e§lSHIFT RIGHT CLICK §7to remove" : "",
                            "",
                            "§7§oIf mode is §f§lCOPY...",
                            "§e§lLEFT CLICK §7to copy",
                            "",
                            "§7§oIf mode is §f§lPASTE...",
                            "§e§lLEFT CLICK §7to paste",
                            "",
                            "§7§oIf mode is §f§lINSERT...",
                            "§e§lLEFT CLICK §7to insert jukebox note",
                            "§e§lRIGHT CLICK §7to insert delay")
                    .setCustomModelData(isJukeboxNote ? ((JukeboxNote) note).getNoteNumber() + 1 : 0)
                    .build());
            
            item.addClickAction(click -> {
                if (isJukeboxNote) {
                    if (mode == EditMode.NORMAL)
                        ((JukeboxNote) note).display(click.getPlayer(), this, getNumNotesUpToTick(tick) / tickOverviewMenu.getItemsPerPage());
                }
                if (mode == EditMode.COPY) {
                    clipboard = note;
                    click.getPlayer().playSound(click.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1F);
                }
                
                
                if (mode == EditMode.PASTE) {
                    insertNote(clipboard, tick);
                    displayTickOverviewMenu(click.getPlayer(), getNumNotesUpToTick(tick) / tickOverviewMenu.getItemsPerPage());
                }
                if (mode == EditMode.INSERT) {
                    insertNote(new JukeboxNote(), tick);
                    displayTickOverviewMenu(click.getPlayer(), getNumNotesUpToTick(tick) / tickOverviewMenu.getItemsPerPage());
                }
            }, ClickType.LEFT);
            
            item.addClickAction(click -> {
                if (mode == EditMode.INSERT)
                    insertNote(new JukeboxSkipTick(), tick);
                
                displayTickOverviewMenu(click.getPlayer(), getNumNotesUpToTick(tick) / tickOverviewMenu.getItemsPerPage());
            }, ClickType.RIGHT);
            
            item.addClickAction(click -> {
                if (mode != EditMode.NORMAL)
                    return;
                
                removeNote(note, tick);
                
                displayTickOverviewMenu(click.getPlayer(), getNumNotesUpToTick(tick) / tickOverviewMenu.getItemsPerPage());
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
        
        if (list.isEmpty())
            for (int i = tick; i < notes.size(); i++) {
                if (i == notes.size() - 1)
                    notes.remove(i);
                else
                    notes.put(i, notes.get(i + 1));
            }
        
    }
    
    private void insertNote(AbstractJukeboxNote note, int tick) {
        List<AbstractJukeboxNote> list = notes.get(tick);
        
        if (list.get(0) instanceof JukeboxNote && note instanceof JukeboxNote)
            list.add(note);
        else if (list.get(0) instanceof JukeboxNote && note instanceof JukeboxSkipTick) {
            shift(tick);
            List<AbstractJukeboxNote> temp = new ArrayList<>();
            temp.add(note);
            notes.put(tick, temp);
        } else if (list.get(0) instanceof JukeboxSkipTick && note instanceof JukeboxNote) {
            if (tick - 1 < 0) {
                shift(tick);
                List<AbstractJukeboxNote> temp = new ArrayList<>();
                temp.add(note);
                notes.put(tick, temp);
            } else {
                List<AbstractJukeboxNote> compare = notes.get(tick - 1);
                if (compare.get(0) instanceof JukeboxNote)
                    compare.add(note);
                else {
                    shift(tick);
                    List<AbstractJukeboxNote> temp = new ArrayList<>();
                    temp.add(note);
                    notes.put(tick, temp);
                }
            }
        } else if (list.get(0) instanceof JukeboxSkipTick && note instanceof JukeboxSkipTick) {
            shift(tick);
            List<AbstractJukeboxNote> temp = new ArrayList<>();
            temp.add(note);
            notes.put(tick, temp);
        }
        
    }
    
    private void shift(int startTick) {
        for (int i = notes.size(); i > startTick; i--) {
            notes.put(i, notes.get(i - 1));
        }
    }
    
    private int getNumNotesUpToTick(int tick) {
        int counter = 0;
        for (int i = 0; i < tick; i++) {
            if (!notes.containsKey(tick))
                break;
            if (i == tick - 1)
                counter += 1;
            else
                counter += notes.get(i).size();
        }
        return counter;
    }
    
    
    private enum EditMode {
        NORMAL,
        COPY,
        PASTE,
        INSERT
    }
}
