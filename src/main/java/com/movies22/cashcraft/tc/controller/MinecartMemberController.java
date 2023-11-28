
package com.movies22.cashcraft.tc.controller;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.api.MinecartMember;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.signactions.SignAction;
import com.movies22.cashcraft.tc.signactions.SignActionBlocker;
import com.movies22.cashcraft.tc.signactions.SignActionRBlocker;

public class MinecartMemberController extends BaseController {
	private ConcurrentHashMap<UUID, MinecartMember> MinecartMembers;
	private ConcurrentHashMap<UUID, MinecartMember> MinecartHeadMembers;
	public MinecartMemberController() {
		this.MinecartMembers = new ConcurrentHashMap<UUID, MinecartMember>();
		this.MinecartHeadMembers = new ConcurrentHashMap<UUID, MinecartMember>();
	}

	public void addMember(MinecartMember m) {
		this.addMember(m.getEntity().getUniqueId(), m);
	}
	
	public Collection<MinecartMember> getHeads() {
		return this.MinecartHeadMembers.values();
	}
	
	public Collection<MinecartMember> getMembers() {
		return this.MinecartMembers.values();
	}

	public void addMember(UUID e, MinecartMember m) {
		if (m.index == 0) {
			MinecartHeadMembers.putIfAbsent(e, m);
		} else {
			MinecartMembers.putIfAbsent(e, m);
		}
	}

	public void removeMember(MinecartMember m) {
		if(m.getEntity() == null) {
			return;
		}
		if (m.index == 0) {
			MinecartHeadMembers.remove(m.getEntity().getUniqueId());
		} else {
			MinecartMembers.remove(m.getEntity().getUniqueId());
		}
	}

	public MinecartMember getFromUUID(UUID e) {
		if (MinecartMembers.get(e) != null) {
			return MinecartMembers.get(e);
		} else {
			return MinecartHeadMembers.get(e);
		}
	}

	public MinecartMember getFromEntity(Entity e) {
		return this.getFromUUID(e.getUniqueId());
	}

	private Double speed;

	public Vector rotate(Vector vector, double angle) { // angle in radians

		// normalize(vector); // No need to normalize, vector is already ok...
		angle = angle / 180 * Math.PI;
		float x1 = (float) (vector.getX() * Math.cos(angle) - vector.getZ() * Math.sin(angle));

		float z1 = (float) (vector.getX() * Math.sin(angle) + vector.getZ() * Math.cos(angle));
		return new Vector(x1, vector.getY(), z1);
	}

	public Boolean validate(MinecartMember m) {
		if(m.virtualized) {
			return true;
		}
		Entity e = (Entity) m.getEntity();
		if (e.isDead() || m == null || m.getGroup() == null || e == null) {
			return false;
		}
		//Location l2 = e.getLocation().subtract(0, 1, 0);
		/*if( ( !e.getLocation().getBlock().getType().equals(Material.RAIL) && !e.getLocation().getBlock().getType().equals(Material.POWERED_RAIL)) && (!l2.getBlock().getType().equals(Material.RAIL) && !l2.getBlock().getType().equals(Material.POWERED_RAIL))) {
			return false;
		}*/
		return true;
	}
	
	Boolean b = false;

