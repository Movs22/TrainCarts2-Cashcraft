package com.movies22.cashcraft.tc.api;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.PathFinding.PathOperation;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.signactions.SignAction;
import com.movies22.cashcraft.tc.signactions.SignActionBlocker;
import com.movies22.cashcraft.tc.signactions.SignActionRBlocker;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.TextDisplay;

public class MinecartMember implements Comparable<MinecartMember> {
   private MinecartGroup group;
   private VirtualMinecart entity;
   public Double _mod = 1.0D;
   public Double currentSpeed = 0.0D;
   private List<PathOperation> route = null;
   public Double _targetSpeed = 0.4D;
   public PathNode prevNode = null;
   public int index;
   public TextDisplay destination;
   public String destinationPos;
   public PathOperation lastCon = null;
   public BlockFace facing;
   public Chunk lastChunk;
   public SignAction lastAction;
   public Boolean spawned = false;
   public Boolean virtualized = true;
   String z;

   MinecartMember(MinecartGroup g, Minecart e, int i) {
      this.setGroup(g);
      this.facing = g._getSpawn().direction;
      this.index = i;
      this.spawned = false;
      if (i == 0) {
         this.setEntity(new VirtualMinecart(e, this, 0.0D));
      } else {
         this.setEntity(new VirtualMinecart(e, this.group.head(), (double)i * 1.5D));
      }

      TrainCarts.plugin.MemberController.addMember(e.getUniqueId(), this);
   }

   public void setPivot(MinecartMember mm) {
      this.entity.setPivot(mm);
   }

   public void setOffset(Double o) {
      this.entity.setOffset(o);
   }

   public Boolean destroy() {
      try {
         if (this.getEntity() != null) {
            TrainCarts.plugin.MemberController.removeMember(this);
            this.getEntity().remove();
         }

         this.route.clear();
         this.route = null;
         return true;
      } catch (Error var2) {
         var2.printStackTrace();
         return false;
      }
   }

   public Boolean eject() {
      try {
         if (this.getEntity() != null) {
            this.getEntity().eject();
         }

         return true;
      } catch (Error var2) {
         var2.printStackTrace();
         return false;
      }
   }

   public void setMaxSpeed(Double speed) {
      Minecart e = this.getEntity();
      if (e != null) {
         e.setMaxSpeed(speed);
      }

   }

   public MinecartGroup getGroup() {
      return this.group;
   }

   public void setGroup(MinecartGroup group) {
      this.group = group;
   }

   public Minecart getEntity() {
      return (Minecart)this.entity.getEntity();
   }

   public void setEntity(VirtualMinecart e) {
      this.entity = e;
   }

   public MinecartMember nextCart() {
      int nc = this.group.getMembers().indexOf(this);
      return nc >= 1 && nc <= this.group.getMembers().size() - 1 ? this.group.getMember(nc - 1) : null;
   }

   public List<PathOperation> getLocalRoute() {
      return this.route;
   }

   public Location getNextLocation() {
      if (this.route == null) {
         return null;
      } else if (this.route.size() == 0) {
         this.group.destroy();
         return null;
      } else {
         if (((PathOperation)this.route.get(0)).locs.size() < 1) {
            if (this.route.size() > 1) {
               this.lastCon = ((PathOperation)this.route.get(0)).clone();
               this.route.remove(0);
            } else {
               this.lastCon = ((PathOperation)this.route.get(0)).clone();
               this.route.remove(0);
               this.loadNextRoute();
            }
         }

         if (this.route.size() == 0) {
            this.group.destroy();
            return null;
         } else if (((PathOperation)this.route.get(0)).locs.size() > 0) {
            return (Location)((PathOperation)this.route.get(0)).locs.get(0);
         } else {
            this.group.destroy();
            return null;
         }
      }
   }

   public PathNode getNextNode() {
      return this.getNextNode(0, false);
   }

   public PathNode getNextNode(int i) {
      return this.getNextNode(i, false);
   }

   public PathNode getNextNode(int i, boolean b) {
      if (this.route == null) {
         return null;
      } else {
         PathNode a;
         if (i == 0) {
            a = ((PathOperation)this.route.get(0)).getEndNode();
            return (a.getAction() instanceof SignActionBlocker || a.getAction() instanceof SignActionRBlocker) && !b ? this.getNextNode(1) : a;
         } else if (this.route.size() <= i) {
            return null;
         } else {
            a = ((PathOperation)this.route.get(i)).getEndNode();
            return (a.getAction() instanceof SignActionBlocker || a.getAction() instanceof SignActionRBlocker) && !b ? this.getNextNode(i + 1) : a;
         }
      }
   }

   public void proceedTo(Location l) {
      if (this.route != null) {
         if (((PathOperation)this.route.get(0)).locs.size() != 0) {
            if (((PathOperation)this.route.get(0)).locs.indexOf(l) > -1) {
               this.lastCon = ((PathOperation)this.route.get(0)).clone();
               ((PathOperation)this.route.get(0)).locs.remove(0);
            } else {
               this.lastCon = ((PathOperation)this.route.get(0)).clone();
               this.route.remove(0);
            }
         } else if (this.route.size() > 1) {
            this.lastCon = ((PathOperation)this.route.get(0)).clone();
            this.route.remove(0);
         } else {
            this.lastCon = ((PathOperation)this.route.get(0)).clone();
            this.route.remove(0);
         }

      }
   }

   public void setLocalRoute(List<PathOperation> route) {
      this.route = route;
   }

   public void loadNextRoute() {
      if (this.group.routes.size() < 1) {
         this.group.destroy();
      } else {
         this.route = ((PathRoute)this.group.routes.get(0)).clone().route;
         if (this.lastCon == null) {
            this.lastCon = ((PathOperation)this.route.get(0)).clone();
         }

      }
   }

   public Double getTargetSpeed() {
      return this._targetSpeed != null ? this._targetSpeed : this.group.getTargetSpeed();
   }

   public String toString() {
      return this.getGroup().getHeadcode() + "/" + this.index + " | " + this.hashCode();
   }

   public int compareTo(MinecartMember m) {
      return this.index - m.index;
   }

   public Boolean virtualize() {
      this.virtualized = true;
      this.getEntity().remove();
      this.entity.setVirtualized(true);
      return true;
   }

   public Boolean load() {
      return this._targetSpeed == 0.0D ? this.load(true) : this.load(false);
   }

   public Location getLocation() {
      return this.getLocation(true);
   }

   public Location getLocation(Boolean offset) {
      return this.entity.getLocation(offset);
   }

   public Boolean load(Boolean offset) {
      this.entity.load(offset);
      this.virtualized = false;
      TrainCarts.plugin.MemberController.addMember(this.getEntity().getUniqueId(), this);
      return true;
   }
}
