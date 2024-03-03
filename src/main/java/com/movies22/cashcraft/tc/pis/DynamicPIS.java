package com.movies22.cashcraft.tc.pis;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import com.bergerkiller.bukkit.sl.API.Variables;
import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.Station;

public class DynamicPIS {
    public String name;
    public String code;
    public String variable = "--:--";
    public String lastVar = "--:--";
    public List<PISTimer> timers;
    public int delay;
    public DynamicPIS(String c, String n) {
        this.code = c;
        this.name = n;
        this.timers = new ArrayList<PISTimer>();
        TrainCarts.plugin.PisController.addDynPis(c, this);
    }
    
    public void doStep() {
        this.timers.removeIf(z -> (z.timers.size() == 0));
        this.timers.forEach(t -> {
            t.doStep();
        });
    }

    public void updateSign() {
        this.timers.removeIf(z -> z.timers.size() == 0 || z.station.code.startsWith(this.code));
        this.timers.sort((y, z) -> y.timers.get(0) - z.timers.get(0));
        if(this.timers.size() == 1/* && !this.code.startsWith(this.timers.get(0).station.code)  */) {
            Variables.get(this.code + "a").set("");
            Variables.get(this.code + "d").set("");
            Variables.get(this.code + "b").set(this.timers.get(0).colour + this.timers.get(0).station.displayName);
            Variables.get(this.code + "c").set("§f" + this.timers.get(0).getTime());
        } else if(this.timers.size() == 2 /*&& !this.code.startsWith(this.timers.get(0).station.code)*/) {
            Variables.get(this.code + "a").set(this.timers.get(0).colour + this.timers.get(0).station.displayName);
            Variables.get(this.code + "b").set("§f" + this.timers.get(0).getTime());
            Variables.get(this.code + "c").set(this.timers.get(0).colour + this.timers.get(0).station.displayName);
            Variables.get(this.code + "d").set("§f" + this.timers.get(0).getTime());
        } else {
            Variables.get(this.code + "a").set("");
            Variables.get(this.code + "d").set("");
            Variables.get(this.code + "b").set("§lWelcome to");
            Variables.get(this.code + "c").set(this.name);
        }
    }

    public void addTimer(String h, Station s, int a, String col) {
        PISTimer t = this.timers.stream().filter(z -> z.headcode.equals(h)).findFirst().orElse(null);
        if(t == null) {
            t = new PISTimer(s, h, col);
        } else {
            this.timers.remove(t);
        }
        t.addTimer(a);
        this.timers.add(t);
    }

    public void setArrived(String h, Station s, Boolean b, String c) {
        PISTimer t = this.timers.stream().filter(z -> z.headcode.equals(h)).findFirst().orElse(null);
        if(t == null) {
            t = new PISTimer(s, h, c);
            this.timers.add(t);
        }
        t.setArrived(b);
    }

    public class PISTimer {
        public List<Integer> timers;
        public List<Integer> timers2;
        public String headcode;
        public Station station;
        public String colour;
        public Boolean arrived = false;
        PISTimer(Station st, String h, String c) {
            this.headcode = h;
            this.station = st;
            this.colour = "§x§" + c.charAt(0) + "§" + c.charAt(1) + "§" + c.charAt(2) + "§" + c.charAt(3) + "§" + c.charAt(4) + "§" + c.charAt(5);
            this.timers = new ArrayList<Integer>();
        }

        public void setArrived(Boolean b) {
            if(b) {
                this.arrived = true;
                this.timers.removeIf(z -> (z < 1));
            } else {
                this.arrived = false;
            }
        }

        public void doStep() {
            timers2 = new ArrayList<Integer>();
            this.timers.forEach(timer -> {
                timers2.add(timer - 1);
            });
            this.timers = new ArrayList<Integer>(timers2);
        }

        public void addTimer(int a) {
            if(this.timers.size() > 0 && a < this.timers.get(this.timers.size() - 1)) {
                this.timers.add(a);
                this.timers.sort((Integer z, Integer y) -> y - z);
            } else {
                this.timers.add(a);
            }
        }

        public String getTime() {
            if(this.arrived) return "Arrived";
            if(this.timers.get(0) < 0 && !this.arrived) return "Delayed";
            if(this.timers.get(0) < -30) {
                this.timers.removeIf(z -> (z < -30));
                return "Cancelled";
            }
            if(this.timers.get(0) < 10) return "0:0" + this.timers.get(0);
            if(this.timers.get(0) < 60) return "0:" + this.timers.get(0);
            if(this.timers.get(0) % 60 < 10) return Math.floor(this.timers.get(0)/60) + ":0" + this.timers.get(0) % 60;
            return Math.floor(this.timers.get(0)/60) + ":" + this.timers.get(0) % 60;
        }
    }
}