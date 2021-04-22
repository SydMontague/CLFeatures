package de.craftlancer.clfeatures.fragmentextractor;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.Feature;
import de.craftlancer.clfeatures.FeatureInstance;
import de.craftlancer.clfeatures.ManualPlacementFeatureInstance;
import de.craftlancer.core.CLCore;
import de.craftlancer.core.Utils;
import de.craftlancer.core.menu.ConditionalMenu;
import de.craftlancer.core.menu.Menu;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.resourcepack.ResourcePackManager;
import de.craftlancer.core.resourcepack.TranslateSpaceFont;
import de.craftlancer.core.structure.BlockStructure;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FragmentExtractorFeatureInstance extends ManualPlacementFeatureInstance {
    
    private static final long COOLDOWN = 1000L * 10 * 60;
    
    private ConditionalMenu menu;
    private ItemStack[] inventory;
    private long lastGenerateTime;
    private boolean notify;
    
    protected FragmentExtractorFeatureInstance(Player player, BlockStructure blocks, Location location, ItemStack usedItem) {
        super(player.getUniqueId(), blocks, location, usedItem);
        
        notify = true;
        lastGenerateTime = System.currentTimeMillis();
        inventory = new ItemStack[getSlots(player)];
        Arrays.fill(inventory, new ItemStack(Material.AIR));
    }
    
    @SuppressWarnings("unchecked")
    public FragmentExtractorFeatureInstance(Map<String, Object> map) {
        super(map);
        
        List<ItemStack> items = (List<ItemStack>) map.getOrDefault("inventory", new ArrayList<>());
        
        inventory = new ItemStack[items.size()];
        for (int i = 0; i < items.size(); i++)
            inventory[i] = items.get(i);
        
        this.notify = (boolean) map.getOrDefault("notify", true);
        this.lastGenerateTime = (long) map.getOrDefault("lastGenerateTime", System.currentTimeMillis());
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("inventory", Arrays.asList(inventory));
        map.put("lastGenerateTime", lastGenerateTime);
        map.put("notify", notify);
        
        return map;
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        for (ItemStack i : inventory)
            if (!i.getType().isAir())
                getInitialBlock().getWorld().dropItemNaturally(getInitialBlock(), i);
    }
    
    @Override
    protected void tick() {
        if (!Utils.isChunkLoaded(getInitialBlock()))
            return;
        
        if (Arrays.stream(inventory).anyMatch(i -> !i.getType().isAir()))
            getInitialBlock().getWorld().spawnParticle(Particle.CRIT_MAGIC, getInitialBlock().add(0.5, 2, 0.5), 5);
        
        Player player = Bukkit.getPlayer(getOwnerId());
        
        if (player == null)
            return;
        
        lastGenerateTime += 20;
        
        int slots = getSlots(player);
        
        if (inventory.length < slots) {
            ItemStack[] temp = new ItemStack[slots];
            for (int i = 0; i < temp.length; i++)
                if (inventory.length > i)
                    temp[i] = inventory[i];
                else
                    temp[i] = new ItemStack(Material.AIR);
            inventory = temp;
        }
        
        if (lastGenerateTime + COOLDOWN <= System.currentTimeMillis()) {
            lastGenerateTime = System.currentTimeMillis();
            addFragment(player);
        }
        
    }
    
    @Override
    protected void interact(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        if (event.getPlayer().getUniqueId().equals(getOwnerId())) {
            display(event.getPlayer());
            event.setCancelled(true);
        } else
            event.getPlayer().sendMessage(CLFeatures.CC_PREFIX + "You cannot open someone else's fragment extractor.");
    }
    
    @Override
    protected Feature<? extends FeatureInstance> getManager() {
        return CLFeatures.getInstance().getFeature("fragmentExtractor");
    }
    
    private void addFragment(Player player) {
        getInitialBlock().getWorld().playSound(getInitialBlock(), Sound.BLOCK_GRINDSTONE_USE, 1F, 1F);
        for (int i = 0; i < inventory.length; i++)
            if (inventory[i].getType().isAir()) {
                if (notify)
                    player.sendMessage(CLFeatures.CC_PREFIX + "§aA fragment has been extracted at your fragment extractor!");
                inventory[i] = getCurrency();
                break;
            }
        
        if (menu == null)
            createInventory();
        for (Menu m : menu.getMenus().values())
            if (!m.getInventory().getViewers().isEmpty())
                display((Player) m.getInventory().getViewers().get(0));
    }
    
    private ItemStack getCurrency() {
        double random = Math.random();
        
        if (random < 0.02)
            return CLCore.getInstance().getItemRegistry().getItem("greaterfragment").orElse(new ItemStack(Material.AIR));
        else if (random < 0.1)
            return CLCore.getInstance().getItemRegistry().getItem("commonfragment").orElse(new ItemStack(Material.AIR));
        else if (random < 0.4)
            return CLCore.getInstance().getItemRegistry().getItem("pettyfragment").orElse(new ItemStack(Material.AIR));
        else
            return CLCore.getInstance().getItemRegistry().getItem("lesserfragment").orElse(new ItemStack(Material.AIR));
    }
    
    public void setNotify(boolean notify) {
        this.notify = notify;
    }
    
    private int getSlots(Player player) {
        return player.getEffectivePermissions().stream().filter(p -> p.getValue() && p.getPermission().contains("fragmentextractor.slots."))
                .map(s -> Integer.parseInt(s.getPermission().replace("fragmentextractor.slots.", ""))).max(Comparator.comparingInt(e -> e)).orElse(1);
    }
    
    private void display(Player player) {
        createInventory();
        
        player.playSound(player.getLocation(), Sound.UI_STONECUTTER_TAKE_RESULT, 0.5F, 1F);
        player.openInventory(menu.getMenu(ResourcePackManager.getInstance().isFullyAccepted(player) ? "resource" : "default").getInventory());
    }
    
    private void createInventory() {
        menu = new ConditionalMenu(getManager().getPlugin(), 3, Arrays.asList(new Tuple<>("default", "Fragment Extractor"),
                new Tuple<>("resource", "§f" + TranslateSpaceFont.TRANSLATE_NEGATIVE_8 + "\uE306" + TranslateSpaceFont.getSpecificAmount(-169) + "§8Fragment Extractor")));
        
        for (int i = inventory.length + 9; i < 18; i++)
            menu.set(i, new MenuItem(new ItemBuilder(Material.STONE).setDisplayName("§6§lLOCKED").setLore("", "§e§lYou must rank up §7to", "§7unlock this slot.")
                    .setCustomModelData(19).build()), "resource");
        
        for (int i = 0; i < inventory.length; i++) {
            final int key = i;
            ItemStack value = inventory[key];
            menu.set(key + 9, new MenuItem(value)
                    .addClickAction(c -> {
                        Player player = c.getPlayer();
                        
                        if (!c.getCursor().getType().isAir())
                            return;
                        
                        if (c.getItem().getItem().getType().isAir())
                            return;
                        
                        player.setItemOnCursor(c.getItem().getItem().clone());
                        inventory[key] = new ItemStack(Material.AIR);
                        menu.replace(key + 9, new ItemStack(Material.AIR));
                    }));
        }
    }
}
