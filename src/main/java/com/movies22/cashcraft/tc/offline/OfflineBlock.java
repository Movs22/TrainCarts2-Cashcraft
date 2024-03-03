package com.movies22.cashcraft.tc.offline;

import java.util.Collection;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;

public class OfflineBlock implements Block {

    private Material type;
    private OfflineLocation loc;
    private BlockState state;
    private BlockData data;
    public OfflineBlock(Block b, OfflineLocation l) {
        this.type = b.getType();
        this.loc = l;
        this.state = b.getState();
        this.data = b.getBlockData();
    }

    @Override
    public List<MetadataValue> getMetadata(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMetadata'");
    }

    @Override
    public boolean hasMetadata(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasMetadata'");
    }

    @Override
    public void removeMetadata(String arg0, Plugin arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeMetadata'");
    }

    @Override
    public void setMetadata(String arg0, MetadataValue arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setMetadata'");
    }

    @Override
    public String getTranslationKey() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTranslationKey'");
    }

    @Override
    public boolean applyBoneMeal(BlockFace arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'applyBoneMeal'");
    }

    @Override
    public boolean breakNaturally() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'breakNaturally'");
    }

    @Override
    public boolean breakNaturally(ItemStack arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'breakNaturally'");
    }

    @Override
    public boolean canPlace(BlockData arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'canPlace'");
    }

    @Override
    public Biome getBiome() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBiome'");
    }

    @Override
    public BlockData getBlockData() {
        return this.data;
    }

    @Override
    public int getBlockPower() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBlockPower'");
    }

    @Override
    public int getBlockPower(BlockFace arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBlockPower'");
    }

    @Override
    public BoundingBox getBoundingBox() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBoundingBox'");
    }

    @Override
    public float getBreakSpeed(Player arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBreakSpeed'");
    }

    @Override
    public Chunk getChunk() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getChunk'");
    }

    @Override
    public VoxelShape getCollisionShape() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCollisionShape'");
    }

    @Override
    public byte getData() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getData'");
    }

    @Override
    public Collection<ItemStack> getDrops() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDrops'");
    }

    @Override
    public Collection<ItemStack> getDrops(ItemStack arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDrops'");
    }

    @Override
    public Collection<ItemStack> getDrops(ItemStack arg0, Entity arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDrops'");
    }

    @Override
    public BlockFace getFace(Block arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFace'");
    }

    @Override
    public double getHumidity() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHumidity'");
    }

    @Override
    public byte getLightFromBlocks() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLightFromBlocks'");
    }

    @Override
    public byte getLightFromSky() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLightFromSky'");
    }

    @Override
    public byte getLightLevel() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLightLevel'");
    }

    @Override
    public OfflineLocation getLocation() {
        return this.loc;
    }

    @Override
    public Location getLocation(Location arg0) {
        return this.loc;
    }

    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPistonMoveReaction'");
    }

    @Override
    public Block getRelative(BlockFace arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRelative'");
    }

    @Override
    public Block getRelative(BlockFace arg0, int arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRelative'");
    }

    @Override
    public Block getRelative(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRelative'");
    }

    @Override
    public BlockState getState() {
        return this.state;
    }

    @Override
    public double getTemperature() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTemperature'");
    }

    @Override
    public Material getType() {
        return this.type;
    }

    @Override
    public World getWorld() {
        return this.loc.getWorld();
    }

    @Override
    public int getX() {
        return this.loc.getBlockX();
    }

    @Override
    public int getY() {
        return this.loc.getBlockY();
    }

    @Override
    public int getZ() {
        return this.loc.getBlockZ();
    }

    @Override
    public boolean isBlockFaceIndirectlyPowered(BlockFace arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBlockFaceIndirectlyPowered'");
    }

    @Override
    public boolean isBlockFacePowered(BlockFace arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBlockFacePowered'");
    }

    @Override
    public boolean isBlockIndirectlyPowered() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBlockIndirectlyPowered'");
    }

    @Override
    public boolean isBlockPowered() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBlockPowered'");
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
    }

    @Override
    public boolean isLiquid() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isLiquid'");
    }

    @Override
    public boolean isPassable() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isPassable'");
    }

    @Override
    public boolean isPreferredTool(ItemStack arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isPreferredTool'");
    }

    @Override
    public RayTraceResult rayTrace(Location arg0, Vector arg1, double arg2, FluidCollisionMode arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rayTrace'");
    }

    @Override
    public void setBiome(Biome arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setBiome'");
    }

    @Override
    public void setBlockData(BlockData arg0) {
        this.data = arg0;
        this.state.setBlockData(this.data);
    }

    @Override
    public void setBlockData(BlockData arg0, boolean arg1) {
        this.data = arg0;
        this.state.setBlockData(this.data);
    }

    @Override
    public void setType(Material arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setType'");
    }

    @Override
    public void setType(Material arg0, boolean arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setType'");
    }
    
}
