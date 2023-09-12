package com.movies22.cashcraft.tc.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.logging.Level;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.api.Depot;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.signactions.SignActionSpawner;
import com.movies22.cashcraft.tc.utils.Despawn;

public class DepotStore {
	public HashMap<String, Depot> depots;
	public HashMap<String, Integer> headcodes = new HashMap<String, Integer>();
	//CODE > VARIABLE


	public DepotStore() {
		this.depots = new HashMap<String, Depot>();
	}

	
	public Depot createDepot(String c, String s) {
		Depot a = new Depot(c, s);
		depots.putIfAbsent(c, a);
		return a;
	}
	
	public void addDepot(Depot s) {
		depots.putIfAbsent(s.code, s);
	}

	public void removeDepot(Depot s) {
		depots.remove(s.code);
	}
	
	public void removeDepot(String s) {
		depots.remove(s);
	}

	public Depot getFromCode(String n) {
		return depots.get(n);
	}
	
	private Depot b;
	public Depot getFromName(String n) {
		b = null;
		depots.values().forEach(a -> {
			if(a.name == n) {
				b = a;
			}
		});
		return b;
	}
	
	public String getNextHeadcode(String s) {
		int i = 0;
		if(headcodes.get(s) != null) {
			i = headcodes.get(s);
		}
		if(i > 99) {
			i = 0;
		}
		String headcode = s + i;
		if(i < 10) {
			headcode = s + "0" + i;
		};
		i++;
		headcodes.put(s, i);
		return headcode;
	}
	public void doFixedTick() {
		this.depots.values().forEach(depot -> {
			depot.routerate.values().forEach(v -> {
				long n = LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toInstant().toEpochMilli() - 60*60*1000;
				if(v.getNextSpawnTime() == null) {
					return;
				}
				long z = v.getNextSpawnTime()._timestamp;
				if(n >= z) {
					SignActionSpawner lane = depot.getRandomLane();
					PathRoute a = depot.routes.get(v.route.split(":")[1]);
					if(a != null) {
						String headcode = "[AWAITING HEADCODE]";
						Boolean finished = false;
						PathRoute r = lane.getRoute(a._start).clone();
						r.reverse = v.route;
						PathRoute b = r.clone();
						MinecartGroup m = new MinecartGroup(a._line, headcode, v.getNextTrain());
						PathRoute b2 = b.clone();
						m.addRoute(b2);
						while(!finished) {
							if(b.reverse == null) {
								finished = true;
								continue;
							}
							if(b.reverse.equals("ECS")) {
								m.addRoute(m.getLastRoute()._end.getAction().getRoute(lane.node).clone());
								finished = true;
							} else if(!b.reverse.equals("none")) {
								MetroLine c = TrainCarts.plugin.lines.getLine(b.reverse.split(":")[0]);
								b = c.getRoute(b.reverse.split(":")[1]);
								if(b == null) {
									finished = true;
									m.destroy(Despawn.INVALID_ROUTE);
								}
								m.addRoute(b.clone());
							} else if(b.reverse.equals("none")) {
								PathRoute y = m.getLastRoute()._end.getAction().getRoute(lane.node).clone();
								y.name = "DESPAWN";
								m.addRoute(y);
								finished = true;
							}
						}
						if(v.getNextSpawnTime(1) == null) { 
							m.nextTrain = Integer.MAX_VALUE;
						} else {
							long y = v.getNextSpawnTime(1)._timestamp;
							m.nextTrain = (int) ((y - z) / 1000);
						}
						m.spawn(lane.node);
					}
				};
			});
		});
	}
}
