package com.movies22.cashcraft.tc.controller;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.movies22.cashcraft.tc.TrainCarts;

public class PlayerController extends BaseController {
	
	private List<Location> locs = new ArrayList<Location>();
	
	
	public Boolean hasToLoad(Location l) {
		Boolean a = false;
		for(Location loc : this.locs) {
			if(l.distance(loc) < 32) {
				a = true;
				break;
			}
		};
		return a;
	}
	
	
	@Override
	public void doFixedTick() {
		this.locs.clear();
		TrainCarts.plugin.getServer().getOnlinePlayers().forEach(p -> {
			Location l = p.getLocation();
			if(l.getWorld().getName().equals("Main1")) {
				locs.add(p.getLocation());
			}
		});
	}
}
