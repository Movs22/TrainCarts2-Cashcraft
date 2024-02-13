package com.movies22.cashcraft.tc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.bergerkiller.bukkit.sl.API.Variables;

public class PisController extends BaseController {
	private HashMap<String, PIS> pis;
	
	public PisController() {
		this.pis = new HashMap<String, PIS>();
	}
	
	public PIS getPis(String s) {
		PIS a = this.pis.get(s);
		if(a == null) {
			a = new PIS(s);
			this.pis.put(s, a);
		}
		return a;
	}
	
	public void updateSigns() {
		this.pis.values().forEach(pis -> {
			if(!pis.lastVar.equals(pis.variable)) {
				Variables.get(pis.name).set(pis.variable);
				pis.lastVar = pis.variable;
			}
		});
	}
	
	
	public void clear() {
		this.pis.clear();
	}
	
	@Override
	public void doFixedTick() {
		this.pis.values().forEach(pis -> {
			pis.doStep();
		});
		
	}
	
	
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
				} else if(b < 59) {
					return a + ":" + (b - b % 5 + 5);
				} else {
					return a + ":00";
				}
			} else {
				if(g < 5) {
					return "0:05";
				} else {
					return "0:" + (g - g % 5 + 10);
				}
			}
		}
	}
}
