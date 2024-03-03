package com.movies22.cashcraft.tc.progress;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MetroLines;
import com.movies22.cashcraft.tc.pathFinding.PathNode;
import com.movies22.cashcraft.tc.pathFinding.PathRoute;
import com.movies22.cashcraft.tc.signactions.SignActionBlocker;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;
import com.movies22.cashcraft.tc.signactions.SignActionRBlocker;
import com.movies22.cashcraft.tc.signactions.SignActionSpawner;
import com.movies22.cashcraft.tc.signactions.SignActionSpeed;
import com.movies22.cashcraft.tc.signactions.SignActionSwitcher;

import net.md_5.bungee.api.ChatColor;


public class RouteProgress {
    public Player player;
    public List<PathNode> route;
    public MetroLines.MetroLine line;
    public String name;
    public RouteProgress(Player p, PathNode s, MetroLines.MetroLine l, String n) {
        this.player = p;
        this.route = new ArrayList<PathNode>();
        this.route.add(s);
        this.line = l;
        this.name = n;
    }


    public void addBlock(Block b, Boolean f) {
        PathNode n = TrainCarts.plugin.global.getNode(b.getLocation().add(0.5, 0, 0.5));
        if(n == null) {
            this.player.sendMessage("§cFailed to find a node at " + b.getLocation().getX() + "/" + b.getLocation().getY() + "/" + b.getLocation().getZ());
            return;
        }
        this.route.add(n);
        if(f) {
            PathRoute r = new PathRoute(this.name, this.route, this.line);
			this.line.addRoute(r, this.name);
			this.player.sendMessage(ChatColor.GREEN + "Created route " + ChatColor.YELLOW + this.name + ChatColor.GREEN + " on the " + this.line.getName() + " line with " + ChatColor.YELLOW + r.route.size() + ChatColor.GREEN + " connections.");
            TrainCarts.plugin.playerProgress.remove(this.player.getName());
        } else {
            this.player.sendBlockChange(b.getLocation(), Material.EMERALD_BLOCK.createBlockData());   
            this.player.sendMessage(ChatColor.GREEN + "Added node " + ChatColor.YELLOW + (n.getAction().getAction()) + ChatColor.GREEN + " to route §e" + this.name + "§a.");
        }
        return;
    }

    public void tick() {
        Location p = this.player.getLocation();
        TrainCarts.plugin.global.getNodes().values().forEach(n -> {
            if(n.loc.distance(p) < 30) {
                if(this.route.contains(n)) {
                    this.player.sendBlockChange(n.loc, Material.EMERALD_BLOCK.createBlockData());   
                } else if(n.getAction() instanceof SignActionBlocker) {
                    this.player.sendBlockChange(n.loc, Material.RED_CONCRETE.createBlockData());   
                } else if(n.getAction() instanceof SignActionSpeed) {
                    this.player.sendBlockChange(n.loc, Material.MAGENTA_TERRACOTTA.createBlockData());   
                } else if(n.getAction() instanceof SignActionSpawner) {
                    this.player.sendBlockChange(n.loc, Material.BEACON.createBlockData());   
                } else if(n.getAction() instanceof SignActionPlatform) {
                    this.player.sendBlockChange(n.loc, Material.DIAMOND_BLOCK.createBlockData());   
                } else if(n.getAction() instanceof SignActionSwitcher) {
                    this.player.sendBlockChange(n.loc, Material.GOLD_BLOCK.createBlockData());   
                } else if(n.getAction() instanceof SignActionBlocker) {
                    if(((SignActionRBlocker) n.getAction()).isBlocked(this.name)) {
                        this.player.sendBlockChange(n.loc, Material.RED_CONCRETE.createBlockData());   
                    }
                } else {
                    this.player.sendBlockChange(n.loc, Material.BEDROCK.createBlockData());   
                }
            }
        });
    }
}