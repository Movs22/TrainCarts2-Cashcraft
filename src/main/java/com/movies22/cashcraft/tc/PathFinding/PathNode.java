package com.movies22.cashcraft.tc.pathFinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.signactions.SignAction;
import com.movies22.cashcraft.tc.signactions.SignActionBlocker;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;

public class PathNode {
	public Location loc;
	public Sign sign;
	public MetroLine line;
	public Rail rail;
	private SignAction signAction;
	private List<String> txt = new ArrayList<String>();
	private HashMap<BlockFace, PathNode> neighbours = new HashMap<BlockFace, PathNode>();
	public List<PathOperation> connections = new ArrayList<PathOperation>();
	public List<BlockFace> facings = new ArrayList<BlockFace>();
	public BlockFace direction;
	private int SpeedLimit = -1;
	public MinecartGroup onBlock = null;

	public PathNode(Location l, Block b, MetroLine line) {
		this.handleBuild(l, b, line);
	}

	public String getLocationStr() {
		return this.loc.getBlockX() + "/" + this.loc.getBlockY() + "/" + this.loc.getBlockZ();
	}
	
	@SuppressWarnings("deprecation")
	public void handleBuild(Location l, Block b, MetroLine line) {
		this.txt = new ArrayList<String>();
		this.txt.add(0, null);
		this.txt.add(1, null);
		this.txt.add(2, null);
		this.txt.add(3, null);
		this.loc = l;
		Sign s = null;
		if (b.getState() instanceof Sign) {
			s = (Sign) b.getState();
		}
		this.sign = s;
		this.line = line;
		try {
			this.rail = (Rail) l.getBlock().getBlockData();
		} catch(ClassCastException e) {
			TrainCarts.plugin.getLogger().log(Level.WARNING, l.getX() + "/" + l.getY() + "/" + l.getZ() + " is an invalid node.");
		}
		if (s != null && s.getLines().length > 0) {
			org.bukkit.block.data.type.Sign s2 = null;
			try {
				s2 = (org.bukkit.block.data.type.Sign) b.getBlockData();
			} catch(ClassCastException e) {
				TrainCarts.plugin.getLogger().log(Level.WARNING, l.getX() + "/" + l.getY() + "/" + l.getZ() + " is an invalid node.");
			}
			if(s2 == null) {
				return;
			}
			for (int i = 0; i < s.getLines().length; i++) {
				this.setLine(i, s.getLine(i));
			}
			SignAction a = SignAction.parse(s);
			if (a == null) {
				this.setAction(null);
			} else {
				this.direction = s2.getRotation();
				a.node = this;
				a.sign = s;
				this.setAction(a);
				a.postParse();
				if(a instanceof SignActionPlatform && (((SignActionPlatform) a).station != null && ((SignActionPlatform) a).station.headcode == null)) {
					this.facings.add(this.direction);
				}
			}
		} else {
			this.setAction(null);
		}
	}

	public void setLine(int i, String s) {
		this.txt.set(i, s);
	}

	public int manhatamDistance(Location l) {
		return Math.abs(l.getBlockX() - this.loc.getBlockX()) + Math.abs(l.getBlockZ() - this.loc.getBlockZ());
	}

	public void renderConnections(Player p) {
		this.connections.forEach(con -> {
			BlockData d;
			switch(con.facing) {
				case NORTH:
					d = Material.RED_CONCRETE.createBlockData();
					break;
				case EAST:
					d = Material.YELLOW_CONCRETE.createBlockData();
					break;
				case SOUTH:
					d = Material.GREEN_CONCRETE.createBlockData();
					break;
				case WEST:
					d = Material.BLUE_CONCRETE.createBlockData();
					break;
				default:
					d = Material.BEDROCK.createBlockData();
					break;
			}
			con.locs.forEach(loc -> {
				p.sendBlockChange(loc, d);
			});
		});
	}

	public <T extends SignAction> T setAction(T a) {
		this.signAction = a;
		return a;
	}

	public Location getLocation() {
		return loc;
	}

	public SignAction getAction() {
		return this.signAction;
	}

	public String getLine(int i) {
		return this.txt.get(i);
	}

	public HashMap<BlockFace, PathNode> getNeighbours() {
		return neighbours;
	}

