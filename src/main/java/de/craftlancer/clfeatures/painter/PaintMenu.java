package de.craftlancer.clfeatures.painter;

import de.craftlancer.core.menu.Menu;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.resourcepack.TranslateSpaceFont;
import de.craftlancer.core.util.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.Plugin;

public class PaintMenu extends Menu {
    
    private static final ItemStack COLOR_ITEM = new ItemBuilder(Material.LEATHER_HORSE_ARMOR).setCustomModelData(7).addItemFlag(ItemFlag.HIDE_DYE).dye(Color.WHITE).build();
    private static final ItemStack SELECTED_COLOR_ITEM = new ItemBuilder(COLOR_ITEM).setDisplayName("§aSelected Color").build();
    private static final ItemStack SELECT_COLOR_ITEM = new ItemBuilder(COLOR_ITEM).setDisplayName("§8Select Color...").addLore("", "§7Click to set color.").build();
    
    public PaintMenu(Plugin plugin) {
        super(plugin, TranslateSpaceFont.TRANSLATE_NEGATIVE_8 + "§f\uE305", 6);
        
        set(10,
                new MenuItem(new ItemStack(Material.AIR))
                        .addClickAction(click -> {
                            ItemStack cursor = click.getCursor();
                            MenuItem item = click.getItem();
                            
                            if (cursor == null || (cursor.getType() != Material.AIR && !(cursor.getItemMeta() instanceof LeatherArmorMeta)))
                                return;
                            
                            replace(10, cursor.clone());
                            click.getPlayer().setItemOnCursor(item.getItem().clone());
                            if (getMenuItem(10).getItem().getType() == Material.AIR)
                                replace(37, new ItemStack(Material.AIR));
                        }));
        
        set(12, new MenuItem(SELECTED_COLOR_ITEM));
        
        //defaults
        set(5, new MenuItem(new ItemBuilder(SELECT_COLOR_ITEM).dye(Color.YELLOW).build()).addClickAction(c -> setColor(Color.YELLOW)));
        set(6, new MenuItem(new ItemBuilder(SELECT_COLOR_ITEM).dye(Color.ORANGE).build()).addClickAction(c -> setColor(Color.ORANGE)));
        set(7, new MenuItem(new ItemBuilder(SELECT_COLOR_ITEM).dye(Color.RED).build()).addClickAction(c -> setColor(Color.RED)));
        set(14, new MenuItem(new ItemBuilder(SELECT_COLOR_ITEM).dye(Color.AQUA).build()).addClickAction(c -> setColor(Color.AQUA)));
        set(15, new MenuItem(new ItemBuilder(SELECT_COLOR_ITEM).dye(Color.LIME).build()).addClickAction(c -> setColor(Color.GREEN)));
        set(16, new MenuItem(new ItemBuilder(SELECT_COLOR_ITEM).dye(Color.TEAL).build()).addClickAction(c -> setColor(Color.TEAL)));
        set(23, new MenuItem(new ItemBuilder(SELECT_COLOR_ITEM).dye(Color.fromRGB(65, 105, 225)).build()).addClickAction(c -> setColor(Color.fromRGB(65, 105, 225))));
        set(24, new MenuItem(new ItemBuilder(SELECT_COLOR_ITEM).dye(Color.PURPLE).build()).addClickAction(c -> setColor(Color.PURPLE)));
        set(25, new MenuItem(new ItemBuilder(SELECT_COLOR_ITEM).dye(Color.WHITE).build()).addClickAction(c -> setColor(Color.WHITE)));
        
        setPalette(Color.WHITE);
        
        set(37,
                new MenuItem(new ItemStack(Material.AIR))
                        .addClickAction(click -> {
                            ItemStack cursor = click.getCursor();
                            MenuItem item = click.getItem();
                            
                            if (cursor != null && cursor.getType().isAir())
                                return;
                            
                            if (getMenuItem(10).getItem().getType().isAir())
                                return;
                            
                            replace(10, new ItemStack(Material.AIR));
                            replace(37, new ItemStack(Material.AIR));
                            click.getPlayer().setItemOnCursor(item.getItem().clone());
                        }));
    }
    
    private void setPalette(Color color) {
        HSLColor hsl = new HSLColor(new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue()));
        
        java.awt.Color prev;
        for (int i = 39; i < 45; i++) {
            prev = hsl.adjustShade(15);
            hsl = new HSLColor(prev);
            Color bukkitColor = Color.fromRGB(prev.getRed(), prev.getGreen(), prev.getBlue());
            set(i, new MenuItem(new ItemBuilder(SELECT_COLOR_ITEM).dye(bukkitColor).build()).addClickAction(c -> setColor(bukkitColor)));
        }
        
        prev = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
        hsl = new HSLColor(prev);
        for (int i = 48; i < 54; i++) {
            prev = hsl.adjustTone(10);
            hsl = new HSLColor(prev);
            Color bukkitColor = Color.fromRGB(prev.getRed(), prev.getGreen(), prev.getBlue());
            set(i, new MenuItem(new ItemBuilder(SELECT_COLOR_ITEM).dye(bukkitColor).build()).addClickAction(c -> setColor(bukkitColor)));
        }
    }
    
    private void setColor(Color color) {
        if (!getInventory().getViewers().isEmpty()) {
            Player player = (Player) getInventory().getViewers().get(0);
            player.playSound(player.getLocation(), Sound.ENTITY_TURTLE_EGG_HATCH, 0.5F, 1F);
        }
        
        replace(12, new ItemBuilder(SELECTED_COLOR_ITEM).dye(color).build());
        setPalette(color);
        if (getMenuItem(10).getItem().getType() != Material.AIR)
            replace(37, new ItemBuilder(getMenuItem(10).getItem()).dye(color).addItemFlag(ItemFlag.HIDE_DYE).build());
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() != getInventory())
            return;
        
        MenuItem item = getMenuItem(10);
        
        if (item.getItem().getType() != Material.AIR)
            event.getPlayer().getInventory().addItem(item.getItem().clone()).forEach((i, e) -> event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), e));
    }
}
