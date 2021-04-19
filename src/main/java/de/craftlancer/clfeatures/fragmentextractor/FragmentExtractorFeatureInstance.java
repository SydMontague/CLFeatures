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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FragmentExtractorFeatureInstance extends ManualPlacementFeatureInstance {
    
    private static final long COOLDOWN = 1000 * 20;//1000 * 30 * 60;
    
    private ConditionalMenu menu;
    private Map<Integer, ItemStack> inventory;
    private Map<Integer, Long> lastPickupTime;
    
    protected FragmentExtractorFeatureInstance(UUID ownerId, BlockStructure blocks, Location location, ItemStack usedItem) {
        super(ownerId, blocks, location, usedItem);
        
        this.inventory = new HashMap<>();
        this.lastPickupTime = new HashMap<>();
    }
    
    public FragmentExtractorFeatureInstance(Map<String, Object> map) {
        super(map);
        
        this.inventory = (Map<Integer, ItemStack>) map.getOrDefault("inventory", new HashMap<>());
        this.lastPickupTime = (Map<Integer, Long>) map.getOrDefault("lastTimes", new HashMap<>());
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("inventory", inventory);
        map.put("lastTimes", lastPickupTime);
        
        return map;
    }
    
    @Override
    protected void tick() {
        if (!Utils.isChunkLoaded(getInitialBlock()))
            return;
        
        if (inventory.values().stream().anyMatch(i -> !i.getType().isAir()))
            getInitialBlock().getWorld().spawnParticle(Particle.CRIT_MAGIC, getInitialBlock().add(0.5, 2, 0.5), 5);
        
        Player player = Bukkit.getPlayer(getOwnerId());
        
        if (player == null)
            return;
        
        int slots = getSlots(player);
        
        if (inventory.size() < slots)
            for (int i = inventory.size() + 1; i < slots + 1; i++)
                addFragment(i);
        
        if (inventory.values().stream().noneMatch(i -> i.getType().isAir()))
            return;
        
        inventory.entrySet().stream().filter(e -> e.getValue().getType().isAir()).forEach(e -> {
            if (!lastPickupTime.containsKey(e.getKey()) || lastPickupTime.get(e.getKey()) + (COOLDOWN) <= System.currentTimeMillis())
                addFragment(e.getKey());
        });
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
    
    private void addFragment(int key) {
        getInitialBlock().getWorld().playSound(getInitialBlock(), Sound.BLOCK_GRINDSTONE_USE, 1F, 1F);
        inventory.put(key, CLCore.getInstance().getItemRegistry().getItem("lesserfragment").orElse(new ItemStack(Material.AIR)));
        lastPickupTime.put(key, System.currentTimeMillis());
        
        Player player = Bukkit.getPlayer(getOwnerId());
        
        if (player == null)
            return;
        
        player.sendMessage(CLFeatures.CC_PREFIX + "§aA fragment has been extracted at your fragment extractor!");
        
        for (Menu m : menu.getMenus().values())
            if (!m.getInventory().getViewers().isEmpty())
                display((Player) m.getInventory().getViewers().get(0));
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
        
        for (int i = inventory.size() + 9; i < 18; i++)
            menu.set(i, new MenuItem(new ItemBuilder(Material.STONE).setDisplayName("§6§lLOCKED").setLore("", "§e§lYou must rank up §7to", "§7unlock this slot.")
                    .setCustomModelData(19).build()), "resource");
        
        inventory.forEach((key, value) -> menu.set(key + 8, new MenuItem(value)
                .addClickAction(c -> {
                    Player player = c.getPlayer();
                    
                    if (!c.getCursor().getType().isAir())
                        return;
                    
                    if (c.getItem().getItem().getType().isAir())
                        return;
                    
                    player.setItemOnCursor(c.getItem().getItem().clone());
                    inventory.put(key, new ItemStack(Material.AIR));
                    lastPickupTime.put(key, System.currentTimeMillis());
                    menu.replace(key + 8, new ItemStack(Material.AIR));
                })));
    }
}