	public void addNeighbour(BlockFace f, PathNode neighbour) {
		this.neighbours.putIfAbsent(f, neighbour);
	}

	public void removeNeighbour(BlockFace f) {
		this.neighbours.remove(f);
	}

	public void clearNeighbours() {
		this.neighbours.clear();
	}

	public int getSpeedLimit() {
		return this.SpeedLimit;
	}

	public void setSpeedLimit(int speedLimit) {
		this.SpeedLimit = speedLimit;
	}

	public void findNeighbours() {
		this.connections.clear();
		this.facings.clear();
		Location a = this.loc.clone();
		if (this.signAction == null) {
			return;
		}
		if(this.signAction.getAction().equals("SignActionBlocker")) {
			BlockFace f = ((SignActionBlocker) this.signAction).getBlocked(this.sign);
			this.facings.add(f);
			this.connections.add(new PathOperation(this, this, f));
		}

		if (!this.signAction.getAction().equals("SignActionSwitcher")) {
			switch (this.rail.getShape()) {
			case EAST_WEST:
				this.facings.add(BlockFace.NORTH);
				this.connections.add(new PathOperation(this, this, BlockFace.NORTH));
				this.facings.add(BlockFace.SOUTH);
				this.connections.add(new PathOperation(this, this, BlockFace.SOUTH));
				break;
			case NORTH_EAST:
				this.facings.add(BlockFace.WEST);
				this.connections.add(new PathOperation(this, this, BlockFace.WEST));
				this.facings.add(BlockFace.SOUTH);
				this.connections.add(new PathOperation(this, this, BlockFace.SOUTH));
				break;
			case NORTH_SOUTH:
				this.facings.add(BlockFace.EAST);
				this.connections.add(new PathOperation(this, this, BlockFace.EAST));
				this.facings.add(BlockFace.WEST);
				this.connections.add(new PathOperation(this, this, BlockFace.WEST));
				break;
			case NORTH_WEST:
				this.facings.add(BlockFace.EAST);
				this.connections.add(new PathOperation(this, this, BlockFace.EAST));
				this.facings.add(BlockFace.SOUTH);
				this.connections.add(new PathOperation(this, this, BlockFace.SOUTH));
				break;
			case SOUTH_EAST:
				this.facings.add(BlockFace.NORTH);
				this.connections.add(new PathOperation(this, this, BlockFace.NORTH));
				this.facings.add(BlockFace.WEST);
				this.connections.add(new PathOperation(this, this, BlockFace.WEST));
				break;
			case SOUTH_WEST:
				this.facings.add(BlockFace.NORTH);
				this.connections.add(new PathOperation(this, this, BlockFace.NORTH));
				this.facings.add(BlockFace.EAST);
				this.connections.add(new PathOperation(this, this, BlockFace.EAST));
				break;
			default:
				break;
			}
		}
		//north
		a.subtract(0, 0, 1);
		Material b = a.getBlock().getType();
		if ((b == Material.RAIL || b == Material.POWERED_RAIL) && !this.facings.contains(BlockFace.NORTH)) {
			this.facings.add(BlockFace.NORTH);
			this.connections.add(new PathOperation(this, null, BlockFace.NORTH));
		};
		//east
		a.add(1, 0, 1);
		b = a.getBlock().getType();
		if ((b == Material.RAIL || b == Material.POWERED_RAIL) && !this.facings.contains(BlockFace.EAST)) {
			this.facings.add(BlockFace.EAST);
			this.connections.add(new PathOperation(this, null, BlockFace.EAST));
		};
		//south
		a.add(-1, 0, 1);
		b = a.getBlock().getType();
		if ((b == Material.RAIL || b == Material.POWERED_RAIL) && !this.facings.contains(BlockFace.SOUTH)) {
			this.facings.add(BlockFace.SOUTH);
			this.connections.add(new PathOperation(this, null, BlockFace.SOUTH));
		};
		//west
		a.subtract(1, 0, 1);
		b = a.getBlock().getType();
		if ((b == Material.RAIL || b == Material.POWERED_RAIL) && !this.facings.contains(BlockFace.WEST)) {
			this.facings.add(BlockFace.WEST);
			this.connections.add(new PathOperation(this, null, BlockFace.WEST));
		};
	}

	public void reroute() {
		this.findNeighbours();
	}
}
