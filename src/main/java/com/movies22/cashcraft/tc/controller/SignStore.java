package com.movies22.cashcraft.tc.controller;

import com.movies22.cashcraft.tc.api.MetroLines;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.block.Sign;

public class SignStore {
   public HashMap<Location, Sign> signs = new HashMap();
   public MetroLines.MetroLine line;

   public void addSign(Sign s) {
      this.signs.putIfAbsent(s.getLocation(), s);
   }

   public Sign getSign(Location l) {
      return (Sign)this.signs.get(l);
   }

   public Sign getSign(Sign s) {
      return (Sign)this.signs.get(s.getLocation());
   }
}
