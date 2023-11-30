package com.movies22.cashcraft.tc.api;

import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.signactions.SignActionSpawner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Depot {
   public HashMap<String, SignActionSpawner> lanes = new HashMap<String, SignActionSpawner>();
   public String name;
   public String code;
   public HashMap<String, PathRoute> routes = new HashMap<String, PathRoute>();
   public HashMap<String, SpawnerRate> routerate = new HashMap<String, SpawnerRate>();
   public List<MetroLines.MetroLine> lines = new ArrayList<MetroLines.MetroLine>();
   private SignActionSpawner a;

   public Depot(String code, String name) {
      this.code = code;
      this.name = name;
   }

   public Boolean addLane(String i, SignActionSpawner a) {
      this.lanes.put(i, a);
      if (!this.lines.contains(a.node.line)) {
         this.lines.add(a.node.line);
      }

      return true;
   }

   public SignActionSpawner getRandomLane() {
      this.a = null;
      if (this.lanes.values().size() == 0) {
         return null;
      } else {
         while(this.a == null) {
            this.lanes.values().forEach((l) -> {
               if (Math.random() * (double)this.lanes.size() < 1.0D) {
                  this.a = l;
               }
            });
         }

         return this.a;
      }
   }

   public Boolean removeLane(String i) {
      this.lanes.remove(i);
      return true;
   }

   public void addRoute(PathRoute r) {
      this.routes.put(r.name, r);
   }

   public void removeRoute(PathRoute r) {
      this.routes.remove(r.name);
   }

   public void addRouteRate(PathRoute r, SpawnerRate s) {
      this.routerate.put(r._line.getName() + ":" + r.name, s);
   }

   public void addRouteRate(String r, SpawnerRate s) {
      this.routerate.put(r, s);
   }

   public SpawnerRate getRouteRate(PathRoute r) {
      return (SpawnerRate)this.routerate.get(r._line.getName() + ":" + r.name);
   }

   public SpawnerRate getRouteRate(String r) {
      return (SpawnerRate)this.routerate.get(r);
   }

   public void removeRouteRate(PathRoute r) {
      this.routerate.remove(r._line.getName() + ":" + r.name);
   }

   public void removeRouteRate(String r) {
      this.routerate.remove(r);
   }
}
