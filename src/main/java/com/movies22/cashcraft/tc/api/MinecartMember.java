package com.movies22.cashcraft.tc.api;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.TextDisplay;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.PathFinding.PathOperation;
import com.movies22.cashcraft.tc.signactions.SignAction;
import com.movies22.cashcraft.tc.signactions.SignActionBlocker;

public class MinecartMember implements Comparable<MinecartMember> {
	private MinecartGroup group;
	private VirtualMinecart entity;
	public Double _mod = 1.0;
	public Double currentSpeed = 0.0;
	private List<PathOperation> route = null;
	public Double _targetSpeed = 0.4;
	public PathNode prevNode = null;
	public int index;
	public TextDisplay destination;
	public String destinationPos;
	public PathOperation lastCon = null;
	public BlockFace facing;
	public Chunk lastChunk;
	public SignAction lastAction;
	public Boolean spawned = false;
	public Boolean virtualized = true;
	MinecartMember(MinecartGroup g, Minecart e, int i) {
		this.setGroup(g);
		this.facing = g._getSpawn().direction;
		this.index = i;
		this.spawned = false;
		if(i == 0) {
			this.setEntity(new VirtualMinecart(e, this, 0.0d));
		} else {
			this.setEntity(new VirtualMinecart(e, group.head(), i*1.5d));
		}
		TrainCarts.plugin.MemberController.addMember(e.getUniqueId(), this);
	}
	
	public void setPivot(MinecartMember mm) {
		this.entity.setPivot(mm);
	}
	
	public Location getLocation() {
		return this.entity.getLocation();
	}
	
	public Boolean destroy() {
		try {
			TrainCarts.plugin.MemberController.removeMember(this);
			if(this.getEntity() != null) {
				this.getEntity().remove();
			}
			this.route.clear();
			this.route = null;
			return true;
		} catch(Error e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Boolean eject() {
		try {
			if(this.getEntity() != null) {
				this.getEntity().eject();
			}
			return true;
		} catch(Error e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public MinecartGroup getGroup() {
		return group;
	}
	public void setGroup(MinecartGroup group) {
		this.group = group;
	}
	public Minecart getEntity() {
		return (Minecart) this.entity.getEntity();
	}
	public void setEntity(VirtualMinecart e) {
		this.entity = e;
	}
	
	public MinecartMember nextCart() {
		int nc = this.group.getMembers().indexOf(this);
		if(nc < 1 || nc > (this.group.getMembers().size() - 1)) return null;
		return this.group.getMember(nc - 1);
	}
	
	public List<PathOperation> getLocalRoute() {
		return route;
	}
	
	public Location getNextLocation() {
		if(this.route == null) return null;
		if(this.route.size() == 0) {
			this.group.destroy();
			return null;
		}
		if(this.route.get(0).locs.size() < 1) {
			if(this.route.size() > 1) {
				this.lastCon = this.route.get(0).clone();
				this.route.remove(0);
			} else {
				this.lastCon = this.route.get(0).clone();
				this.route.remove(0);
				this.loadNextRoute();
			} 
		}
		if(this.route.size() == 0) {
			this.group.destroy();
			return null;
		}
		if(this.route.get(0).locs.size() > 0) {
			return this.route.get(0).locs.get(0);
		} else {
			this.group.destroy();
			return null;
		}
	}
	
	public PathNode getNextNode() {
		return this.getNextNode(0, false);
	}
	
	public PathNode getNextNode(int i) {
		return this.getNextNode(i, false);
	}
	
	public PathNode getNextNode(int i, boolean b) {
		if(this.route == null) return null;
		if(i == 0) {
			if(this.route.size() > 1) {
				if(this.route.get(0).locs.size() == 0) {
					if(this.route.size() > 1) {
						this.lastCon = this.route.get(i).clone();
						this.route.remove(0);
					}
					}
				}
		} else {
			if(this.route.size() > 1) {
				if(this.route.get(i).locs.size() == 0) {
					if(this.route.size() > 1) {
						this.lastCon = this.route.get(i).clone();
					for(int z = 0; z <= i; z++) {
						this.route.remove(0);
					}
				} else {
					this.lastCon = this.route.get(i).clone();
					this.route.remove(0);
					this.loadNextRoute();
					for(int z = 0; z < i; z++) {
						this.route.remove(0);
					}
				}
			}
			} else {
				PathNode a = this.route.get(0).getEndNode();
				if(a.getAction() instanceof SignActionBlocker && !b) {
					return this.getNextNode(i + 1);
				} else {
					return a;
				}
			}
		}
		if(this.route.size() > i) {
			PathNode a = this.route.get(i).getEndNode();
			if(a.getAction() instanceof SignActionBlocker && !b) {
				return this.getNextNode(i + 1);
			} else {
				return a;
			}
		} else {
			if(this.route.size() > 0) {
				PathNode a = this.route.get(0).getEndNode();
				if(a.getAction() instanceof SignActionBlocker && !b) {
					return this.getNextNode(i + 1);
				} else {
					return a;
				}
			} else {
				return null;
			}
		}
	}
	String z;
	public void proceedTo(Location l) {
		if(this.route.get(0).locs.size() != 0) {
			if(this.route.get(0).locs.indexOf(l) > -1) {
				this.lastCon = this.route.get(0).clone();
				this.route.get(0).locs.remove(0);
			} else {
				this.lastCon = this.route.get(0).clone();
				this.route.remove(0);
			}
		} else {
			if(this.route.size() > 1) {
				this.lastCon = this.route.get(0).clone();
				this.route.remove(0);
			} else {
				this.lastCon = this.route.get(0).clone();
				this.route.remove(0);
			}
		}
	}
	public void setLocalRoute(List<PathOperation> route) {
		this.route = route;
		return;
	}
	public void loadNextRoute() {
		if(this.group.routes.size() < 1) {
			this.group.destroy();
			return;
		}
		this.route = this.group.routes.get(0).clone().route;
		if(this.lastCon == null) {
			this.lastCon = this.route.get(0).clone();
		}
		return;
	}
	
	public Double getTargetSpeed() {
		if(this._targetSpeed != null) return this._targetSpeed;
		return this.group.getTargetSpeed();
	}
	
	@Override
	public String toString() {
		return "" + this.getGroup().getHeadcode() + "/" + this.index + " | " + this.hashCode();
		
	}
	
	@Override
    public int compareTo(MinecartMember m) {
        return (int)(this.index - m.index);
    }
	
	public Boolean virtualize() {
		if(!this.virtualized) {
		this.virtualized = true;
		this.getEntity().remove();
		this.entity.setVirtualized(true);
		}
		return true;
	}
	
	public Boolean load() {
		if(this.virtualized) {
		this.entity.load();
		this.virtualized = false;
		TrainCarts.plugin.MemberController.addMember(this.getEntity().getUniqueId(), this);
		}
		return true;
	}
}
