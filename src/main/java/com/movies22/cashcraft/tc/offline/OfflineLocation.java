package com.movies22.cashcraft.tc.offline;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import com.movies22.cashcraft.tc.TrainCarts;

public class OfflineLocation extends Location {

    private OfflineBlock block;
    private OfflineWorld world;
    private Vector originalLoc;
    public OfflineLocation(World world, int x, int y, int z, OfflineBlock b) {
        super(world, x, y, z);
        this.block = b;
        this.world = TrainCarts.plugin.offlineWorlds.get(world);
        this.originalLoc = new Vector(x, y, z);
    }

    public OfflineLocation(World world, int x, int y, int z) {
        super(world, x, y, z);
        this.block = new OfflineBlock(world.getBlockAt(x, y, z), this);
        this.world = TrainCarts.plugin.offlineWorlds.get(world);
        this.originalLoc = new Vector(x, y, z);
    }

    @Override
    public OfflineBlock getBlock() {
        if(getBlockX() != this.originalLoc.getBlockX() || getBlockY() != this.originalLoc.getBlockY() || getBlockZ() != this.originalLoc.getBlockZ()) {
            return world.getBlock(getBlockX(), getBlockY(), getBlockZ());
        } else {
            return this.block;
        }
    }

    @Override
    public OfflineLocation clone() {
        return new OfflineLocation(getWorld(), getBlockX(), getBlockY(), getBlockZ(), this.block);
    }
    
}
