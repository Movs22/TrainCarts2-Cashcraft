package com.movies22.cashcraft.tc.pathFinding;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rail;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.signactions.SignAction;

public class PathOperation implements Cloneable {
	private PathNode start;
	private PathNode end;
	public BlockFace facing;
	public BlockFace opositeFacing;
	public List<Location> locs = new ArrayList<Location>();
	public PathOperation(PathNode n, PathNode e, BlockFace facing) {
		this.setStartNode(n);
		this.setFacing(facing);
		locs.add(n.loc);
		if (e == null) {
			try {
				this.calculate(2000);
			} catch(StackOverflowError z) {
				TrainCarts.plugin.getLogger().log(Level.INFO, "FAILED at " + n.getLocationStr() );
			}
		} else {
			this.setEndNode(e);
		}
	}

	@Override
	public PathOperation clone() {
		PathOperation a = new PathOperation(this.start, this.end, this.facing);
		a.locs = new ArrayList<Location>(this.locs);
		a.facing = this.facing;
		a.opositeFacing = this.opositeFacing;
		return a;
	}
	
	public PathNode getStartNode() {
		return start;
	}

	public void setStartNode(PathNode start) {
		this.start = start;
	}

	public PathNode getEndNode() {
		return end;
	}

	public void setEndNode(PathNode end) {
		this.end = end;
	}

	public PathOperation getOposite() {
		PathOperation a = new PathOperation(this.getEndNode(), this.getStartNode(), this.opositeFacing.getOppositeFace());
		for(int i = (this.locs.size() - 2); i >= 0; i--) {
			a.locs.add(this.locs.get(i));
		};
		a.opositeFacing = this.facing.getOppositeFace();
		return a;
	}

	public BlockFace getFacing() {
		return facing;
	}

