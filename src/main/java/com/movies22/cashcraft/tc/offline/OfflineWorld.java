package com.movies22.cashcraft.tc.offline;

import org.bukkit.World;

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
}