	@Override
	public void doFixedTick() {
		this.MinecartHeadMembers.values().forEach(m -> {
			if (!m.spawned)
				return;
			if (this.validate(m)) {
				Minecart e = (Minecart) m.getEntity();
				MinecartGroup g = m.getGroup();

				Location l = e.getLocation();
				Location nextLoc = m.getNextLocation();
				PathNode n = m.getNextNode();
				PathNode n2 = m.getNextNode(1);
				Location nextNode;
				if (n != null) {
					nextNode = n.loc;
					if(nextLoc == null) {
						nextLoc = nextNode;
					}
				} else {
					nextNode = l;
					nextLoc = l;
				}
				Double nd = l.distance(nextNode);
				Double ld = l.distance(nextLoc);
				Double nd2 = 0.0;
				if(n2 != null) {
					nd2 = l.distance(n2.loc);
				}
				if (m._targetSpeed >= 0.05 && m.lastAction != null && !m.lastAction.ExitExecuted.contains(g)
						&& m.lastAction != n.getAction()) {
					m.lastAction.ExitExecuted.add(g);
					m.lastAction.exit(m.getGroup());
					m.lastAction.executed.remove(m.getGroup());
					m.lastAction.node.onBlock = null;
					m.lastAction = null;
				}
				if(g.currentRoute.stops == null) {
					return;
				}
				if(g.currentRoute.stops.size() > 0) {
					PathNode st = g.currentRoute.stops.get(0).node;
					Double sd = l.distance(st.loc);
					if(sd < 10 && st.onBlock != null && !st.onBlock.equals(g) && !st.onBlock.despawned) {
						g.getMembers().forEach(mm -> {
							mm.currentSpeed = Double.MIN_VALUE;
							if(!mm.virtualized || mm.index == 0) {
								mm.getEntity().setMaxSpeed(0.0);
							}
						});
					}
					g.canProceed = false;
				}
				if(nd < 20 && (n.onBlock != null && !n.onBlock.equals(g) && !n.onBlock.despawned) || (n2 != null && nd2 < 3 && (n2.onBlock != null && !n2.onBlock.equals(g) && !n2.onBlock.despawned))) {
					g.getMembers().forEach(mm -> {
						mm.currentSpeed = Double.MIN_VALUE;
						if(!mm.virtualized || mm.index == 0) {
							mm.getEntity().setMaxSpeed(0.0);
						}
					});
					g.canProceed = false;
					return;
				} else if(nd < 20 && !g.getHeadcode().startsWith("0") ) {
					n.onBlock = g;
					if(n2 != null && (nd2 < 3 && !g.getHeadcode().startsWith("0") )) {
						n2.onBlock = g;
					}
					g.canProceed = true;
				} else {
					g.canProceed = true;
				}
				if (nd < 10.0) {
					if(n.getAction().getSpeedLimit(g) != null && m._targetSpeed > 0.05) {
						speed = Math.abs(m._targetSpeed - n.getAction().getSpeedLimit(g)) * ((nd + 2.0) / 12.0)
							+ n.getAction().getSpeedLimit(g);
						m.currentSpeed = speed;
					}
				} 
				
				m._mod = 1.0;

				if (m.currentSpeed < 0.05) {
					m.currentSpeed = 0.0;
				}
				// Initializes variables for the vectors.
				Double x = 0.0;
				Double z = 0.0;
				int i = 0;
				if (nextLoc != null) {
					x = nextLoc.getX() - l.getX();
					z = nextLoc.getZ() - l.getZ();
				}
				if (m._targetSpeed > 0.05) {
					while (i < 5 && (ld < (2.0) || nd < (1.0 + m._targetSpeed))) {
						if (nd < (2.0)) {
							SignAction b = n.getAction();
							if (!b.executed.contains(g) && !b.getClass().equals(SignActionBlocker.class) && !b.getClass().equals(SignActionRBlocker.class)) {
								b.ExitExecuted.remove(g);
								if (b.getSpeedLimit(g) != null) {
									double s = b.getSpeedLimit(g);
									g.getMembers().forEach(mm -> {
										mm._targetSpeed = s;
										if(!mm.virtualized) {
											mm.getEntity().setMaxSpeed(s*mm._mod);
										}
										mm.currentSpeed = s;
									});
								}
								m.prevNode = m.getNextNode();
								m.lastAction = b;
								b.execute(g);
								b.executed.add(g);
							}
						}
						if(ld >= (2.0)) {
							break;
						}
						m.proceedTo(nextLoc);
						nextLoc = m.getNextLocation();
						nextNode = m.getNextNode().loc;
						if (nextLoc == null) {
							i = 5;
							break;
						}
						x = nextLoc.getX() - l.getX();
						z = nextLoc.getZ() - l.getZ();
						while((x == 0 && z == 0)) {
							m.proceedTo(nextLoc);
							nextLoc = m.getNextLocation();
							nextNode = m.getNextNode().loc;
							if (nextLoc == null) {
								i = 5;
								break;
							}
							x = nextLoc.getX() - l.getX();
							z = nextLoc.getZ() - l.getZ();
							i++;
						}
						if (n != null) {
							nextNode = n.loc;
							if(nextLoc == null) {
								nextLoc = nextNode;
							}
						} else {
							nextNode = l;
							nextLoc = l;
						}
						nd = l.distance(nextNode);
						ld = l.distance(nextLoc);
					}
				}
				Double ts = m._targetSpeed;
				if (Math.round(m.currentSpeed * 100) < (ts * 100) && Math.round(m.currentSpeed * 100) < (ts * 100 - 5)) {
					m.currentSpeed += 0.05;
				} else if (Math.round(m.currentSpeed * 100) > (ts * 100) && Math.round(m.currentSpeed * 100) > (ts + 5 * 100)) {
					m.currentSpeed -= 0.05;
				}
				speed = m.currentSpeed;
				m.setMaxSpeed(m._targetSpeed*m._mod);
				Block rail = e.getLocation().getBlock();
				if(!g.virtualized) {
					if(((l.distance(g.tail().getEntity().getLocation()) > 20.0d))) {
						g.destroy();
						return;
					}
				}
				if(rail.getType().equals(Material.POWERED_RAIL) || rail.getType().equals(Material.RAIL)) {
					Rail rail2 = (Rail) rail.getBlockData();
					if(rail.getType().equals(Material.RAIL) || rail2.getShape().name().contains("ASCENDING")) {
						g.head()._targetSpeed = 0.4d;
						m.getEntity().setMaxSpeed(0.4d);
=======
						m.setMaxSpeed(0.4d);
>>>>>>> Stashed changes
						g.lastCurve = rail.getLocation();
					} else {
						m.setMaxSpeed(m._targetSpeed*m._mod);
					}
					switch(rail2.getShape()) {
						case EAST_WEST:
							x = nextLoc.getX() - l.getX();
							if(x > 0) {
								m.facing = BlockFace.EAST;
							} else {
								m.facing = BlockFace.WEST;
							}
							z = 0.0;
							break;
						case NORTH_SOUTH:
							x = 0.0;
							z = nextLoc.getZ() - l.getZ();
							if(z > 0) {
								m.facing = BlockFace.SOUTH;
							} else {
								m.facing = BlockFace.NORTH;
							}
							break;
						case SOUTH_EAST:
							x = nextLoc.getX() - l.getX();
							z = nextLoc.getZ() - l.getZ();
							if(z > 0) {
								m.facing = BlockFace.SOUTH;
							} else {
								m.facing = BlockFace.EAST;
							}
							break;
						case SOUTH_WEST:
							x = nextLoc.getX() - l.getX();
							z = nextLoc.getZ() - l.getZ();
							if(z > 0) {
								m.facing = BlockFace.SOUTH;
							} else {
								m.facing = BlockFace.WEST;
							}
							break;
						case NORTH_WEST:
							x = nextLoc.getX() - l.getX();
							z = nextLoc.getZ() - l.getZ();
							if(z < 0) {
								m.facing = BlockFace.NORTH;
							} else {
								m.facing = BlockFace.WEST;
							}
							break;
						case NORTH_EAST:
							x = nextLoc.getX() - l.getX();
							z = nextLoc.getZ() - l.getZ();
							if(z < 0) {
								m.facing = BlockFace.NORTH;
							} else {
								m.facing = BlockFace.EAST;
							}

							break;
						case ASCENDING_NORTH: 
							x = 0.0;
							z = nextLoc.getZ() - l.getZ();
							m.facing = BlockFace.NORTH;
							break;
						case ASCENDING_SOUTH: 
							x = 0.0;
							z = nextLoc.getZ() - l.getZ();
							m.facing = BlockFace.SOUTH;
							break;
						case ASCENDING_EAST: 
							x = nextLoc.getX() - l.getX();
							z = 0.0;
							m.facing = BlockFace.EAST;
							break;
						case ASCENDING_WEST: 
							x = nextLoc.getX() - l.getX();
							z = 0.0;
							m.facing = BlockFace.WEST;
							break;
						default:
							break;
					}
				} 
				Vector vel;
				if (x == 0 && z != 0) {
					vel = new Vector(0, 0, z / Math.abs(z));
				} else if (z == 0 && x != 0) {
					vel = new Vector(x / Math.abs(x), 0, 0);
				} else if (x == 0 && z == 0) {
					vel = new Vector(0, 0, 0);
					/*HashMap<String, String> a = new HashMap<String, String>();
					a.putIfAbsent("X", "" + x);
					a.putIfAbsent("Z", "" + z);
					g.destroy(Despawn.INVALID_HEADING, a);
					return;*/
				} else {
					vel = new Vector(x / Math.abs(x), 0, z / Math.abs(z));
				}
				speed = m.currentSpeed;				
				if (e.getPassengers().size() > 0) {
					vel = vel.multiply(4);
					vel = vel.divide(new Vector(3, 3, 3));
				}
				e.setVelocity(vel.multiply(speed));
			} else {
				m.getGroup().destroy();
			}
		});
		
		this.MinecartMembers.values().forEach(m -> {
			if (!m.spawned)
				return;
			if (this.validate(m)) {
				Minecart e = (Minecart) m.getEntity();
				MinecartGroup g = m.getGroup();
				if(!g.canProceed) {
					return;
				}
				Location l = e.getLocation();

				m._targetSpeed = g.head()._targetSpeed;
				m.currentSpeed = g.head().currentSpeed;
				MinecartMember nextCart = m.nextCart();
				if (nextCart == null || nextCart.getEntity() == null) {
					return;
				}
				m._mod = l.distance(nextCart.getLocation(true)) / 1.2;


				if (m.currentSpeed < 0.05) {
					m.currentSpeed = 0.0;
				}
				
				if (m._mod < 0.0) {
					m._mod = 0.0;
				}
				/*if(m.index == (g.getMembers().size()) - 1 && g.lastCurve != null) {
					if(g.lastCurve.distance(l) > 15.0) {
						g.getMembers().forEach(mm -> {
							mm.setMaxSpeed(mm._targetSpeed*mm._mod);
						});
						g.lastCurve = null;
					} else {
						g.getMembers().forEach(mm -> {
							mm.getEntity().setMaxSpeed(0.4d);
						});
					}
				}*/
				
				// Initializes variables for the vectors.
				Double x = 0.0;
				Double z = 0.0;
				Location nextLoc = m.nextCart().getEntity().getLocation();
				
				Double ts = g.head()._targetSpeed;
				if (Math.round(m.currentSpeed * 100) < (ts * 100)) {
					m.currentSpeed += 0.05;
				} else if (Math.round(m.currentSpeed * 100) > (ts * 100)) {
					m.currentSpeed -= 0.05;
				}
				speed = m.currentSpeed;
				m.setMaxSpeed(ts*m._mod);
				Block rail = e.getLocation().subtract(0,  0, 0).getBlock();
				if(rail.getType().equals(Material.POWERED_RAIL) || rail.getType().equals(Material.RAIL)) {
					Rail rail2 = (Rail) rail.getBlockData();
					switch(rail2.getShape()) {
					case EAST_WEST:
						x = nextLoc.getX() - l.getX();
						if(x > 0) {
							m.facing = BlockFace.EAST;
						} else {
							m.facing = BlockFace.WEST;
						}
						z = 0.0;
						break;
					case NORTH_SOUTH:
						x = 0.0;
						z = nextLoc.getZ() - l.getZ();
						if(z > 0) {
							m.facing = BlockFace.SOUTH;
						} else {
							m.facing = BlockFace.NORTH;
						}
						break;
					case SOUTH_EAST:
						x = nextLoc.getX() - l.getX();
						z = nextLoc.getZ() - l.getZ();
						if(z > 0) {
							m.facing = BlockFace.SOUTH;
						} else {
							m.facing = BlockFace.EAST;
						}
						break;
					case SOUTH_WEST:
						x = nextLoc.getX() - l.getX();
						z = nextLoc.getZ() - l.getZ();
						if(z > 0) {
							m.facing = BlockFace.SOUTH;
						} else {
							m.facing = BlockFace.WEST;
						}
						break;
					case NORTH_WEST:
						x = nextLoc.getX() - l.getX();
						z = nextLoc.getZ() - l.getZ();
						if(z < 0) {
							m.facing = BlockFace.NORTH;
						} else {
							m.facing = BlockFace.WEST;
						}
						break;
					case NORTH_EAST:
						x = nextLoc.getX() - l.getX();
						z = nextLoc.getZ() - l.getZ();
						if(z < 0) {
							m.facing = BlockFace.NORTH;
						} else {
							m.facing = BlockFace.EAST;
						}

						break;
					case ASCENDING_NORTH: 
						x = 0.0;
						z = nextLoc.getZ() - l.getZ();
						m.facing = BlockFace.NORTH;
						break;
					case ASCENDING_SOUTH: 
						x = 0.0;
						z = nextLoc.getZ() - l.getZ();
						m.facing = BlockFace.SOUTH;
						break;
					case ASCENDING_EAST: 
						x = nextLoc.getX() - l.getX();
						z = 0.0;
						m.facing = BlockFace.EAST;
						break;
					case ASCENDING_WEST: 
						x = nextLoc.getX() - l.getX();
						z = 0.0;
						m.facing = BlockFace.WEST;
						break;
					default:
						break;
					}
				} 
				Vector vel;
				if (x == 0 && z != 0) {
					vel = new Vector(0, 0, z / Math.abs(z));
				} else if (z == 0 && x != 0) {
					vel = new Vector(x / Math.abs(x), 0, 0);
				} else if (x == 0 && z == 0) {
					vel = new Vector(0, 0, 0);
					/*HashMap<String, String> a = new HashMap<String, String>();
					a.putIfAbsent("X", "" + x);
					a.putIfAbsent("Z", "" + z);
					g.destroy(Despawn.INVALID_HEADING, a);*/
					return;
				} else {
					vel = new Vector(x / Math.abs(x), 0, z / Math.abs(z));
				}
				speed = g.head().currentSpeed;
				if (speed % 0.05 > 0.0) {
					speed = speed - (speed % 0.05);
				}

				if (speed < 0.05) {
					speed = 0.0;
				}
				if (e.getPassengers().size() > 0) {
					vel = vel.multiply(4);
					vel = vel.divide(new Vector(3, 3, 3));
				}
				e.setVelocity(vel.multiply( (m._mod * speed) ));
			} else {
				m.getGroup().destroy();
			}
		});
	}
	
}
