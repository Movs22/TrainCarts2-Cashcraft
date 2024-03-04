package com.movies22.cashcraft.tc.api;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.TextDisplay;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.pathFinding.PathNode;
import com.movies22.cashcraft.tc.pathFinding.PathOperation;
import com.movies22.cashcraft.tc.signactions.SignAction;
import com.movies22.cashcraft.tc.signactions.SignActionBlocker;
import com.movies22.cashcraft.tc.signactions.SignActionRBlocker;

public class MinecartMember implements Comparable<MinecartMember> {
	private MinecartGroup group;
	private VirtualMinecart entity;
	public Double _mod = 1.0;
	public Double currentSpeed = 0.0;
	private List<PathOperation> route = null;
	public Double _targetSpeed = 0.6;
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
		if (i == 0) {
			this.setEntity(new VirtualMinecart(e, this, 0.0d));
		} else if(i == g._getLength()/2 && g.doubleUnit == true) {
			this.setEntity(new VirtualMinecart(e, group.head(), g._getLength()/2 * 1.2d + 1.6d));
		} else if(i < g._getLength()/2) {
			this.setEntity(new VirtualMinecart(e, group.head(), i * 1.2d));
		} else {
			this.setEntity(new VirtualMinecart(e, group.head(), (i - 1) * 1.2d + 1.6d));
		}
		TrainCarts.plugin.MemberController.addMember(e.getUniqueId(), this);
	}

	public void setPivot(MinecartMember mm) {
		this.entity.setPivot(mm);
	}

	public void setOffset(Double o) {
		this.entity.setOffset(o);
	}

	public Location getLocation() {
		return this.entity.getLocation();
	}

	public Boolean destroy() {
		try {
			if (this.getEntity() != null) {
				TrainCarts.plugin.MemberController.removeMember(this);
				this.getEntity().remove();
			}
			this.route.clear();
			this.route = null;
			return true;
		} catch (Error e) {
			e.printStackTrace();
			return false;
		}
	}

	public Boolean eject() {
		try {
			if (this.getEntity() != null) {
				this.getEntity().eject();
			}
			return true;
		} catch (Error e) {
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

	public VirtualMinecart getEntity() {
		return (VirtualMinecart) this.entity;
	}

	public void setEntity(VirtualMinecart e) {
		this.entity = e;
	}

	public MinecartMember nextCart() {
		int nc = this.index;
		if (nc < 1 || nc > (this.group.getMembers().size() - 1))
			return null;
		return this.group.getMember(nc - 1);
	}

	public List<PathOperation> getLocalRoute() {
		return route;
	}

	public Location getNextLocation() {
		if (this.route == null)
			return null;
		if (this.route.size() == 0) {
			this.group.destroy();
			return null;
		}
		if (this.route.get(0).locs.size() < 1) {
			if (this.route.size() > 1) {
				this.lastCon = this.route.get(0).clone();
				this.route.remove(0);
			} else {
				this.lastCon = this.route.get(0).clone();
				this.route.remove(0);
				this.loadNextRoute();
			}
		}
		if (this.route.size() == 0) {
			this.group.destroy();
			return null;
		}
		if (this.route.get(0).locs.size() > 0) {
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
		if (this.route == null)
			return null;
		if (i == 0) {
			PathNode a = this.route.get(0).getEndNode();
			if (((a.getAction() instanceof SignActionBlocker) || (a.getAction() instanceof SignActionRBlocker)) && !b) {
				return this.getNextNode(1);
			} else {
				return a;
			}
		} else {
			if(this.route.size() > i) { 
			PathNode a = this.route.get(i).getEndNode();
			if (((a.getAction() instanceof SignActionBlocker) || (a.getAction() instanceof SignActionRBlocker)) && !b) {
				return this.getNextNode(i + 1);
			} else {
				return a;
			}
			}
			return null;
		}
	}

	String z;

	public void proceedTo(Location l) {
		if(this.route == null) {
			return;
		}
		if (this.route.get(0).locs.size() != 0) {
			if (this.route.get(0).locs.indexOf(l) > -1) {
				this.lastCon = this.route.get(0).clone();
				this.route.get(0).locs.remove(0);
			} else {
				this.lastCon = this.route.get(0).clone();
				this.route.remove(0);
			}
		} else {
			if (this.route.size() > 1) {
				this.lastCon = this.route.get(0).clone();
				this.route.remove(0);
			} else {
				this.lastCon = this.route.get(0).clone();
				this.route.remove(0);
			}
		}
		this.route.removeIf(a -> a.getEndNode().loc.equals(l));
	}

	public void setLocalRoute(List<PathOperation> route) {
		this.route = route;
		return;
	}

	public void loadNextRoute() {
		if (this.group.routes.size() < 1) {
			this.group.destroy();
			return;
		}
		this.route = this.group.routes.get(0).clone().route;
		if (this.lastCon == null) {
			this.lastCon = this.route.get(0).clone();
		}
		return;
	}

	public Double getTargetSpeed() {
		if (this._targetSpeed != null)
			return this._targetSpeed;
		return this.group.getTargetSpeed();
	}

	@Override
	public String toString() {
		return "" + this.getGroup().getHeadcode() + "/" + this.index + " | " + this.hashCode();

	}

	@Override
	public int compareTo(MinecartMember m) {
		return (int) (this.index - m.index);
	}

	public Boolean virtualize() {
		this.virtualized = true;
		this.getEntity().remove();
		this.getEntity().setVirtualized(true);
		return true;
	}

	public Boolean load() {
		if(this._targetSpeed == 0.0) {
			return this.load(true);
		}
		return this.load(false);
	}

	public Boolean load(Boolean offset) {
		TrainCarts.plugin.MemberController.removeMember(this);
		this.getEntity().load(offset);
		this.virtualized = false;
		TrainCarts.plugin.MemberController.addMember(this.getEntity().getUniqueId(), this);
		return true;
	}
}
