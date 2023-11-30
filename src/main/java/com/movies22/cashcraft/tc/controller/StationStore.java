package com.movies22.cashcraft.tc.controller;

import com.movies22.cashcraft.tc.api.MetroLines;
import com.movies22.cashcraft.tc.api.Station;
import java.util.Arrays;
import java.util.HashMap;

public class StationStore {
   public HashMap<String, Station> Stations = new HashMap();
   public MetroLines.MetroLine line;
   private Station b;
   private String z;

   public Station createStation(String c, String s, String s2) {
      Station a = new Station(c, s, s2);
      this.Stations.putIfAbsent(c, a);
      return a;
   }

   public void addStation(Station s) {
      this.Stations.putIfAbsent(s.code, s);
   }

   public void removeStation(Station s) {
      this.Stations.remove(s.code);
   }

   public void removeStation(String s) {
      this.Stations.remove(s);
   }

   public Station getFromCode(String n) {
      return (Station)this.Stations.get(n);
   }

   public Station getFromName(String n) {
      this.b = null;
      this.Stations.values().forEach((a) -> {
         if (a.name.toLowerCase().equals(n.toLowerCase())) {
            this.b = a;
         }

      });
      return this.b;
   }

   public void postParse() {
      this.Stations.values().forEach((station) -> {
         this.z = station.osi;
         station.platforms.values().forEach((p) -> {
            if (!this.z.contains(p.node.line.getChar())) {
               this.z = this.z + p.node.line.getChar();
            }

         });
         String[] z2 = this.z.split("");
         Arrays.sort(z2);
         station.osi = String.join("", z2);
      });
   }
}
