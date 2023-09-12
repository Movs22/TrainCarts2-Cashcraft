package com.movies22.cashcraft.tc.PathFinding;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;
import com.movies22.cashcraft.tc.signactions.SignActionBlocker;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;
import com.movies22.cashcraft.tc.signactions.SignActionRBlocker;

public class PathRoute implements Cloneable {
	public PathNode _start;
	public PathNode _end;
	public String name;
	public MetroLine _line;
	public List<PathOperation> route = new ArrayList<PathOperation>();
	public List<SignActionPlatform> stops = new ArrayList<SignActionPlatform>();
	public List<PathNode> nodes = new ArrayList<PathNode>();
	public String reverse;
	public PathRoute(String n, List<PathNode> s, MetroLine l) {
		if(s.size() == 0) {
			return;
		}
		this._start = s.get(0);
		this._end = s.get(s.size()-1);
		this.nodes = s;
		this.name = n;
		this._line = l;
	}
	
	@Override
	public String toString() {
		return this._line + ":" + this.name;
	}
	
	@Override
	public PathRoute clone() {
		PathRoute a = new PathRoute(this.name, this.nodes, this._line);
		a.route = new ArrayList<PathOperation>();
		this.route.forEach(op -> {
			a.route.add(op.clone());
		});
		a.stops = new ArrayList<SignActionPlatform>(this.stops);
		a.nodes = new ArrayList<PathNode>(nodes);
		a.reverse = this.reverse;
		return a;
	}
	
	public List<PathOperation> getRoute() {
		return this.route;
	}
	
	public Boolean setReverse(String r) {
		this.reverse = r;
		return true;
	}
	
	public String getReverse() {
		return this.reverse;
	}
	private int i;
	public void calculate() {
		route = new ArrayList<PathOperation>();
		stops = new ArrayList<SignActionPlatform>();
		for(i = 0; i < (this.nodes.size() - 1); i++) {
			List<PathOperation> s = this.calculateSection(this.nodes.get(i), this.nodes.get(i+1));
			List<PathOperation> s2 = new ArrayList<PathOperation>();
			s.forEach(a -> {
				PathOperation z = a.clone();
				/*if(i == 0 && s.indexOf(a) == 0) {
						z.locs.remove(0);
				}*/
				s2.add(z);
			});
			this.route.addAll(s2);
		}
		this.route.forEach(con -> {
			this._line.addNode(con.getStartNode());
			this._line.addNode(con.getEndNode());
		});
	}
	
	PathNode start;
	double sD;
	double oX;
	double oZ;
	PathOperation connection;
	boolean finished;
	private List<Location> checkednodes;
	@SuppressWarnings("unused")
	private int z = 0;
	public List<PathOperation> calculateSection(PathNode st, PathNode en) {
		checkednodes = new ArrayList<Location>();
		List<PathOperation> route = new ArrayList<PathOperation>();
		PathNode end = en;
		finished = false;
		int i = 0;
		start = st;
		/*if(start.getAction() instanceof SignActionPlatform) {
			if(!stops.contains((SignActionPlatform) start.getAction())) {
				stops.add((SignActionPlatform) start.getAction());
			}
		}*/
		sD = Double.MAX_VALUE;
		oX = Double.MAX_VALUE;
		oZ = Double.MAX_VALUE;
		connection = null;
		checkednodes.add(start.loc);
		PathNode lastNode = null;
		while (!finished) {
			z = 0;
			start.connections.forEach(con -> {
				Location e = end.getLocation();
				if(con.getEndNode() == null) {
					return;
				}
				if (con.getEndNode() != null && !con.getEndNode().equals(start) && !checkednodes.contains(con.getEndNode().loc)) {
					Location c = con.getEndNode().getLocation();
					double a = e.distance(c);
					if(con.getEndNode().getAction().getClass().equals(SignActionBlocker.class)) {
						SignActionBlocker b = (SignActionBlocker) con.getEndNode().getAction();
						if(b.getBlocked(con.getEndNode().sign).equals(con.getFacing())) {
							z++;
							return;
						};
					}
					if(con.getEndNode().getAction().getClass().equals(SignActionRBlocker.class)) {
						SignActionRBlocker b = (SignActionRBlocker) con.getEndNode().getAction();
						if(b.isBlocked(this)) {
							z++;
							return;
						};
					}
					if (((a < sD+1) && ((Math.abs(e.getX() - c.getX()) < oX || Math.abs(e.getZ() - c.getZ()) < oZ))) || (a < sD+1)) {
						sD = a;
						oX = Math.abs(e.getX() - c.getX());
						oZ = Math.abs(e.getZ() - c.getZ());
						connection = con.clone();
						if (a < 1D) {
							if(con.getEndNode().getAction() instanceof SignActionPlatform && !stops.contains((SignActionPlatform) con.getEndNode().getAction())) {
								stops.add((SignActionPlatform) con.getEndNode().getAction());
							}
							route.add(con);
							finished = true;
							return;
						}
					}
				} else {
					z++;
				}
			});
			if(!finished && connection != null) {
				if(start != null && connection.getEndNode() != null) {
					route.add(connection);
					checkednodes.add(connection.getEndNode().loc);
					lastNode = start;
					start = connection.getEndNode();
					if(start.getAction() != null && start.getAction() instanceof SignActionPlatform && !stops.contains((SignActionPlatform) start.getAction())) {
						stops.add((SignActionPlatform) start.getAction());
					}
				} else {
					sD = Double.MAX_VALUE;
				}
			};
			i++;
			if(lastNode != null && lastNode.equals(start)) {
				finished = true;
				this._line.deleteRoute(this.name);
				TrainCarts.plugin.getLogger().log(Level.WARNING, "Route " + this.name + " looped at " + st.getLocationStr() + ">" + end.getLocationStr());
				break;
			}
			if(i > 1000) {
				finished = true;
				this._line.deleteRoute(this.name);
				TrainCarts.plugin.getLogger().log(Level.WARNING, "Route " + this.name + " couldn't reach its destination."  + st.getLocationStr() + ">" + end.getLocationStr());
				break;
			}
		}
		return route;
	}
}
