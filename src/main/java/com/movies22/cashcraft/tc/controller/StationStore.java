package com.movies22.cashcraft.tc.controller;

import java.util.HashMap;

import com.movies22.cashcraft.tc.api.MetroLines;
import com.movies22.cashcraft.tc.api.Station;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;

public class StationStore {
	public HashMap<String, Station> Stations;
	//CODE > VARIABLE
	
	public MetroLine line;

	public StationStore() {
		this.Stations = new HashMap<String, Station>();
	}

	
	public Station createStation(String c, String s) {
		Station a = new Station(c, s);
		Stations.putIfAbsent(c, a);
		return a;
	}
	
	public void addStation(Station s) {
		Stations.putIfAbsent(s.code, s);
	}

	public void removeStation(Station s) {
		Stations.remove(s.code);
	}
	
	public void removeStation(String s) {
		Stations.remove(s);
	}

	public Station getFromCode(String n) {
		return Stations.get(n);
	}
	
	private Station b;
	public Station getFromName(String n) {
		b = null;
		Stations.values().forEach(a -> {
			if(a.name == n) {
				b = a;
			}
		});
		return b;
	}
}
