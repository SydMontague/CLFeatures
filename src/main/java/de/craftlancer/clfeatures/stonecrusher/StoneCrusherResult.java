package de.craftlancer.clfeatures.stonecrusher;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.craftlancer.core.CLCore;

public class StoneCrusherResult {
    private ItemStack item;
    private final String result;
    private final double chance;
    
    public StoneCrusherResult(String result, double chance) {
        this.chance = chance;
        this.result = result;
        reloadResult();
    }
    
    public void reloadResult() {
        this.item = CLCore.getInstance().getItemRegistry().getItem(result)
                          .orElse(new ItemStack(Optional.ofNullable(Material.matchMaterial(result)).orElse(Material.AIR)));
    }
    
    public double getChance() {
        return chance;
    }
    
    public ItemStack getResult() {
        return item;
    }
}