package com.movies22.cashcraft.tc.controller;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.api.Depot;
import com.movies22.cashcraft.tc.api.MetroLines;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.signactions.SignActionSpawner;
import com.movies22.cashcraft.tc.utils.Despawn;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;

public class DepotController extends BaseController {
   public HashMap<String, Depot> depots = new HashMap();
   public HashMap<String, Integer> headcodes = new HashMap();
   private Depot b;

   public Depot createDepot(String c, String s) {
      Depot a = new Depot(c, s);
      this.depots.putIfAbsent(c, a);
      return a;
   }

   public void addDepot(Depot s) {
      this.depots.putIfAbsent(s.code, s);
   }

   public void removeDepot(Depot s) {
      this.depots.remove(s.code);
   }

   public void removeDepot(String s) {
      this.depots.remove(s);
   }

   public Depot getFromCode(String n) {
      return (Depot)this.depots.get(n);
   }

   public Depot getFromName(String n) {
      this.b = null;
      this.depots.values().forEach((a) -> {
         if (a.name == n) {
            this.b = a;
         }

      });
      return this.b;
   }

   public String getNextHeadcode(String s) {
      int i = 0;
      if (this.headcodes.get(s) != null) {
         i = (Integer)this.headcodes.get(s);
      }

      if (i > 99) {
         i = 0;
      }

      String headcode = s + i;
      if (i < 10) {
         headcode = s + "0" + i;
      }

      ++i;
      this.headcodes.put(s, i);
      return headcode;
   }

   public void doFixedTick() {
      this.depots.values().forEach((depot) -> {
         depot.routerate.values().forEach((v) -> {
            long n = LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toInstant().toEpochMilli() - 3600000L;
            if (v.getNextSpawnTime() != null) {
               long z = v.getNextSpawnTime()._timestamp;
               if (n >= z) {
                  SignActionSpawner lane = depot.getRandomLane();
                  PathRoute a = (PathRoute)depot.routes.get(v.route.split(":")[1]);
                  if (a != null) {
                     String headcode = "[AWAITING HEADCODE]";
                     Boolean finished = false;
                     PathRoute r = lane.getRoute(a._start).clone();
                     r.reverse = v.route;
                     PathRoute b = r.clone();
                     MinecartGroup m = new MinecartGroup(a._line, headcode, v.getNextTrain());
                     PathRoute b2 = b.clone();
                     m.addRoute(b2);

                     while(!finished) {
                        if (b.reverse == null) {
                           finished = true;
                        } else if (b.reverse.equals("ECS")) {
                           m.addRoute(m.getLastRoute()._end.getAction().getRoute(lane.node).clone());
                           finished = true;
                        } else if (!b.reverse.equals("none")) {
                           MetroLines.MetroLine c = TrainCarts.plugin.lines.getLine(b.reverse.split(":")[0]);
                           b = c.getRoute(b.reverse.split(":")[1]);
                           if (b == null) {
                              finished = true;
                              m.destroy(Despawn.INVALID_ROUTE);
                           }

                           m.addRoute(b.clone());
                        } else if (b.reverse.equals("none")) {
                           PathRoute y = m.getLastRoute()._end.getAction().getRoute(lane.node).clone();
                           y.name = "DESPAWN";
                           m.addRoute(y);
                           finished = true;
                        }
                     }

                     if (v.getNextSpawnTime(1) == null) {
                        m.nextTrain = Integer.MAX_VALUE;
                     } else {
                        long yx = v.getNextSpawnTime(1)._timestamp;
                        m.nextTrain = (int)((yx - z) / 1000L);
                     }

                     m.spawn(lane.node);
                  }
               }

            }
         });
      });
   }
}