	public void setFacing(BlockFace facing) {
		this.facing = facing;
	}
	@SuppressWarnings("deprecation")
	private void calculate(int i) {
		int a = 0;
		boolean finished = false;
		Location b = this.getStartNode().getLocation().clone();
		//b.clone().add(0,  2,  0).getBlock().setType(Material.BEACON);
		if (this.facing == BlockFace.NORTH) {
			b.subtract(0, 0, 1);
		}
		if (this.facing == BlockFace.SOUTH) {
			b.add(0, 0, 1);
		}
		if (this.facing == BlockFace.EAST) {
			b.add(1, 0, 0);
		}
		if (this.facing == BlockFace.WEST) {
			b.subtract(1, 0, 0);
		}
		a++;
		BlockFace f = this.facing;
		while (a < i && !finished) {
			if (!(b.getBlock().getBlockData() instanceof Rail)) {
				b.subtract(0, 1, 0);
			}
			if (!(b.getBlock().getBlockData() instanceof Rail)) {
				b.add(0, 2, 0);
			}
			if (b.getBlock().getBlockData() instanceof Rail) {
				//e.getBlock().setType(Material.WHITE_CONCRETE);
				Rail c = (Rail) b.getBlock().getBlockData();
				Location d = b.clone();
				d.subtract(0, 2, 0);
				if (d.getBlock().getState() instanceof Sign) {
					Sign s = (Sign) d.getBlock().getState();
					if (s.getLine(0).startsWith("t:")) {
						SignAction sa = SignAction.parse(s);
						if (sa != null) {
							PathNode n = this.start.line.createNode(b, d.getBlock());
							locs.add(b.clone());
							this.setEndNode(n);
							this.opositeFacing = f.getOppositeFace();
							if(!n.facings.contains(f.getOppositeFace())) {
								n.facings.add(f.getOppositeFace());
								n.connections.add(this.getOposite());
								n.addNeighbour(f.getOppositeFace(), this.start);
								n.findNeighbours();
							}
							finished = true;
							break;
						}
					}
				}
				/*switch(f) {
					case EAST:
						b.clone().add(0,  3,  0).getBlock().setType(Material.YELLOW_CONCRETE);
						break;
					case NORTH:
						b.clone().add(0,  2,  0).getBlock().setType(Material.RED_CONCRETE);
						break;
					case SOUTH:
						b.clone().add(0,  4,  0).getBlock().setType(Material.GREEN_CONCRETE);
						break;
					case WEST:
						b.clone().add(0,  5,  0).getBlock().setType(Material.BLUE_CONCRETE);
						break;
					default:
						break;
				}*/
					
				switch (c.getShape()) {
				case ASCENDING_EAST:
					//locs.add(b.clone());
					if (f == BlockFace.EAST) {
						// UP - east>west
						b.add(1, 0, 0);
						f = BlockFace.EAST;
					} else {
						// DOWN - west>east
						b.subtract(1, 0, 0);
						f = BlockFace.WEST;
					}
					break;
				case ASCENDING_NORTH:
					//locs.add(b.clone());
					if (f == BlockFace.NORTH) {
						// UP - north>south
						b.add(0, 0, -1);
						f = BlockFace.NORTH;
					} else {
						// DOWN - south>north
						b.subtract(0, 0, -1);
						f = BlockFace.SOUTH;
					}
					break;
				case ASCENDING_SOUTH:
					//locs.add(b.clone());
					if (f == BlockFace.SOUTH) {
						// UP - south>north
						b.add(0, 0, 1);
						f = BlockFace.SOUTH;
					} else {
						// DOWN - north>south
						b.subtract(0, 0, 1);
						f = BlockFace.NORTH;
					}
					break;
				case ASCENDING_WEST:
					//locs.add(b.clone());
					if (f == BlockFace.WEST) {
						// UP - west>east
						b.add(-1, 0, 0);
						f = BlockFace.WEST;
					} else {
						// DOWN - east>west
						b.subtract(-1, 0, 0);
						f = BlockFace.EAST;
					}
					break;
				case EAST_WEST:
					if (f == BlockFace.EAST) {
						// east>west
						b.add(1, 0, 0);
						f = BlockFace.EAST;
					} else {
						// west>east
						b.subtract(1, 0, 0);
						f = BlockFace.WEST;
					}
					break;
				case NORTH_SOUTH:
					if (f == BlockFace.NORTH) {
						// north>south
						b.subtract(0, 0, 1);
						f = BlockFace.NORTH;
					} else {
						// south>north
						b.add(0, 0, 1);
						f = BlockFace.SOUTH;
					}
					break;
				case NORTH_EAST:
					locs.add(b.clone());
					if (f == BlockFace.SOUTH) {
						// south>east
						b.add(1, 0, 0);
						f = BlockFace.EAST;
					} else {
						// east>south
						b.subtract(0, 0, 1);
						f = BlockFace.NORTH;
					}
					break;
				case NORTH_WEST:
					locs.add(b.clone());
					if (f == BlockFace.SOUTH) {
						// north>west
						b.subtract(1, 0, 0);
						f = BlockFace.WEST;
					} else {
						// west>north
						b.subtract(0, 0, 1);
						f = BlockFace.NORTH;
					}
					break;
				case SOUTH_EAST:
					locs.add(b.clone());
					if (f == BlockFace.NORTH) {
						// north>west
						b.add(1, 0, 0);
						f = BlockFace.EAST;
					} else {
						// west>north
						b.add(0, 0, 1);
						f = BlockFace.SOUTH;
					}
					break;
				case SOUTH_WEST:
					locs.add(b.clone());
					if (f == BlockFace.NORTH) {
						// north>west
						b.subtract(1, 0, 0);
						f = BlockFace.WEST;
					} else {
						// west>north
						b.add(0, 0, 1);
						f = BlockFace.SOUTH;
					}
					break;
				default:
					break;
				}
			} else {
				finished = true;
				continue;
			}
			a++;
		}
	}
}
