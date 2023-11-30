package com.movies22.cashcraft.tc.controller;

import com.movies22.cashcraft.tc.TrainCarts;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Location;

public class PlayerController extends BaseController {
   private List<Location> locs = new ArrayList();

   public Boolean hasToLoad(Location l) {
      Boolean a = false;
      Iterator var3 = this.locs.iterator();

      while(var3.hasNext()) {
         Location loc = (Location)var3.next();
         if (l.distance(loc) < 32.0D) {
            a = true;
            break;
         }
      }

      return a;
   }

   public void doFixedTick() {
      this.locs.clear();
      TrainCarts.plugin.getServer().getOnlinePlayers().forEach((p) -> {
         Location l = p.getLocation();
         if (l.getWorld().getName().equals("Main1")) {
            this.locs.add(p.getLocation());
         }

      });
   }
}
