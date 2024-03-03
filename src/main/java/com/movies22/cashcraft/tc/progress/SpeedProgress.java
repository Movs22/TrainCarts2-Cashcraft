package com.movies22.cashcraft.tc.progress;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.movies22.cashcraft.tc.pathFinding.PathNode;
import com.movies22.cashcraft.tc.signactions.SignActionSpeed;
import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;

public class SpeedProgress {
    public Player player;
    public MetroLine line;
    public List<FakeBlock> fakeBlocks;
    public Inventory prevInv;
    public List<RouteSection> secs;
    public Double speed;
    public SpeedProgress(Player p, MetroLine r) {
        this.player = p;
        this.line = r;
        this.prevInv = p.getInventory();
        this.fakeBlocks = new ArrayList<FakeBlock>();
        this.secs = new ArrayList<RouteSection>();
        p.getInventory().clear();
        p.sendMessage("§aLoading editor...");
        r.getRoutes().values().forEach(ro -> {
        speed = 0.6;
        ro.route.forEach(con -> {
            if(con.getStartNode().getAction() instanceof SignActionSpeed) {
                speed = ((SignActionSpeed) con.getStartNode().getAction()).speed;
            }
            this.secs.add(new RouteSection(con.getStartNode(), con.getEndNode(), speed, con.locs));
        });
        //TrainCarts.plugin.getLogger().log(Level.INFO, this.secs.toString());
        p.sendMessage("§aLoaded §e" + this.secs.size() + "§a connections.");
        this.secs.forEach(sec -> {
            Material t = Material.GRAY_CONCRETE;
            if(sec.speed == 0.4) t = Material.RED_CONCRETE;
            if(sec.speed == 0.5) t = Material.ORANGE_CONCRETE;
            if(sec.speed == 0.6) t = Material.YELLOW_CONCRETE;
            if(sec.speed == 0.7) t = Material.LIME_CONCRETE;
            if(sec.speed == 0.8) t = Material.GREEN_CONCRETE;
            if(sec.speed == 0.9) t = Material.CYAN_CONCRETE;
            if(sec.speed == 1.0) t = Material.LIGHT_BLUE_CONCRETE;
            if(sec.speed == 1.2) t = Material.BLUE_CONCRETE;
            if(sec.speed == 1.3) t = Material.PURPLE_CONCRETE;
            if(sec.speed == 1.5) t = Material.MAGENTA_CONCRETE;
            if(sec.speed == 1.6) t = Material.PINK_CONCRETE;
            if(sec.speed == 2.0) t = Material.DIAMOND_BLOCK;
            for(int i = 0; i < (sec.locs.size() -1); i++) {
                drawLine(sec.locs.get(i), sec.locs.get(i+1), t);
            }
        });
    });

    }

    public void cancel(Player p) {
        BlockData a = Material.AIR.createBlockData();
        this.fakeBlocks.forEach(c -> {
            p.sendBlockChange(c.l, a);
        });
        this.fakeBlocks.clear();
        this.secs.clear();
        TrainCarts.plugin.speedProgress.remove(p.getName());
        p.sendMessage("§cCancelled changes.");
    }

    public void drawLine(Location st, Location e, Material t) {
        Double dx = e.getX() - st.getX();
        Double dz = e.getZ() - st.getZ();
        Location s = st.clone();
        s.add(0, 315-st.getBlockY(), 0);
        BlockData d = t.createBlockData();
        //TrainCarts.plugin.getLogger().log(Level.INFO, "Drawing from " + s + " to " + e + " - material - " + t + " | " + dx + " | " + dz);
        if(Math.abs(dx) > Math.abs(dz)) {
            if(dx > 0) {
                for(Double i = st.getX(); i < e.getX(); i++) {
                    this.player.sendBlockChange(s.add(dx/Math.abs(dx), 0, Math.round(dz/Math.abs(dx))), d); 
                    //if(!this.fakeBlocks.stream().anyMatch(z -> z.l.equals(s)))  
                    this.fakeBlocks.add(new FakeBlock(s.clone(), d));
                }
            } else {
                for(Double i = st.getX(); i > e.getX(); i--) {
                    this.player.sendBlockChange(s.add(dx/Math.abs(dx), 0, Math.round(dz/Math.abs(dx))), d); 
                    //if(!this.fakeBlocks.stream().anyMatch(z -> z.l.equals(s)))  
                    this.fakeBlocks.add(new FakeBlock(s.clone(), d));
                }

            }   
        } else {
            if(dz > 0) {
                for(Double i = st.getZ(); i < e.getZ(); i++) {
                    this.player.sendBlockChange(s.add(Math.round(dx/Math.abs(dz)), 0, dz/Math.abs(dz)), d); 
                    //TrainCarts.plugin.getLogger().log(Level.INFO, "Loading block at " + s + ". Match - " + this.fakeBlocks.stream().anyMatch(z -> z.l.equals(s)));
                    //if(!this.fakeBlocks.stream().anyMatch(z -> z.l.equals(s)))  
                    this.fakeBlocks.add(new FakeBlock(s.clone(), d));
                }
            } else {
                for(Double i = st.getZ(); i > e.getZ(); i--) {
                    this.player.sendBlockChange(s.add(Math.round(dx/Math.abs(dz)), 0, dz/Math.abs(dz)), d); 
                    //if(!this.fakeBlocks.stream().anyMatch(z -> z.l.equals(s)))  
                    this.fakeBlocks.add(new FakeBlock(s.clone(), d));
                }
            }
        }
    }
    private Location p;
    public void tick() {
        p = this.player.getLocation();
        /*this.route.nodes.forEach(n -> {
            if(n.loc.distance(p) < 30) {
                Location l = n.loc.clone();
                l.add(0, 383-l.getBlockY(), 0);
                BlockData d = Material.DIAMOND_BLOCK.createBlockData();
                this.player.sendBlockChange(l, d); 
                if(this.fakeBlocks.stream().anyMatch(z -> z.l.equals(l))) this.fakeBlocks.add(new FakeBlock(l, d));
            }
        });*/
        this.fakeBlocks.forEach(b -> {
            if(b.l.distance(p) < 50) {
                //TrainCarts.plugin.getLogger().log(Level.INFO, "Loading block at " + b.l + " - " + b.t);
                this.player.sendBlockChange(b.l, b.t);   
            }
        });
    }
    
    private class FakeBlock {
        private BlockData t;
        private Location l;
        public FakeBlock(Location l, BlockData t) {
            this.t = t;
            this.l = l;
        }
    }

    private class RouteSection {
        private PathNode s;
        private PathNode e;
        private Double speed = 0.0;
        private Boolean selected = false;
        private List<Location> locs;
        public RouteSection(PathNode start, PathNode end, Double speed, List<Location> locs) {
            this.s = start;
            this.e = end;
            this.speed = speed;
            this.locs = locs;
        }
    }
}
