package de.craftlancer.clfeatures.stonecrusher;

import de.craftlancer.clfeatures.BlueprintFeatureInstance;
import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.core.structure.BlockStructure;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StoneCrusherFeatureInstance extends BlueprintFeatureInstance {
    private StoneCrusherFeature manager;
    
    private Location inputChest;
    private Location outputChest;
    private BlockFace facing;
    private Inventory input;
    private Inventory output;
    
    public StoneCrusherFeatureInstance(StoneCrusherFeature manager, UUID ownerId, BlockStructure blocks, Location location, String usedSchematic) {
        super(ownerId, blocks, location, usedSchematic);
        this.manager = manager;
        
        outputChest = location;
        inputChest = location.clone().add(0, 2, 0);
    }
    
    public StoneCrusherFeatureInstance(Map<String, Object> map) {
        super(map);
        
        outputChest = getInitialBlock();
        inputChest = getInitialBlock().clone().add(0, 2, 0);
    }
    
    public Location getInputChest() {
        return inputChest;
    }
    
    public Location getOutputChest() {
        return outputChest;
    }
    
    private boolean doCrushing(Material material, List<StoneCrusherResult> lootTable) {
        if (!input.contains(material))
            return false;
        
        double sum = 0;
        double chance = Math.random();
        if (!input.removeItem(new ItemStack(material)).isEmpty())
            return false;
        
        for (StoneCrusherResult a : lootTable) {
            sum += a.getChance();
            if (chance < sum) {
                if (!output.addItem(a.getResult().clone()).isEmpty()) {
                    World world = getInitialBlock().getWorld();
                    world.dropItem(getOutputChest(), a.getResult());
                    world.playSound(getInitialBlock(), Sound.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1, 1);
                }
                return true;
            }
        }
        
        return true;
    }
    
    @Override
    protected void tick() {
        World w = getInitialBlock().getWorld();
        
        if (input == null || output == null) {
            facing = ((Directional) getInitialBlock().getBlock().getBlockData()).getFacing().getOppositeFace();
            input = ((Chest) getInputChest().getBlock().getState()).getInventory();
            output = ((Chest) getOutputChest().getBlock().getState()).getInventory();
        }
        
        // don't process feature in unloaded chunks
        if (!w.isChunkLoaded(inputChest.getBlockX() >> 4, inputChest.getBlockZ() >> 4)
                || !w.isChunkLoaded(outputChest.getBlockX() >> 4, outputChest.getBlockZ() >> 4))
            return;
        
        for (int i = 0; i < getManager().getCrushesPerTick(); i++)
            getManager().getLootTable().entrySet().stream().anyMatch(a -> doCrushing(a.getKey(), a.getValue()));
        
        Location effectLocation = getInitialBlock().clone().add(-facing.getModZ() + 0.4 + Math.random() / 5, 1.5, facing.getModX() + 0.4 + Math.random() / 5);
        w.spawnParticle(Particle.FALLING_LAVA, effectLocation, 1);
        w.spawnParticle(Particle.SPELL_INSTANT, effectLocation, 3);
    }
    
    @EventHandler()
    public void onChunkLoad(ChunkLoadEvent e) {
        Chunk c = e.getChunk();
        
        if (c.getX() == inputChest.getBlockX() >> 4 && c.getZ() == inputChest.getBlockZ() >> 4) {
            facing = ((Directional) getInitialBlock().getBlock().getBlockData()).getFacing().getOppositeFace();
            input = ((Chest) getInputChest().getBlock().getState()).getInventory();
            output = ((Chest) getOutputChest().getBlock().getState()).getInventory();
        }
    }
    
    @Override
    protected StoneCrusherFeature getManager() {
        if (manager == null)
            manager = (StoneCrusherFeature) CLFeatures.getInstance().getFeature("stonecrusher");
        
        return manager;
    }
}
