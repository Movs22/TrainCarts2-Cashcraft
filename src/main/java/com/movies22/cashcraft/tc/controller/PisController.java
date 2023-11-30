package com.movies22.cashcraft.tc.controller;

import com.bergerkiller.bukkit.sl.API.Variables;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PisController extends BaseController {
   private HashMap<String, PisController.PIS> pis = new HashMap();

   public PisController.PIS getPis(String s) {
      PisController.PIS a = (PisController.PIS)this.pis.get(s);
      if (a == null) {
         a = new PisController.PIS(s);
         this.pis.put(s, a);
      }

      return a;
   }

   public void updateSigns() {
      this.pis.values().forEach((pis) -> {
         if (!pis.lastVar.equals(pis.variable)) {
            Variables.get(pis.name).set(pis.variable);
            pis.lastVar = pis.variable;
         }

      });
   }

   public void clear() {
      this.pis.clear();
   }

   public void doFixedTick() {
      this.pis.values().forEach((pis) -> {
         pis.doStep();
      });
   }

   public class PIS {
      public String name;
      public String variable = "--:--";
      public String lastVar = "--:--";
      public int delay;
      public List<Integer> timers = new ArrayList();
      private List<Integer> timers2;

      PIS(String n) {
         this.name = n;
      }

      public void setArrived(Boolean b) {
         if (b) {
            this.variable = "Arrived";
            if (this.timers.size() > 0 && (Integer)this.timers.get(0) < 30) {
               this.timers.remove(0);
            }
         } else {
            this.variable = this.getTime(this.getNextTimer());
         }

      }

      public void addTimer(int a) {
         if (a > 10000000) {
            this.variable = "No Services";
         } else {
            if (this.timers.size() > 0 && a < (Integer)this.timers.get(this.timers.size() - 1)) {
               this.timers.add(a);
               this.timers.sort((z, y) -> {
                  return y - z;
               });
            } else {
               this.timers.add(a);
            }

         }
      }

      public Integer getNextTimer() {
         for(int i = 0; i < this.timers.size(); ++i) {
            if ((Integer)this.timers.get(i) < this.delay || (Integer)this.timers.get(i) < -30) {
               this.timers.remove(i);
            }
         }

         if (this.timers.size() > 0) {
            return (Integer)this.timers.get(0);
         } else {
            return 0;
         }
      }

      public void doStep() {
         this.timers2 = new ArrayList();
         this.timers.forEach((timer) -> {
            this.timers2.add(timer - 1);
         });
         this.timers = new ArrayList(this.timers2);
         if (this.timers.size() == 0) {
            this.variable = "No Services";
         } else {
            this.delay = -(Integer)this.timers.get(0);
            if ((Integer)this.timers.get(0) < 0 && !this.variable.equals("Arrived")) {
               if ((Integer)this.timers.get(0) > -31) {
                  this.variable = "0:05";
               } else {
                  this.variable = "Cancelled";
                  if (this.timers.size() > 0) {
                     this.timers.remove(0);
                     if (this.timers.size() > 0) {
                        this.delay = -(Integer)this.timers.get(0);
                     } else {
                        this.delay = 0;
                     }
                  } else {
                     this.delay = 0;
                  }
               }
            } else {
               if (this.variable.equals("Arrived")) {
                  return;
               }

               this.variable = this.getTime((Integer)this.timers.get(0));
            }

         }
      }

      public String getTime(int g) {
         if (g > 54) {
            int b = (g + 5) % 60;
            int a = (g + 5 - b % 5) / 60;
            if (b < 6) {
               return a + ":05";
            } else {
               return b < 60 ? a + ":" + (b - b % 5) : a + ":00";
            }
         } else {
            return g < 6 ? "0:05" : "0:" + (g + 5 - g % 5);
         }
      }
   }
}
