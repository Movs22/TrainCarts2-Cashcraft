package com.movies22.cashcraft.tc.PathFinding;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MetroLines;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.signactions.SignAction;
import com.movies22.cashcraft.tc.signactions.SignActionBlocker;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rail.Shape;

public class PathNode {
   public Location loc;
   public Sign sign;
   public MetroLines.MetroLine line;
   public Rail rail;
   private SignAction signAction;
   private List<String> txt = new ArrayList();
   private HashMap<BlockFace, PathNode> neighbours = new HashMap();
   public List<PathOperation> connections = new ArrayList();
   public List<BlockFace> facings = new ArrayList();
   public BlockFace direction;
   private int SpeedLimit = -1;
   public MinecartGroup onBlock = null;
   // $FF: synthetic field
   private static volatile int[] $SWITCH_TABLE$org$bukkit$block$data$Rail$Shape;

   public PathNode(Location l, Block b, MetroLines.MetroLine line) {
      this.handleBuild(l, b, line);
   }

   public String getLocationStr() {
      return this.loc.getBlockX() + "/" + this.loc.getBlockY() + "/" + this.loc.getBlockZ();
   }

   public void handleBuild(Location l, Block b, MetroLines.MetroLine line) {
      this.txt = new ArrayList<String>();
      this.txt.add(null);
      this.txt.add(null);
      this.txt.add(null);
      this.txt.add(null);
      this.loc = l;
      Sign s = null;
      if (b.getState() instanceof Sign) {
         s = (Sign)b.getState();
      }

      this.sign = s;
      this.line = line;

      try {
         this.rail = (Rail)l.getBlock().getBlockData();
      } catch (ClassCastException var8) {
         TrainCarts.plugin.getLogger().log(Level.WARNING, l.getX() + "/" + l.getY() + "/" + l.getZ() + " is an invalid node.");
      }

      if (s != null && s.getLines().length > 0) {
         org.bukkit.block.data.type.Sign s2 = null;

         try {
            s2 = (org.bukkit.block.data.type.Sign)b.getBlockData();
         } catch (ClassCastException var7) {
            TrainCarts.plugin.getLogger().log(Level.WARNING, l.getX() + "/" + l.getY() + "/" + l.getZ() + " is an invalid node.");
         }

         if (s2 == null) {
            return;
         }

         for(int i = 0; i < s.getLines().length; ++i) {
            this.setLine(i, s.getLine(i));
         }

         SignAction a = SignAction.parse(s);
         if (a == null) {
            this.setAction((SignAction)null);
         } else {
            this.direction = s2.getRotation();
            a.node = this;
            a.sign = s;
            this.setAction(a);
            a.postParse();
            if (a instanceof SignActionPlatform && ((SignActionPlatform)a).station != null && ((SignActionPlatform)a).station.headcode == null) {
               this.facings.add(this.direction);
            }
         }
      } else {
         this.setAction((SignAction)null);
      }

   }

   public void setLine(int i, String s) {
      this.txt.set(i, s);
   }

   public <T extends SignAction> T setAction(T a) {
      this.signAction = a;
      return a;
   }

   public Location getLocation() {
      return this.loc;
   }

   public SignAction getAction() {
      return this.signAction;
   }

   public String getLine(int i) {
      return (String)this.txt.get(i);
   }

   public HashMap<BlockFace, PathNode> getNeighbours() {
      return this.neighbours;
   }

   public void addNeighbour(BlockFace f, PathNode neighbour) {
      this.neighbours.putIfAbsent(f, neighbour);
   }

   public void removeNeighbour(BlockFace f) {
      this.neighbours.remove(f);
   }

   public void clearNeighbours() {
      this.neighbours.clear();
   }

   public int getSpeedLimit() {
      return this.SpeedLimit;
   }

   public void setSpeedLimit(int speedLimit) {
      this.SpeedLimit = speedLimit;
   }

