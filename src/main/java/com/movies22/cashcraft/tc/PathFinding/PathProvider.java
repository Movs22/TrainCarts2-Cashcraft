package com.movies22.cashcraft.tc.PathFinding;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Location;

import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;

public class PathProvider {
	private MetroLine line;
	public PathProvider(MetroLine l) {
		this.setLine(l);
	}
	
	public MetroLine getLine() {
		return line;
	}
	public void setLine(MetroLine line) {
		this.line = line;
	}
	
	@SuppressWarnings("unchecked")
	public Boolean reroute() {
		Iterator<PathNode> nodes = getLine().getNodes().values().iterator();
		while (nodes.hasNext()) {
            PathNode a = nodes.next();
            a.connections.clear();
            a.facings.clear();
        }
		nodes = getLine().getNodes().values().iterator();
		while (nodes.hasNext()) {
            PathNode a = nodes.next();
            if(a.sign != null) {
            	a.handleBuild(a.loc, a.sign.getBlock(), getLine());
            	a.reroute();
            } else {
            	getLine().getNodes().remove(a);
            }
        }
		return null;
	}
}
