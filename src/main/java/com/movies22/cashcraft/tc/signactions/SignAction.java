package com.movies22.cashcraft.tc.signactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.pathFinding.PathNode;
import com.movies22.cashcraft.tc.pathFinding.PathRoute;

public class SignAction implements Cloneable {
	
	public static List<SignAction> actions = Collections.emptyList();
	public List<MinecartGroup> executed = new ArrayList<MinecartGroup>();
	public List<MinecartGroup> ExitExecuted = new ArrayList<MinecartGroup>();
	public PathNode node;
	public Sign sign;
	public String content;
    public static void init() {
        actions = new ArrayList<>();
        register(new SignActionPlatform());
        register(new SignActionBuffer());
        register(new SignActionSwitcher());
        register(new SignActionSpawner());
        register(new SignActionBlocker());
        register(new SignActionStop());
        register(new SignActionSpeed());
        register(new SignActionRBlocker());
        register(new SignActionDestroy());
    }
	
    public HashMap<PathNode, PathRoute> rcache = new HashMap<PathNode, PathRoute>();
    private String s;
	public PathRoute getRoute(PathNode e) {
		return getRoute(e, false);
	}
	public PathRoute getRoute(PathNode e, Boolean skip) {
		PathRoute r = this.rcache.get(e);
		if(r == null) {
			List<PathNode> a = new ArrayList<PathNode>();
			a.add(this.node);
			a.add(e);
			s = "[";
			if(e.getAction() instanceof SignActionPlatform) {
				s = "[S";
			}
			r = new PathRoute(s + "CACHED ROUTE/" + e.getLocationStr() + "]", a, TrainCarts.plugin.global);
			r.calculate();
			if(skip) {
				for(int i = 0; i < (r.stops.size()-1); i++) {
					r.stops.remove(0);
				}
			}
			this.rcache.put(e, r);
			return r.clone();
		} else {
			if(skip) {	
				for(int i = 0; i < (r.stops.size()-1); i++) {
					r.stops.remove(0);
				}
			}
			return r.clone();
		}
	}
    public static <T extends SignAction> T register(T action) {
    	actions.add(action);
		return action;
    }
    
    public Boolean match(String s) {
    	return false;
    }
    
    public Boolean execute(MinecartGroup group) {
    	return false;
    }
    
    public Boolean exit(MinecartGroup group) {
    	return false;
    }
    
    public String getAction() {
		return null;
    }
    
    public Double getSpeedLimit(MinecartGroup group) {
    	return null;
    }
    
    public BlockFace getBlocked(Sign s) {
    	return null;
    }
    
    public void postParse() {
    	return;
    }
    
	public static SignAction parse(Sign s) {
		String a = "";
		for(int i = 0; i < 4; i++) {
			a = a + s.getLine(i);
		}
		if(!a.startsWith("t:")) {
			return null;
		}
		String[] b = a.split(" ");
		for(SignAction c : SignAction.actions) {
			if(c.match(b[0])) {
				SignAction d = null;
				try {
					d = (SignAction) c.clone();
					d.executed = new ArrayList<MinecartGroup>();
					d.ExitExecuted = new ArrayList<MinecartGroup>();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				d.sign = s;
				d.content = a;
				return d;
			}
		};
		return null;
	}
	
	public void handleBuild(Player p) {
		p.sendMessage(this.getClass().toString());
	}
}
