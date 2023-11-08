package com.movies22.cashcraft.tc.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;
import com.movies22.cashcraft.tc.utils.StationAnnouncements;

public class Station {
	public HashMap<String, SignActionPlatform> platforms = new HashMap<String, SignActionPlatform>();
	public String name;
	public String code;
	public String displayName;
	public List<MinecartGroup> groups = new ArrayList<MinecartGroup>();
	public List<MetroLine> lines = new ArrayList<MetroLine>();
	public Boolean canTerminate = false;
	public String headcode = null;
	
	public String hosi = "";
	public String osi = "";
	public String station = "";
	public Boolean closed = false;
	public HashMap<String, String> ann = new HashMap<String, String>();
	public Station(String code, String name, String displayName) {
		this.code = code;
		this.name = name;
		if(displayName != null && displayName != "") {
			this.displayName = displayName;
		} else {
			this.displayName = name;
		}
	}
	
	public void setOsi(String s) {
		setOsi(s, false);
	}
	
	public void setOsi(String s, Boolean heavy) {
		if(s.equals("FALSE") || s.equals("TRUE")) {
			return;
		}
		if(heavy) {
			this.hosi = s;
		} else {
			this.osi = s;
		}
	}	
	
	public Boolean addPlatform(String i, SignActionPlatform a) {
		this.platforms.put(i, a);
		if(!this.lines.contains(a.node.line)) {
			this.lines.add(a.node.line);
		}
		return true;
	}
	
	public Boolean removePlatform(String i) {
		this.platforms.remove(i);
		return true;
	}
	
	public String generateConnection( MetroLine l) {
		if(ann.get(l.getName()) == null) {
		if(this.station.equals("")) return null;
		Station s = TrainCarts.plugin.StationStore.getFromCode(this.station);
		if(s == null) {
			return null;
		}
		String a = "";
		if(!s.osi.equals("")) {
			if(!s.hosi.equals("")) {
				a = StationAnnouncements.parseMetro(s.osi, l, true).replaceAll("\\]", ", ");
				a = a + StationAnnouncements.parseRail(s.hosi, l, true, true);
			} else {
				a = StationAnnouncements.parseMetro(s.osi, l, true).replaceAll(".", "") + ", {\"text\":\" services at {STATION}.\", \"color\":\"" + l.getColour() + "\"}]";
			}
		} else {
			a = StationAnnouncements.parseRail(s.hosi, l, false, true);
		}
		if(a != null && !a.equals("")) {
			a = a.replaceAll("\\{STATION}", s.name);
			ann.put(l.getName(), a);
			return a;
		} else {
			return null;
		}
		} else {
			String a = ann.get(l.getName());
			Station s = TrainCarts.plugin.StationStore.getFromCode(this.station);
			if(a != null && !a.equals("")) {
				a = a.replaceAll("\\{STATION}", s.name);
				return a;
			} else {
				return null;
			}
		}
	}
}