   public void findNeighbours() {
      this.connections.clear();
      this.facings.clear();
      Location a = this.loc.clone();
      if (this.signAction != null) {
         if (this.signAction.getAction().equals("SignActionBlocker")) {
            BlockFace f = ((SignActionBlocker)this.signAction).getBlocked(this.sign);
            this.facings.add(f);
            this.connections.add(new PathOperation(this, this, f));
         }

         if (!this.signAction.getAction().equals("SignActionSwitcher")) {
            switch($SWITCH_TABLE$org$bukkit$block$data$Rail$Shape()[this.rail.getShape().ordinal()]) {
            case 1:
               this.facings.add(BlockFace.EAST);
               this.connections.add(new PathOperation(this, this, BlockFace.EAST));
               this.facings.add(BlockFace.WEST);
               this.connections.add(new PathOperation(this, this, BlockFace.WEST));
               break;
            case 2:
               this.facings.add(BlockFace.NORTH);
               this.connections.add(new PathOperation(this, this, BlockFace.NORTH));
               this.facings.add(BlockFace.SOUTH);
               this.connections.add(new PathOperation(this, this, BlockFace.SOUTH));
            case 3:
            case 4:
            case 5:
            case 6:
            default:
               break;
            case 7:
               this.facings.add(BlockFace.NORTH);
               this.connections.add(new PathOperation(this, this, BlockFace.NORTH));
               this.facings.add(BlockFace.WEST);
               this.connections.add(new PathOperation(this, this, BlockFace.WEST));
               break;
            case 8:
               this.facings.add(BlockFace.NORTH);
               this.connections.add(new PathOperation(this, this, BlockFace.NORTH));
               this.facings.add(BlockFace.EAST);
               this.connections.add(new PathOperation(this, this, BlockFace.EAST));
               break;
            case 9:
               this.facings.add(BlockFace.EAST);
               this.connections.add(new PathOperation(this, this, BlockFace.EAST));
               this.facings.add(BlockFace.SOUTH);
               this.connections.add(new PathOperation(this, this, BlockFace.SOUTH));
               break;
            case 10:
               this.facings.add(BlockFace.WEST);
               this.connections.add(new PathOperation(this, this, BlockFace.WEST));
               this.facings.add(BlockFace.SOUTH);
               this.connections.add(new PathOperation(this, this, BlockFace.SOUTH));
            }
         }

         a.subtract(0.0D, 0.0D, 1.0D);
         Material b = a.getBlock().getBlockData().getMaterial();
         if ((b == Material.RAIL || b == Material.POWERED_RAIL) && !this.facings.contains(BlockFace.NORTH)) {
            this.facings.add(BlockFace.NORTH);
            this.connections.add(new PathOperation(this, (PathNode)null, BlockFace.NORTH));
         }

         a.add(1.0D, 0.0D, 1.0D);
         b = a.getBlock().getBlockData().getMaterial();
         if ((b == Material.RAIL || b == Material.POWERED_RAIL) && !this.facings.contains(BlockFace.EAST)) {
            this.facings.add(BlockFace.EAST);
            this.connections.add(new PathOperation(this, (PathNode)null, BlockFace.EAST));
         }

         a.add(-1.0D, 0.0D, 1.0D);
         b = a.getBlock().getBlockData().getMaterial();
         if ((b == Material.RAIL || b == Material.POWERED_RAIL) && !this.facings.contains(BlockFace.SOUTH)) {
            this.facings.add(BlockFace.SOUTH);
            this.connections.add(new PathOperation(this, (PathNode)null, BlockFace.SOUTH));
         }

         a.subtract(1.0D, 0.0D, 1.0D);
         b = a.getBlock().getBlockData().getMaterial();
         if ((b == Material.RAIL || b == Material.POWERED_RAIL) && !this.facings.contains(BlockFace.WEST)) {
            this.facings.add(BlockFace.WEST);
            this.connections.add(new PathOperation(this, (PathNode)null, BlockFace.WEST));
         }

      }
   }

   public void reroute() {
      this.findNeighbours();
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$org$bukkit$block$data$Rail$Shape() {
      int[] var10000 = $SWITCH_TABLE$org$bukkit$block$data$Rail$Shape;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[Shape.values().length];

         try {
            var0[Shape.ASCENDING_EAST.ordinal()] = 3;
         } catch (NoSuchFieldError var10) {
         }

         try {
            var0[Shape.ASCENDING_NORTH.ordinal()] = 5;
         } catch (NoSuchFieldError var9) {
         }

         try {
            var0[Shape.ASCENDING_SOUTH.ordinal()] = 6;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[Shape.ASCENDING_WEST.ordinal()] = 4;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[Shape.EAST_WEST.ordinal()] = 2;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[Shape.NORTH_EAST.ordinal()] = 10;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[Shape.NORTH_SOUTH.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[Shape.NORTH_WEST.ordinal()] = 9;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[Shape.SOUTH_EAST.ordinal()] = 7;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[Shape.SOUTH_WEST.ordinal()] = 8;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$org$bukkit$block$data$Rail$Shape = var0;
         return var0;
      }
   }
}
