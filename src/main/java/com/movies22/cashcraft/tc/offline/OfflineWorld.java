package com.movies22.cashcraft.tc.offline;

import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class OfflineWorld {
    World world;
    private HashMap<String, OfflineBlock> blocks;

    public OfflineWorld(World w) {
        this.world = w;
        this.blocks = new HashMap<>();
    }

    public OfflineBlock getBlock(int x, int y, int z) {
        OfflineBlock b = blocks.get(x + "/" + y + "/" + z);
        if(b == null) {
            b = new OfflineLocation(this.world, x, y, z).getBlock();
            blocks.put(x + "/" + y + "/" + z, b);
        }
        return b;
    }

    public void removeBlock(Vector v) {
        removeBlock(v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }

    public void removeBlock(int x, int y, int z) {
        blocks.remove(x + "/" + y + "/" + z);
        return;
    }

}
