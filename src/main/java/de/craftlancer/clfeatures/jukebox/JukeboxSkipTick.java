package de.craftlancer.clfeatures.jukebox;

import java.util.HashMap;
import java.util.Map;

public class JukeboxSkipTick implements AbstractJukeboxNote {
    
    public JukeboxSkipTick() {
    
    }
    
    public JukeboxSkipTick(Map<String, Object> map) {
    
    }
    
    @Override
    public Map<String, Object> serialize() {
        return new HashMap<>();
    }
}
