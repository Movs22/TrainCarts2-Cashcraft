package com.movies22.cashcraft.tc.controller;

import java.util.Arrays;
import java.util.HashMap;

import com.movies22.cashcraft.tc.api.Station;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;

public class StationStore {
	public HashMap<String, Station> Stations;
	//CODE > VARIABLE
	
	public MetroLine line;

	public StationStore() {
		this.Stations = new HashMap<String, Station>();
	}

	
	public Station createStation(String c, String s, String s2) {
		Station a = new Station(c, s, s2);
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
			if(a.name.toLowerCase().equals(n.toLowerCase())) {
				b = a;
			}
		});
		return b;
	}
	
	private String z;
	public void postParse() {
		Stations.values().forEach(station -> {
			z = "";
			station.platforms.values().forEach(p -> {
				if(!z.contains(p.node.line.getChar())) {
					z = z + p.node.line.getChar();
				}
			});	
			String z2[] = z.split("");
			Arrays.sort(z2);
			station.osi = String.join("", z2);
		});
	}
}
