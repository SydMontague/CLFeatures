package de.craftlancer.clfeatures;

import de.craftlancer.core.CLCore;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.Utils;
import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.conversation.ClickableBooleanPrompt;
import de.craftlancer.core.conversation.FormattedConversable;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class Feature<T extends FeatureInstance> implements Listener {
    
    private final CLFeatures plugin;
    private int defaultLimit;
    private int maxLimit;
    private Map<String, Integer> limitMap = new HashMap<>();
    
    private final NamespacedKey limitKey;
    private String featureItem;
    private final String limitToken;
    
    public Feature(CLFeatures plugin, ConfigurationSection config, NamespacedKey limitKey) {
        this.plugin = plugin;
        this.limitKey = limitKey;
        this.limitToken = config.getString("limitToken", "");
        
        featureItem = config.getString("featureItem");
        
        defaultLimit = config.getInt("defaultLimit", -1);
        maxLimit = config.getInt("maxLimit", -1);
        ConfigurationSection limitConfig = config.getConfigurationSection("limits");
        if (limitConfig != null)
            limitConfig.getKeys(false).forEach(a -> limitMap.put(a, limitConfig.getInt(a)));
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        new LambdaRunnable(() -> getFeatures().forEach(FeatureInstance::tick)).runTaskTimer(getPlugin(), 10, getTickFrequency());
    }
    
    public String getMoveMetaData() {
        return getName() + "Move";
    }
    
    public int getLimit(Player player) {
        int groupLimit = limitMap.entrySet().stream().filter(a -> plugin.getPermissions().playerInGroup(player, a.getKey())).map(Entry::getValue)
                .max(Integer::compare).orElseGet(() -> defaultLimit);
        int individualLimit = player.getPersistentDataContainer().getOrDefault(limitKey, PersistentDataType.INTEGER, 0).intValue();
        
        return groupLimit < 0 ? -1 : groupLimit + individualLimit;
    }
    
    public boolean checkFeatureLimit(Player player) {
        if (player.hasPermission(String.format("clfeature.%s.ignoreLimit", getName())))
            return true;
        
        int limit = getLimit(player);
        
        if (limit < 0)
            return true;
        
        long current = getFeatures().stream().filter(a -> a.isOwner(player)).count();
        
        return current < limit;
    }
    
    
    public List<T> getFeaturesByUUID(UUID uuid) {
        return getFeatures().stream().filter(a -> a.isOwner(uuid)).collect(Collectors.toList());
    }
    
    
    public void addFeatureLimit(Player player, int amount) {
        int individualLimit = player.getPersistentDataContainer().getOrDefault(limitKey, PersistentDataType.INTEGER, 0).intValue();
        player.getPersistentDataContainer().set(limitKey, PersistentDataType.INTEGER, individualLimit + amount);
    }
    
    public boolean isLimitToken(@Nonnull ItemStack item) {
        if (item.getType().isAir())
            return false;
        
        return item.isSimilar(CLCore.getInstance().getItemRegistry().getItem(limitToken).orElseGet(() -> new ItemStack(Material.AIR)));
    }
    
    public int getMaxLimit() {
        return maxLimit;
    }
    
    public String getFeatureItemRegistryKey() {
        return featureItem;
    }
    
    public CLFeatures getPlugin() {
        return plugin;
    }
    
    public void giveFeatureItem(Player player, T instance) {
        ItemStack item = getFeatureItem(player, instance);
        if (item != null)
            player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
    }
    
    public void giveFeatureItem(Player player) {
        giveFeatureItem(player, null);
    }
    
    public abstract ItemStack getFeatureItem(Player player, T instance);
    
    public ItemStack getFeatureItem(Player player) {
        return getFeatureItem(player, null);
    }
    
    public abstract void save();
    
    public abstract CommandHandler getCommandHandler();
    
    public abstract void remove(FeatureInstance instance);
    
    public long getTickFrequency() {
        return 10;
    }
    
    protected BreakAction getBreakAction() {
        return BreakAction.PROMPT;
    }
    
    @Nonnull
    protected abstract String getName();
    
    public abstract List<T> getFeatures();
    
    private boolean handlePiston(List<Block> blockList) {
        BoundingBox bb = Utils.calculateBoundingBoxBlock(blockList);
        
        return getFeatures().stream().filter(a -> a.getStructure().containsBoundingBox(bb))
                .anyMatch(a -> a.getStructure().containsAnyBlock(blockList));
    }
    
    private void handleExplosion(List<Block> blockList) {
        BoundingBox bb = Utils.calculateBoundingBoxBlock(blockList);
        
        Set<Location> locs = getFeatures().stream().map(T::getStructure).filter(a -> a.containsBoundingBox(bb)).flatMap(a -> a.getBlocks().stream())
                .collect(Collectors.toSet());
        
        blockList.removeIf(a -> locs.contains(a.getLocation()));
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock())
            return;
        
        Optional<? extends T> optional = getFeatures().stream().filter(f -> f.getStructure().containsBlock(event.getClickedBlock())).findFirst();
        
        if (!optional.isPresent())
            return;
        
        Player p = event.getPlayer();
        T feature = optional.get();
        
        if (p.hasMetadata(getMoveMetaData()))
            handleMove(feature, event);
        else
            feature.interact(event);
    }
    
    protected boolean handleMove(T feature, PlayerInteractEvent event) {
        Player p = event.getPlayer();
        
        if (!feature.getOwnerId().equals(p.getUniqueId()))
            return false;
        
        feature.destroy();
        giveFeatureItem(p, feature);
        p.sendMessage(CLFeatures.CC_PREFIX + ChatColor.YELLOW + getName() + " successfully moved back to your inventory.");
        p.removeMetadata(getMoveMetaData(), getPlugin());
        event.setCancelled(true);
        
        return true;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (handlePiston(event.getBlocks()))
            event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (handlePiston(event.getBlocks()))
            event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onExplosion(EntityExplodeEvent event) {
        handleExplosion(event.blockList());
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onExplosion(BlockExplodeEvent event) {
        handleExplosion(event.blockList());
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onGrow(BlockFormEvent event) {
        if (getFeatures().stream().anyMatch(a -> a.getStructure().containsBlock(event.getBlock())))
            event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onFlow(BlockFromToEvent event) {
        if (getFeatures().stream().anyMatch(a -> a.getStructure().containsBlock(event.getToBlock())))
            event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (getFeatures().stream().anyMatch(a -> event.getBlock().getLocation().equals(a.getInitialBlock())))
            return;
        
        if (getFeatures().stream().anyMatch(a -> a.getStructure().containsBlock(event.getBlock())))
            event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Optional<T> optional = getFeatures().stream().filter(f -> event.getBlock().getLocation().equals(f.getInitialBlock())).findFirst();
        
        if (!optional.isPresent())
            return;
        
        T feature = optional.get();
        
        switch (getBreakAction()) {
            case PROMPT:
                new ConversationFactory(getPlugin()).withLocalEcho(false).withModality(false).withTimeout(30)
                        .withFirstPrompt(new DestroyPrompt(feature)).buildConversation(new FormattedConversable(event.getPlayer())).begin();
                event.setCancelled(true);
                break;
            case DROP_IF_ANY:
                feature.destroy();
                feature.getInitialBlock().getWorld().dropItemNaturally(feature.getInitialBlock(), getFeatureItem(event.getPlayer(), feature));
                break;
            case DROP_IF_OWNER:
                if (!event.getPlayer().getUniqueId().equals(feature.getOwnerId())) {
                    new ConversationFactory(getPlugin()).withLocalEcho(false).withModality(false).withTimeout(30)
                            .withFirstPrompt(new DestroyPrompt(feature)).buildConversation(new FormattedConversable(event.getPlayer())).begin();
                    event.setCancelled(true);
                    break;
                }
                feature.destroy();
                feature.getInitialBlock().getWorld().dropItemNaturally(feature.getInitialBlock(), getFeatureItem(event.getPlayer(), feature));
                break;
            case DESTROY:
                feature.destroy();
                break;
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (getFeatures().stream().anyMatch(a -> a.getStructure().containsBlock(event.getBlock())))
            event.setCancelled(true);
    }
    
    protected boolean sendDestroyPrompt() {
        return true;
    }
    
    private class DestroyPrompt extends ClickableBooleanPrompt {
        
        private T feature;
        
        public DestroyPrompt(T feature) {
            super(CLFeatures.CC_PREFIX + "§eDo you really want to destroy this feature? It will be gone forever! Check the move command of the feature otherwise.");
            
            this.feature = feature;
        }
        
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
            if (input) {
                feature.destroy();
                context.getForWhom().sendRawMessage(CLFeatures.CC_PREFIX + "§eYou destroyed this feature.");
            } else
                context.getForWhom().sendRawMessage(CLFeatures.CC_PREFIX + "§eYou didn't destroy this feature.");
            return END_OF_CONVERSATION;
        }
        
    }
    
    public enum BreakAction {
        DESTROY,
        PROMPT,
        DROP_IF_ANY,
        DROP_IF_OWNER
    }
}
