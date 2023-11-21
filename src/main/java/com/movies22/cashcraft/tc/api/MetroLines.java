package com.movies22.cashcraft.tc.api;

import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.PathFinding.PathProvider;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;

public class MetroLines {
	private HashMap<String, MetroLine> lines = new HashMap<String, MetroLine>();
	
	public MetroLine getLine(String s) {
		return lines.get(s);
	}
	
	public MetroLine deleteLine(String s) {
		return lines.remove(s);
	}
	
	public MetroLine createLine(String s, String c) {
		MetroLine a = new MetroLine(s, c);
		lines.putIfAbsent(s, a);
		return a;
	}
	
	public void addLine(String s, MetroLine c) {
		lines.putIfAbsent(s, c);
	}
	
	public HashMap<String, MetroLine> getLines() {
		return this.lines;
	}
	
	public void clearLines() {
		this.lines.clear();
		this.lines = null;
	}
	
	private MetroLine l;
	public MetroLine getFromChar(String s) {
		l = null;
		this.lines.values().forEach(l2 -> {
			if(l2.getChar().equals(s)) {
				l = l2;
			}
		});
		return l;
	}
	
	public class MetroLine {
		private String name;
		private String colour;
		private String character = "";
		private HashMap<Location, PathNode> nodes = new HashMap<Location, PathNode>();
		//<Location, PathNode>
		//TODO: Add option to get by node name
		private HashMap<String, MinecartGroup> trains = new HashMap<String, MinecartGroup>();
		//<HeadCode, Group>
		private HashMap<String, PathRoute> routes = new HashMap<String, PathRoute>();
		private PathProvider provider;
		public MetroLine(String s, String c) {
			this.name = s;
			this.colour = c;
			this.provider = new PathProvider(this);
		}
		
		public PathNode getNode(Location s) {
			return this.nodes.get(s);
		}
		
		public HashMap<Location, PathNode> getNodes() {
			return this.nodes;
		}
		
		public Collection<MinecartGroup> getTrains() {
			return this.trains.values();
		}
		
		public void addNode(PathNode p) {
			nodes.put(p.getLocation(), p);
			p.line = this;
		}
		
		public PathNode createNode(Location a) {
			if(getNode(a) != null) return getNode(a);
			Location b = new Location(a.getWorld(), a.getX(), a.getY(), a.getZ());
			Block c = b.subtract(0, 2, 0).getBlock();
			return createNode(a, c);
		}
		
		public PathNode createNode(Location a, Block s) {
			if(getNode(a) != null) return getNode(a);
			//if(!(a.getBlock().getBlockData() instanceof Rail)) return null;
			PathNode n = new PathNode(a, s, this);
			addNode(n);
			return n;
		}
		
		public void deleteNode(Location a) {
			nodes.remove(a);
		}
		
		public void deleteNode(PathNode a) {
			nodes.remove(a.loc);
		}
		
		public void deleteNode(Sign a) {
			nodes.remove(a.getLocation());
		}
		
		public void reroute() {
			this.provider.reroute();
		}
		
		public String getName() {
			return this.name;
		}
		
		public String getColour() {
			return this.colour;
		}
		
		public void addTrain(MinecartGroup g) {
			g.setLine(this);
			this.trains.putIfAbsent(g.getHeadcode(), g);
		}
		
		public MinecartGroup getTrain(MinecartGroup s) {
			return this.trains.get(s.getHeadcode());
		}
		
		public MinecartGroup getTrain(String s) {
			return this.trains.get(s);
		}
		
		public MinecartGroup removeTrain(String s) {
			MinecartGroup g = this.trains.get(s);
			if(g == null) return null;
			return removeTrain(g);
		}
		
		public MinecartGroup removeTrain(MinecartGroup g) {
			if(g == null) return null;
			return this.trains.remove(g.getHeadcode());
		}

		public HashMap<String, PathRoute> getRoutes() {
			return routes;
		}
		
		public void addRoute(PathRoute r, String s) {
			if(r.route.size() == 0) {
				r.calculate();
			}
			this.routes.putIfAbsent(s, r);
		}
		
		public PathRoute getRoute(String s) {
			return this.routes.get(s);
		}
		
		public void deleteRoute(String s) {
			this.routes.remove(s);
		}

		public String getChar() {
			return character;
		}

		public void setChar(String character) {
			this.character = character;
		}
	}
}
