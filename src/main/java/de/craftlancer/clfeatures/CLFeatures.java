package de.craftlancer.clfeatures;

import de.craftlancer.clapi.blueprints.event.BlueprintPostPasteEvent;
import de.craftlancer.clapi.blueprints.event.BlueprintPrePasteEvent;
import de.craftlancer.clapi.clfeatures.AbstractFeatureInstance;
import de.craftlancer.clapi.clfeatures.PluginCLFeatures;
import de.craftlancer.clfeatures.amplifiedbeacon.AmplifiedBeaconFeatureInstance;
import de.craftlancer.clfeatures.atm.ATMFeature;
import de.craftlancer.clfeatures.atm.ATMFeatureInstance;
import de.craftlancer.clfeatures.chair.ChairFeature;
import de.craftlancer.clfeatures.chair.ChairFeatureInstance;
import de.craftlancer.clfeatures.fragmentextractor.FragmentExtractorFeature;
import de.craftlancer.clfeatures.fragmentextractor.FragmentExtractorFeatureInstance;
import de.craftlancer.clfeatures.furniture.FurnitureFeature;
import de.craftlancer.clfeatures.furniture.FurnitureFeatureInstance;
import de.craftlancer.clfeatures.jukebox.JukeboxFeature;
import de.craftlancer.clfeatures.jukebox.JukeboxFeatureInstance;
import de.craftlancer.clfeatures.painter.PainterFeature;
import de.craftlancer.clfeatures.painter.PainterFeatureInstance;
import de.craftlancer.clfeatures.portal.PortalFeature;
import de.craftlancer.clfeatures.portal.PortalFeatureInstance;
import de.craftlancer.clfeatures.replicator.ReplicatorFeature;
import de.craftlancer.clfeatures.replicator.ReplicatorFeatureInstance;
import de.craftlancer.clfeatures.spawnblocker.SpawnBlockGroup;
import de.craftlancer.clfeatures.spawnblocker.SpawnBlockerFeature;
import de.craftlancer.clfeatures.spawnblocker.SpawnBlockerFeatureInstance;
import de.craftlancer.clfeatures.stonecrusher.StoneCrusherFeature;
import de.craftlancer.clfeatures.stonecrusher.StoneCrusherFeatureInstance;
import de.craftlancer.clfeatures.transmutationstation.TransmutationStationFeature;
import de.craftlancer.clfeatures.transmutationstation.TransmutationStationFeatureInstance;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorBoost;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorFeature;
import de.craftlancer.clfeatures.trophydepositor.TrophyDepositorFeatureInstance;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.conversation.ClickableBooleanPrompt;
import de.craftlancer.core.conversation.FormattedConversable;
import de.craftlancer.core.util.MessageUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class CLFeatures extends JavaPlugin implements Listener, PluginCLFeatures {
    public static final String CC_PREFIX = "§f[§4Craft§fCitizen]§e ";
    
    private static CLFeatures instance;
    private static final Material ERROR_BLOCK = Material.RED_CONCRETE;
    private static final long ERROR_TIMEOUT = 100L; // 5s
    
    private NamespacedKey featureItemKey;
    private Map<String, Feature<?>> features = new HashMap<>();
    
    private ConversationFactory conversation = new ConversationFactory(this).withLocalEcho(false).withModality(false).withTimeout(30)
            .withFirstPrompt(new UseLimitTokenPrompt());
    
    public static CLFeatures getInstance() {
        return instance;
    }
    
    @Override
    public void onEnable() {
        instance = this;
        
        ConfigurationSerialization.registerClass(PortalFeatureInstance.class);
        ConfigurationSerialization.registerClass(StoneCrusherFeatureInstance.class);
        ConfigurationSerialization.registerClass(TrophyDepositorFeatureInstance.class);
        ConfigurationSerialization.registerClass(TrophyDepositorFeature.TrophyEntry.class);
        ConfigurationSerialization.registerClass(TrophyDepositorBoost.class);
        ConfigurationSerialization.registerClass(ReplicatorFeatureInstance.class);
        ConfigurationSerialization.registerClass(SpawnBlockerFeatureInstance.class);
        ConfigurationSerialization.registerClass(SpawnBlockGroup.class);
        ConfigurationSerialization.registerClass(TransmutationStationFeatureInstance.class);
        ConfigurationSerialization.registerClass(AmplifiedBeaconFeatureInstance.class);
        ConfigurationSerialization.registerClass(JukeboxFeatureInstance.class);
        ConfigurationSerialization.registerClass(ChairFeatureInstance.class);
        ConfigurationSerialization.registerClass(PainterFeatureInstance.class);
        ConfigurationSerialization.registerClass(FragmentExtractorFeatureInstance.class);
        ConfigurationSerialization.registerClass(FurnitureFeatureInstance.class);
        ConfigurationSerialization.registerClass(ATMFeatureInstance.class);
        
        Bukkit.getServicesManager().register(PluginCLFeatures.class, this, this, ServicePriority.Highest);
        
        saveDefaultConfig();
        featureItemKey = new NamespacedKey(this, "clfeature");
        
        getServer().getPluginManager().registerEvents(this, this);
        
        registerFeature("portal", PortalFeature::new);
        registerFeature("stonecrusher", StoneCrusherFeature::new);
        registerFeature("replicator", ReplicatorFeature::new);
        registerFeature("spawnBlocker", SpawnBlockerFeature::new);
        registerFeature("transmutationStation", TransmutationStationFeature::new);
        registerFeature("trophyDepositor", TrophyDepositorFeature::new);
        registerFeature("jukebox", JukeboxFeature::new);
        registerFeature("chair", ChairFeature::new);
        registerFeature("painter", PainterFeature::new);
        registerFeature("furniture", FurnitureFeature::new);
        registerFeature("fragmentExtractor", FragmentExtractorFeature::new);
        registerFeature("atm", ATMFeature::new);
        
        MessageUtil.register(this, new TextComponent("§f[§4Craft§fCitizen]"), ChatColor.WHITE, ChatColor.YELLOW, ChatColor.RED,
                ChatColor.DARK_RED, ChatColor.DARK_AQUA, ChatColor.GREEN);
        
        new LambdaRunnable(() -> features.forEach((a, b) -> b.save())).runTaskTimer(this, 18000L, 18000L);
    }
    
    private void registerFeature(String name, BiFunction<CLFeatures, ConfigurationSection, Feature<?>> constructor) {
        if (!getConfig().isConfigurationSection(name))
            getConfig().createSection(name);
        
        Feature<?> feature = constructor.apply(this, getConfig().getConfigurationSection(name));
        features.put(name, feature);
        getCommand(name).setExecutor(feature.getCommandHandler());
    }
    
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        features.forEach((a, b) -> b.save());
    }
    
    @Override
    public Map<String, Feature<?>> getFeatures() {
        return features;
    }
    
    public NamespacedKey getFeatureItemKey() {
        return featureItemKey;
    }
    
    @Override
    @Nullable
    public Feature<? extends AbstractFeatureInstance> getFeature(@Nonnull String string) {
        return features.get(string);
    }
    
    @EventHandler
    public void onBluePrintPrePaste(BlueprintPrePasteEvent event) {
        Optional<Feature<?>> feature = features.values().stream().filter(a -> a.getName().equalsIgnoreCase(event.getType())).findFirst();
        
        if (!feature.isPresent())
            return;
        
        if (!feature.get().checkFeatureLimit(event.getPlayer())) {
            event.getPlayer().sendMessage(CC_PREFIX + ChatColor.DARK_RED + "You've reached your limit for this feature.");
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBluePrintPaste(BlueprintPostPasteEvent event) {
        Optional<Feature<?>> feature = features.values().stream().filter(a -> a.getName().equalsIgnoreCase(event.getType())).findFirst();
        
        if (!feature.isPresent() || !(feature.get() instanceof BlueprintFeature))
            return;
        
        ((BlueprintFeature<?>) feature.get()).createInstance(event.getPlayer(), event);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent event) {
        Optional<Feature<?>> feature = features.values().stream().filter(a ->
                a instanceof ManualPlacementFeature && ((ManualPlacementFeature<?>) a).isFeatureItem(event.getItemInHand())).findFirst();
        
        Player p = event.getPlayer();
        
        if (!feature.isPresent())
            return;
        
        if (!feature.get().checkFeatureLimit(event.getPlayer())) {
            p.sendMessage(CC_PREFIX + ChatColor.DARK_RED + "You've reached your limit for this feature.");
            event.setCancelled(true);
        }
        
        Collection<Block> blocks = ((ManualPlacementFeature<?>) feature.get()).checkEnvironment(event.getBlock());
        
        if (!blocks.isEmpty()) {
            p.sendMessage(CC_PREFIX + ChatColor.DARK_RED + "This location isn't suited for this feature. Make sure you have enough space.");
            p.sendMessage(CC_PREFIX + ChatColor.DARK_RED + "See " + ChatColor.GREEN + "https://craftlancer.de/wiki/index.php/Special_Structures");
            event.setCancelled(true);
            
            blocks.forEach(a -> event.getPlayer().sendBlockChange(a.getLocation(), ERROR_BLOCK.createBlockData()));
            new LambdaRunnable(() -> blocks.forEach(a -> p.sendBlockChange(a.getLocation(), a.getBlockData()))).runTaskLater(this, ERROR_TIMEOUT);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlaceFinal(BlockPlaceEvent event) {
        Optional<Feature<?>> feature = features.values().stream().filter(a ->
                a instanceof ManualPlacementFeature && ((ManualPlacementFeature<?>) a).isFeatureItem(event.getItemInHand())).findFirst();
        
        if (!feature.isPresent())
            return;
        
        ((ManualPlacementFeature<?>) feature.get()).createInstance(event.getPlayer(), event.getBlock(), event.getItemInHand().clone());
    }
    
    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasItem())
            return;
        
        ItemStack item = event.getItem().clone();
        item.setAmount(1);
        Player player = event.getPlayer();
        for (Feature<?> feature : features.values()) {
            if (feature.isLimitToken(item)) {
                Conversation convo = conversation.buildConversation(new FormattedConversable(player));
                convo.getContext().setSessionData("player", player);
                convo.getContext().setSessionData("item", item);
                convo.getContext().setSessionData("feature", feature);
                convo.begin();
                return;
            }
        }
    }
    
    private class UseLimitTokenPrompt extends ClickableBooleanPrompt {
        
        public UseLimitTokenPrompt() {
            super(CC_PREFIX + "Do you really want to use this limit token?");
        }
        
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
            if (!input) {
                context.getForWhom().sendRawMessage(CC_PREFIX + "You didn't increase your feature limit.");
                return Prompt.END_OF_CONVERSATION;
            }
            
            Player player = (Player) context.getSessionData("player");
            ItemStack item = (ItemStack) context.getSessionData("item");
            Feature<?> feature = (Feature<?>) context.getSessionData("feature");
            
            if (!feature.isLimitToken(item))
                context.getForWhom().sendRawMessage(CC_PREFIX + "The token has changed?");
            else if (!player.getInventory().containsAtLeast(item, 1))
                context.getForWhom().sendRawMessage(CC_PREFIX + "You don't have the token in your inventory anymore.");
            else if (feature.getLimit(player) == feature.getMaxLimit())
                context.getForWhom().sendRawMessage(CC_PREFIX + "You've already reached the maximal limit for this feature.");
            else {
                player.getInventory().removeItem(item);
                feature.addFeatureLimit(player, 1);
                context.getForWhom().sendRawMessage(CC_PREFIX + "You've increased your feature limit for " + feature.getName() + " by 1.");
            }
            
            return Prompt.END_OF_CONVERSATION;
        }
    }
}
