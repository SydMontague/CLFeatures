package de.craftlancer.clfeatures.portal.addressbook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class AddressBookUtils {
    private AddressBookUtils() {
    }
    
    private static final List<Integer> ADDRESSBOOK_MODEL_DATA = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    private static final int DEFAULT_ADDRESSBOOK_MODEL_DATA = 1;
    
    public static boolean isAddressBook(ItemStack item) {
        if (item.getType() != Material.WRITTEN_BOOK)
            return false;
        
        BookMeta meta = (BookMeta) item.getItemMeta();
        
        return meta.hasCustomModelData() && ADDRESSBOOK_MODEL_DATA.contains(meta.getCustomModelData());
    }
    
    public static Optional<String> getCurrentTarget(ItemStack item) {
        if (item.getType() != Material.WRITTEN_BOOK)
            return Optional.empty();
        
        BookMeta meta = (BookMeta) item.getItemMeta();
        
        if(!meta.hasCustomModelData() || !ADDRESSBOOK_MODEL_DATA.contains(meta.getCustomModelData()))
            return Optional.empty();
        
        return Optional.of(ChatColor.stripColor(meta.getPage(1).split("\n")[1].trim()));
    }
    
    static List<String> getAddresses(ItemStack item) {
        BookMeta meta = (BookMeta) item.getItemMeta();
        
        List<String> addresses = new ArrayList<>();
        
        for (int i = 0; i < meta.getPageCount(); i++) {
            String[] lines = meta.getPage(1 + i).split("\n");
            Collections.addAll(addresses, Arrays.copyOfRange(lines, i == 0 ? 4 : 0, lines.length));
        }
        
        addresses.replaceAll(String::trim);
        addresses.replaceAll(ChatColor::stripColor);
        return addresses;
    }
    
    public static ItemStack writeBook(ItemStack item, String currentTarget, List<String> addresses) {
        if(item == null || item.getType() != Material.WRITTEN_BOOK)
            throw new IllegalArgumentException("Given Itemstack must be WRITTEN_BOOK");
        
        List<BaseComponent[]> pages = new ArrayList<>();
        
        List<BaseComponent> page1 = new ArrayList<>();
        page1.add(new TextComponent("§lCurrent Target:§r\n"));
        page1.add(new TextComponent(ChatColor.DARK_GREEN.asBungee() + currentTarget + "\n"));
        page1.add(new TextComponent("\n"));
        page1.add(new TextComponent("§lClick to select:§r\n"));
        
        Iterator<String> itr = addresses.iterator();
        
        for (int i = 0; i < 10 && itr.hasNext(); i++) {
            String name = itr.next();
            BaseComponent comp = new TextComponent(name + "\n");
            comp.setColor(currentTarget.equalsIgnoreCase(name) ? ChatColor.DARK_GREEN.asBungee() : ChatColor.GRAY.asBungee());
            comp.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/pbook select " + name));
            page1.add(comp);
        }
        
        pages.add(page1.toArray(new BaseComponent[0]));
        
        while (itr.hasNext()) {
            List<BaseComponent> page = new ArrayList<>();
            
            for (int i = 0; i < 14 && itr.hasNext(); i++) {
                String name = itr.next();
                BaseComponent comp = new TextComponent(name + "\n");
                comp.setColor(currentTarget.equalsIgnoreCase(name) ? ChatColor.DARK_GREEN.asBungee() : ChatColor.GRAY.asBungee());
                comp.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/pbook select " + name));
                page.add(comp);
            }
            
            pages.add(page.toArray(new BaseComponent[0]));
        }
        
        BookMeta meta = (BookMeta) item.getItemMeta();
        if(!meta.hasCustomModelData() || !ADDRESSBOOK_MODEL_DATA.contains(meta.getCustomModelData()))
            meta.setCustomModelData(DEFAULT_ADDRESSBOOK_MODEL_DATA);
        meta.spigot().setPages(pages);
        item.setItemMeta(meta);
        
        return item;
    }
}
