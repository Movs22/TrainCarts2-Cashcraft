package com.movies22.cashcraft.tc.PathFinding;

import com.bergerkiller.bukkit.sl.API.Variables;
import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MetroLines;
import com.movies22.cashcraft.tc.api.Station;
import com.movies22.cashcraft.tc.signactions.SignActionBlocker;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;
import com.movies22.cashcraft.tc.signactions.SignActionRBlocker;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Location;

public class PathRoute implements Cloneable {
   public PathNode _start;
   public PathNode _end;
   public String name;
   public MetroLines.MetroLine _line;
   public List<PathOperation> route = new ArrayList();
   public List<SignActionPlatform> stops = new ArrayList();
   public List<PathNode> nodes = new ArrayList();
   public String reverse;
   private int i;
   PathNode start;
   double sD;
   double oX;
   double oZ;
   PathOperation connection;
   boolean finished;
   private List<Location> checkednodes;
   private List<Station> stations;
   private int z = 0;

   public PathRoute(String n, List<PathNode> s, MetroLines.MetroLine l) {
      if (s.size() != 0) {
         this._start = (PathNode)s.get(0);
         this._end = (PathNode)s.get(s.size() - 1);
         this.nodes = s;
         this.name = n;
         this._line = l;
         if (this._end.getAction() instanceof SignActionPlatform) {
            Station e = ((SignActionPlatform)this._end.getAction()).station;
            Variables.get(l.getChar() + ":" + e.headcode).set(e.displayName);
         }

      }
   }

   public void clear() {
      if (this.nodes != null) {
         this.nodes.clear();
         this.nodes = null;
      }

      this.stops.clear();
      this.stops = null;
   }

   public String toString() {
      return this._line + ":" + this.name;
   }

   public PathRoute clone() {
      PathRoute a = new PathRoute(this.name, this.nodes, this._line);
      a.route = new ArrayList();
      this.route.forEach((op) -> {
         a.route.add(op.clone());
      });
      a.stops = new ArrayList(this.stops);
      a.nodes = new ArrayList(this.nodes);
      a.reverse = this.reverse;
      return a;
   }

   public List<PathOperation> getRoute() {
      return this.route;
   }

   public Boolean setReverse(String r) {
      this.reverse = r;
      return true;
   }

   public String getReverse() {
      return this.reverse;
   }

   public void calculate() {
      this.route = new ArrayList();
      this.stops = new ArrayList();

      for(this.i = 0; this.i < this.nodes.size() - 1; ++this.i) {
         List<PathOperation> s = this.calculateSection((PathNode)this.nodes.get(this.i), (PathNode)this.nodes.get(this.i + 1));
         List<PathOperation> s2 = new ArrayList();
         s.forEach((a) -> {
            PathOperation z = a.clone();
            s2.add(z);
         });
         this.route.addAll(s2);
      }

   }

   public List<PathOperation> calculateSection(PathNode st, PathNode en) {
      this.checkednodes = new ArrayList();
      this.stations = new ArrayList();
      List<PathOperation> route = new ArrayList();
      PathNode end = en;
      this.finished = false;
      int i = 0;
      this.start = st;
      this.sD = Double.MAX_VALUE;
      this.oX = Double.MAX_VALUE;
      this.oZ = Double.MAX_VALUE;
      this.connection = null;
      this.checkednodes.add(this.start.loc);
      PathNode lastNode = null;

      while(!this.finished) {
         this.z = 0;
         this.start.connections.forEach((con) -> {
            Location e = end.getLocation();
            if (con.getEndNode() != null) {
               if (con.getEndNode() != null && !con.getEndNode().equals(this.start) && !this.checkednodes.contains(con.getEndNode().loc)) {
                  Location c = con.getEndNode().getLocation();
                  double a = e.distance(c);
                  if (con.getEndNode().getAction().getClass().equals(SignActionBlocker.class)) {
                     SignActionBlocker bx = (SignActionBlocker)con.getEndNode().getAction();
                     if (bx.getBlocked(con.getEndNode().sign).equals(con.getFacing())) {
                        ++this.z;
                        return;
                     }
                  }

                  if (con.getEndNode().getAction().getClass().equals(SignActionRBlocker.class)) {
                     SignActionRBlocker b = (SignActionRBlocker)con.getEndNode().getAction();
                     if (b.isBlocked(this)) {
                        ++this.z;
                        return;
                     }
                  }

                  if (a < this.sD + 1.0D && (Math.abs(e.getX() - c.getX()) < this.oX || Math.abs(e.getZ() - c.getZ()) < this.oZ) || a < this.sD + 1.0D) {
                     this.sD = a;
                     this.oX = Math.abs(e.getX() - c.getX());
                     this.oZ = Math.abs(e.getZ() - c.getZ());
                     this.connection = con.clone();
                     if (a < 1.0D) {
                        if (con.getEndNode().getAction() instanceof SignActionPlatform && !this.stops.contains((SignActionPlatform)con.getEndNode().getAction())) {
                           this.stops.add((SignActionPlatform)con.getEndNode().getAction());
                           if (this.start.getAction() instanceof SignActionPlatform) {
                              Station s = ((SignActionPlatform)con.getEndNode().getAction()).station;
                              if (s.osi.length() > 1) {
                                 Iterator var9 = this.stations.iterator();

                                 while(var9.hasNext()) {
                                    Station s2 = (Station)var9.next();
                                    s2.osiNeighbours.put(this, ((SignActionPlatform)con.getEndNode().getAction()).station);
                                 }
                              } else {
                                 this.stations.add(s);
                              }
                           }
                        }

                        route.add(con);
                        this.finished = true;
                        return;
                     }
                  }
               } else {
                  ++this.z;
               }

            }
         });
         if (!this.finished && this.connection != null) {
            if (this.start != null && this.connection.getEndNode() != null) {
               route.add(this.connection);
               this.checkednodes.add(this.connection.getEndNode().loc);
               lastNode = this.start;
               this.start = this.connection.getEndNode();
               if (this.start.getAction() != null && this.start.getAction() instanceof SignActionPlatform && !this.stops.contains((SignActionPlatform)this.start.getAction())) {
                  this.stops.add((SignActionPlatform)this.start.getAction());
               }
            } else {
               this.sD = Double.MAX_VALUE;
            }
         }

         ++i;
         if (lastNode != null && lastNode.equals(this.start)) {
            this.finished = true;
            if (!this.name.contains("[CA")) {
               this._line.deleteRoute(this.name);
               TrainCarts.plugin.getLogger().log(Level.WARNING, "Route " + this.name + " looped at " + st.getLocationStr() + ">" + end.getLocationStr());
               break;
            }
         }

         if (i > 1000) {
            this.finished = true;
            this._line.deleteRoute(this.name);
            TrainCarts.plugin.getLogger().log(Level.WARNING, "Route " + this.name + " couldn't reach its destination." + st.getLocationStr() + ">" + end.getLocationStr());
            break;
         }
      }

      return route;
   }
}
