package com.movies22.cashcraft.tc.flags;

import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;

public class BaseFlag {
	
	public String name = null;
	public Boolean required = false;
	
	public void onPreSpawn(MinecartGroup g) {
		
	}
	
	public void onSpawn(MinecartGroup g) {
		
	}
	
	public void onDespawn(MinecartGroup g) {
			
	}
	
	public void onStationEnter(MinecartGroup g, SignActionPlatform p) {
			
	}
	
	public void onStationExit(MinecartGroup g, SignActionPlatform p) {
		
	}
	
	public void onNodeEnter(MinecartGroup g, PathNode n) {
		
	}
	
	public void onNodeExit(MinecartGroup g, PathNode n) {
		
	}
	
	public void onRouteLoad(MinecartGroup g, PathRoute r) {
		
	}
}
