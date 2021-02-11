package de.craftlancer.clfeatures.trophydepositor;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class TrophyDepositorBoost implements ConfigurationSerializable {
    
    private double boost;
    private int trophiesLeft;
    
    public TrophyDepositorBoost(double boost, int trophiesLeft) {
        this.boost = boost;
        this.trophiesLeft = trophiesLeft;
    }
    
    public TrophyDepositorBoost(Map<String, Object> map) {
        this.boost = (double) map.get("boost");
        this.trophiesLeft = (int) map.get("trophiesLeft");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("boost", boost);
        map.put("trophiesLeft", trophiesLeft);
        
        return map;
    }
    
    public double apply(double trophy) {
        trophiesLeft--;
        return boost * trophy - trophy;
    }
    
    public int getTrophiesLeft() {
        return trophiesLeft;
    }
    
    public double getBoost() {
        return boost;
    }
    
    public void addTrophiesLeft(int size) {
        this.trophiesLeft += size;
    }
}
