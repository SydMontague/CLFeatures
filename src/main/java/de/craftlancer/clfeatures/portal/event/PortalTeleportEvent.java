package de.craftlancer.clfeatures.portal.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.craftlancer.clfeatures.portal.PortalFeatureInstance;

public class PortalTeleportEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    
    private boolean isCancelled;
    private final Player player;
    private final PortalFeatureInstance startingPortal;
    private final PortalFeatureInstance targetPortal;
    
    public PortalTeleportEvent(Player player, PortalFeatureInstance startingPortal, PortalFeatureInstance targetPortal) {
        this.player = player;
        this.startingPortal = startingPortal;
        this.targetPortal = targetPortal;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public PortalFeatureInstance getStartingPortal() {
        return startingPortal;
    }
    
    public PortalFeatureInstance getTargetPortal() {
        return targetPortal;
    }
    
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
    
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
