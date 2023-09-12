package com.movies22.cashcraft.tc.controller;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.block.Sign;

import com.movies22.cashcraft.tc.api.MetroLines;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;

public class SignStore {
	public HashMap<Location, Sign> signs;
	//location >> sign variable
	public MetroLine line;

	public SignStore() {
		this.signs = new HashMap<Location, Sign>();
	}
	
	public void addSign(Sign s) {
		this.signs.putIfAbsent(s.getLocation(), s);
	}
	
	public Sign getSign(Location l) {
		return this.signs.get(l);
	}
	
	public Sign getSign(Sign s) {
		return this.signs.get(s.getLocation());
	}
}
