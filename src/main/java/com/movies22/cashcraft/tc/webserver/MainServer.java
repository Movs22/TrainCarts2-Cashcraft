package com.movies22.cashcraft.tc.webserver;

import java.util.ArrayList;
import java.util.List;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.api.MetroLines;
import com.movies22.cashcraft.tc.api.MinecartMember;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;
import com.movies22.cashcraft.tc.controller.PisController.PIS;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.api.Station;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;

import express.Express;
import express.utils.Status;

public class MainServer {
	private Express app;
	private TrainCarts tc;
	private MetroLines lines;
	public MainServer() {
		this.tc = TrainCarts.plugin;
		this.lines = TrainCarts.plugin.lines;
	}
	
	public String getOsi(String s) {
		String z = "[";
		char z2;
		for(int i = 0; i < s.length(); i++) {
			z2 = s.charAt(i);
			MetroLine l = lines.getFromChar("" + z2);
			if(i != 0) z = z + ", ";
			z = z + "\"" + l.getName() + "\"";
		}
		z = z + "]";
		return z;
	}
	
	public List<String> getOsi(String s, Boolean b) {
		List<String> z = new ArrayList<String>();
		char z2;
		for(int i = 0; i < s.length(); i++) {
			z2 = s.charAt(i);
			MetroLine l = lines.getFromChar("" + z2);
			z.add(l.getName());
		}
		return z;
	}
	
	public String getStationInfo(Station s) {
		String plat = "[";
		int x = Integer.MAX_VALUE;
		char x2 = '?';
		int i = 0;
		for(String p : s.platforms.keySet()) {
			if(i != 0) plat = plat + ", ";
			plat = plat + "\"" + p + "\"";
			i++;
		}
		for(SignActionPlatform p2 : s.platforms.values()) {
			for(PIS pis : p2.pis.values()) {
				if(x > pis.delay) {
					x = pis.delay;
					x2 = pis.name.charAt(-1);
				}
			};
		}
		plat = plat + "]";
		return "{\"name\":\"" + s.name + "\",\"code\":\"" + s.code + "\",\"lines\":" + getOsi(s.osi) + ", \"platforms\":" + plat + ", \"nextTrain\":" + x + ", \"nextTrainChar\":\"" + String.valueOf(x2) + "\"}";
	}
	
	public String getTrainInfo(MinecartGroup s) {
		String stops = "[";
		int i = 0;
		for(SignActionPlatform p : s.currentRoute.stops) {
			if(i != 0) stops = stops + ", ";
			stops = stops + "\"" + p.station.name + " (Platform " + p.platform + ")\"";
			i++;
		}
		stops = stops + "]";
		return "{\"headcode\":\"" + s.getHeadcode() + "\",\"length\":\"" + s._getLength() + "\",\"route\":\"" + s.currentRoute._line.getName() + ":" + s.currentRoute.name + "\", \"nextService\":" + s.nextTrain + ", \"stops\":" + stops +", \"virtualized\":" + s.virtualized + ", \"location\": \"" + s.head().getLocation().getBlockX() + "/" + s.head().getLocation().getBlockZ() + "\", \"vec\": \"" + s.head().getEntity().getVelocity().getX() + "/" + s.head().getEntity().getVelocity().getZ()  + "\"}";
	}
	
	public String getLineInfo(MetroLine s) {
		String stops = "[";
		int i = 0;
		for(String p : s.getRoutes().keySet()) {
			if(i != 0) stops = stops + ", ";
			stops = stops + "\"" + p + "\"";
			i++;
		}
		stops = stops + "]";
		return "{\"name\":\"" + s.getName() + "\",\"character\":\"" + s.getChar() + "\",\"colour\":\"" + s.getColour() + "\", \"routes\":" + stops + "}";
	}
	
	public String getRouteInfo(PathRoute s) {
		String stops = "[";
		String stopsR = "[";
		stops = stops + "\"" + ((SignActionPlatform) s._start.getAction()).station.name + "~" + ((SignActionPlatform) s._start.getAction()).platform + "\"";
		stopsR = stopsR + "\"" + (s._start.getAction()).node.loc.getBlockX() + "/" + (s._start.getAction()).node.loc.getBlockZ() + "\"";
		for(SignActionPlatform p : s.stops) {
			stops = stops + ", ";
			stops = stops + "\"" + p.station.name + "~" + p.platform + "\"";
			stopsR = stopsR + ", ";
			stopsR = stopsR + "\"" + p.node.loc.getBlockX() + "/" + p.node.loc.getBlockZ() + "\"";
		}
		stops = stops + "]";
		return "{\"name\":\"" + s.name + "\",\"line\":\"" + s._line.getName() + "\",\"reverse\":\"" + s.reverse + "\", \"stops\":" + stops + ", \"stopsLoc\":" + stopsR+ "}";
	}
	
	
	public void enable() {
		this.app = new Express();

		this.app.get("/", (req, res) -> {
			res.send("{\"version\":\"" + tc.version + "\", \"mcVersion\":\"" + tc.mcVersion + "\"}");
		});
		this.app.get("/stations", (req, res) -> {
			String s = "";
			int i = 0;
			for(Station station : tc.StationStore.Stations.values()) {
				if(i != 0) s = s + ", ";
				s = s + getStationInfo(station);
				i++;
			}
			res.send("{\"stations\":[" + s + "]}");
		});
		this.app.get("/trains", (req, res) -> {
			String s = "";
			int i = 0;
			for(MinecartMember train : tc.MemberController.getHeads()) {
				MinecartGroup g = train.getGroup();
				if(i != 0) s = s + ", ";
				s = s + getTrainInfo(g);
				i++;
			}
			res.send("{\"trains\":[" + s + "]}");
		});
		
		this.app.get("/lines", (req, res) -> {
			String s = "";
			int i = 0;
			for(MetroLine l : tc.lines.getLines().values()) {
				if(i != 0) s = s + ", ";
				s = s + getLineInfo(l);
				i++;
			}
			res.send("{\"lines\":[" + s + "]}");
		});
		
		this.app.get("/station/:station", (req, res) -> {
			Station s = tc.StationStore.getFromName(req.getParam("station").replaceAll("-", " ").replaceAll("%2D", " "));
			if(s == null) {
				s = tc.StationStore.getFromCode(req.getParam("station"));
			}
			if(s == null) {
				res.sendStatus(Status._404);	
			}
			res.send(getStationInfo(s));
		});
		
		this.app.get("/lines/:line", (req, res) -> {
			MetroLine l = tc.lines.getLine(req.getParam("line"));
			if(l == null) {
				res.sendStatus(Status._404);	
			}
			res.send(getLineInfo(l));
		});
		
		this.app.get("/routes/:line/:route", (req, res) -> {
			MetroLine l = tc.lines.getLine(req.getParam("line"));
			if(l == null) {
				res.sendStatus(Status._404);	
			}
			PathRoute r = l.getRoute(req.getParam("route"));
			res.send(getRouteInfo(r));
		});
		
		this.app.get("/train/:train", (req, res) -> {
			MinecartGroup g = null;
			for(MinecartMember mh : tc.MemberController.getHeads()) {
				if(mh.getGroup().getHeadcode().equals(req.getParam("train"))) {
					g = mh.getGroup();
				}
			};
			if(g == null) {
				res.sendStatus(Status._404);	
			}
			res.send(getTrainInfo(g));
		});


		this.app.listen(25566);
	
	}
	
	public void disable() {
		this.app.stop();
	}
}
