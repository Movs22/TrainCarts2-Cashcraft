package com.movies22.cashcraft.tc.pis;

import java.util.ArrayList;
import java.util.List;

public class PIS {
    public String name;
    public String variable = "--:--";
    public String lastVar = "--:--";
    public int delay;
    public List<Integer> timers = new ArrayList<Integer>();
    PIS(String n) {
        this.name = n;
    }
    
    public void setArrived(Boolean b) {
        if(b) {
            this.variable = "Arrived";
            if(this.timers.size() > 0) {
                if(this.timers.get(0) < 30) {
                    this.timers.remove(0);
                }
            }
        } else {
            this.variable = getTime(getNextTimer());
        }
    }
    
    public void addTimer(int a) {
        if(a > 10000000) {
            this.variable = "No Services";
            return;
        }
        if(this.timers.size() > 0 && a < this.timers.get(this.timers.size() - 1)) {
            this.timers.add(a);
            this.timers.sort((Integer z, Integer y) -> y - z);
        } else {
            this.timers.add(a);
        }
    }
    
    public Integer getNextTimer() {
        for(int i = 0; i < this.timers.size(); i++) {
            if(this.timers.get(i) < this.delay || this.timers.get(i) < -30) {
                this.timers.remove(i);
            }
        }
        if(this.timers.size() > 0) {
            return this.timers.get(0);
        } else {
            return 0;
        }
    }
    private List<Integer> timers2;
    public void doStep() {
        timers2 = new ArrayList<Integer>();
        this.timers.forEach(timer -> {
            timers2.add(timer - 1);
        });
        this.timers = new ArrayList<Integer>(timers2);
        if(this.timers.size() == 0) {
            this.variable = "No Services";
            return;
        }
        this.delay = -this.timers.get(0);
        if(this.timers.get(0) < 0 && !this.variable.equals("Arrived")) {
            if(this.timers.get(0) > -31) {
            this.variable = "0:05";
            } else {
                this.variable = "Cancelled";
                if(this.timers.size() > 0) {
                    this.timers.remove(0);
                    if(this.timers.size() > 0) {
                        this.delay = -this.timers.get(0);
                    } else {
                        this.delay = 0;
                    }
                } else {
                    this.delay = 0;
                }
            }
        } else {
            if(this.variable.equals("Arrived")) {
                return;
            }
            this.variable = getTime(this.timers.get(0));
        }
    }
    
    public String getTime(int g) {
        if(g > 45) {
            int b = g % 60;
            int a = (g - b % 5 + 5) / 60;
            if(b < 5) {
                return a + ":05";
            } else if(b < 55) {
                return a + ":" + (b - b % 5 + 5);
            } else {
                return a + ":00";
            }
        } else {
            if(g < 5) {
                return "0:05";
            } else {
                return "0:" + (g - g % 5 + 5);
            }
        }
    }
}