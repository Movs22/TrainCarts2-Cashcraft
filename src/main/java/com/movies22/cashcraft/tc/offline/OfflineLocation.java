package com.movies22.cashcraft.tc.offline;

import org.bukkit.Location;
import org.bukkit.World;

import com.movies22.cashcraft.tc.offline.OfflineBlock;

public class OfflineLocation extends Location {

    private OfflineBlock block;

    public OfflineLocation(World world, int x, int y, int z) {
        super(world, x, y, z);
        this.block = new OfflineBlock(world.getBlockAt(x, y, z), this);
    }

    @Override
    public OfflineBlock getBlock() {
        return this.block;
    }
    
}
