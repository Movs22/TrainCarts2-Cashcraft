
package com.movies22.cashcraft.tc.controller;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MinecartMember;
import com.movies22.cashcraft.tc.api.VirtualMinecart;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.signactions.SignAction;
import com.movies22.cashcraft.tc.signactions.SignActionBlocker;
import com.movies22.cashcraft.tc.signactions.SignActionRBlocker;
import com.movies22.cashcraft.tc.utils.Despawn;
import com.movies22.cashcraft.tc.offline.*;
import com.movies22.cashcraft.tc.pathFinding.PathNode;

public class MinecartMemberController extends BaseController {
	private ConcurrentHashMap<UUID, MinecartMember> MinecartMembers;
	private ConcurrentHashMap<UUID, MinecartMember> MinecartHeadMembers;
	public BlockData d;
	public BlockData d2;
	public BlockData d3;
	private Vector breakVec = new Vector(0, 0, 0);
	public MinecartMemberController() {
		this.d = Material.DIAMOND_BLOCK.createBlockData();
		this.d2 = Material.GOLD_BLOCK.createBlockData();
		this.d3 = Material.BEACON.createBlockData();
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

	public Vector normalizeVector(Vector o) {
		return o.normalize();
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

	private OfflineLocation getLocation(Location l) {
		World w = l.getWorld();
		return (TrainCarts.plugin.offlineWorlds.get(w).getBlock(l.getBlockX(), l.getBlockY(), l.getBlockZ())).getLocation().clone();
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
				VirtualMinecart e = (VirtualMinecart) m.getEntity();
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
					g.destroy(Despawn.INVALID_SECTION);
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
				speed = m.currentSpeed;			
				if(ld < 10.0 && nd > 10.0 && m.currentSpeed > 0.0) {
					speed = Math.abs(m._targetSpeed - 0.4) * ((ld + 2.0) / 10.0) + 0.4;
					m.currentSpeed = speed;
				}
				if(g.currentRoute.stops.size() > 0) {
					PathNode st = g.currentRoute.stops.get(0).node;
					Double sd = l.distance(st.loc);
					if(sd < 10 && st.onBlock != null && !st.onBlock.equals(g) && !st.onBlock.despawned) {
						g.getMembers().forEach(mm -> {
							mm.currentSpeed = 0.0d;
							if(!mm.virtualized || mm.index == 0) {
								mm.getEntity().setMaxSpeed(0.0d);
							}
						});
					} else {
						if(nd < 20 && (n.onBlock != null && !n.onBlock.equals(g) && !n.onBlock.despawned) || (n2 != null && nd2 < 3 && (n2.onBlock != null && !n2.onBlock.equals(g) && !n2.onBlock.despawned))) {
							g.getMembers().forEach(mm -> {
								mm.currentSpeed = 0.0d;
								if(!mm.virtualized || mm.index == 0) {
									mm.getEntity().setMaxSpeed(0.0d);
								}
								g.canProceed = false;
                        		return;
							});
						}
						
						if(nd < 20 && !g.getHeadcode().startsWith("0") ) {
							n.onBlock = g;
							if(n2 != null && (nd2 < 3 && !g.getHeadcode().startsWith("0") )) {
								n2.onBlock = g;
							}
							g.canProceed = true;
						} else {
							g.canProceed = true;
						}
					}
				}


				if (nd < 10.0 && n.getAction().getSpeedLimit(g) != null && m._targetSpeed > 0.05) {
					speed = Math.abs(m._targetSpeed - n.getAction().getSpeedLimit(g)) * ((nd + 2.0) / 12.0) + n.getAction().getSpeedLimit(g);
					m.currentSpeed = speed;
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
				if(!g.canProceed) return;

				if (m._targetSpeed > 0.05) {
					while (i < 5 && (ld < (0.5 + speed) || nd < (0.5 + speed))) {
						if (nd < (0.5 + speed)) {
							SignAction b = n.getAction();
							if (!b.executed.contains(g) && !b.getClass().equals(SignActionBlocker.class) && !b.getClass().equals(SignActionRBlocker.class)) {
								e.syncY(nextNode.getY());
								e.syncPos(nextNode.clone());
								b.ExitExecuted.remove(g);
								m.prevNode = m.getNextNode();
								m.lastAction = b;
								//m.proceedTo(nextNode);
								b.execute(g);
								b.executed.add(g);
								if (b.getSpeedLimit(g) != null) {
									double s = b.getSpeedLimit(g);
									g.getMembers().forEach(mm -> {
										if(!mm.virtualized || mm.index == 0) {
											mm.getEntity().setMaxSpeed(s*mm._mod);
										}
										mm.currentSpeed = s;
										mm._targetSpeed = s;
									});
								}
							}
						}
						if(ld >= (0.5 + speed)) {
							break;
						}
						e.syncY(nextLoc.getY());
						e.syncPos(nextLoc.clone());
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
				if (m.currentSpeed < ts) {
					m.currentSpeed += 0.05;
				} else if (m.currentSpeed > ts) {
					m.currentSpeed -= 0.05;
				}
				m._mod = 1.0;
				speed = m.currentSpeed;
				e.setMaxSpeed(m._targetSpeed);
				OfflineLocation loc = getLocation(e.getLocation());
				Block rail = loc.getBlock();
				Double yChange = 0.0;
				if(!rail.getType().equals(Material.RAIL) && !rail.getType().equals(Material.POWERED_RAIL)) {
					yChange = -1.0;
					rail = loc.subtract(0, 1, 0).getBlock();
				} 
				if(!rail.getType().equals(Material.RAIL) && !rail.getType().equals(Material.POWERED_RAIL)) {
					loc.subtract(0, yChange, 0);
					rail = loc.add(0, 1, 0).getBlock();
					yChange = 1.0;
				}
				if(!g.virtualized) {
					if(((l.distance(g.tail().getEntity().getLocation()) > 20.0d))/* || (l.distance(g.tail(1).getEntity().getLocation()) < 1.0d))*/) {
						g.destroy();
						return;
					}
				}
				if(rail.getType().equals(Material.RAIL) || rail.getType().equals(Material.POWERED_RAIL)) {
					Rail rail2 = (Rail) rail.getBlockData();
					if (rail.getType().equals(Material.RAIL)) {
						if (e.getPassengers().size() > 0 || g.virtualized) {
							e.setMaxSpeed(0.4);
						} else {
							e.setMaxSpeed(0.3);
						}
						g.onCurve = true;
						g.lastCurve = rail.getLocation();
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
							e.syncY(Double.valueOf(loc.getBlockY()));
							break;
						case NORTH_SOUTH:
							x = 0.0;
							z = nextLoc.getZ() - l.getZ();
							if(z > 0) {
								m.facing = BlockFace.SOUTH;
							} else {
								m.facing = BlockFace.NORTH;
							}
							e.syncY(Double.valueOf(loc.getBlockY()));
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
							z = nextLoc.getZ() - l.getZ();
							x = nextLoc.getX() - l.getX();
							if(z < 0) {
								m.facing = BlockFace.NORTH;
							} else {
								m.facing = BlockFace.WEST;
							}
							break;
						case NORTH_EAST:
							z = nextLoc.getZ() - l.getZ();
							x = nextLoc.getX() - l.getX();
							if(z < 0) {
								m.facing = BlockFace.NORTH;
							} else {
								m.facing = BlockFace.EAST;
							}

							break;
						case ASCENDING_NORTH: 
							x = 0.0;
							z = nextLoc.getZ() - l.getZ();
							if(z > 0) {
								e.syncY(Double.valueOf(loc.getBlockY()));
							} else {
								e.syncY(Double.valueOf(loc.getBlockY() + 1));
							}
							break;
						case ASCENDING_SOUTH: 
							x = 0.0;
							z = nextLoc.getZ() - l.getZ();
							if(z < 0) {
								e.syncY(Double.valueOf(loc.getBlockY()));
							} else {
								e.syncY(Double.valueOf(loc.getBlockY() + 1));
							}
							break;
						case ASCENDING_EAST: 
							x = nextLoc.getX() - l.getX();
							z = 0.0;
							if(x < 0) {
								e.syncY(Double.valueOf(loc.getBlockY()));
							} else {
								e.syncY(Double.valueOf(loc.getBlockY() + 1));
							}
							break;
						case ASCENDING_WEST: 
							x = nextLoc.getX() - l.getX();
							z = 0.0;
							if(x > 0) {
								e.syncY(Double.valueOf(loc.getBlockY()));
							} else {
								e.syncY(Double.valueOf(loc.getBlockY() + 1));
							}
							break;
						default:
							break;
					}
				} 
				Vector vel;
				vel = new Vector(x, 0, z);
				if(vel.isZero()) return;
				speed = m.currentSpeed;				
				if (e.getPassengers().size() > 0 || g.virtualized) {
					e.setMaxSpeed(ts);
					if(g.onCurve) {
						e.setMaxSpeed(0.4);
					}
				} else {
					if(m.index == 0) {
						speed = speed * 3 / 4;
					} else {
						m.currentSpeed = speed * 3 / 4;
					}
					e.setMaxSpeed(ts*3/4);
					if(g.onCurve) {
						e.setMaxSpeed(0.3);
					}
				}
				vel = vel.normalize();
				e.setVelocity(vel.multiply(speed));
				/*Player p = TrainCarts.plugin.getServer().getPlayer("Movies22");
				if(p != null) {
					loc.add(0, 2, 0);
					switch(m.facing) {
						case EAST:
							p.sendBlockChange(loc, Material.RED_WOOL.createBlockData());
							break;
						case NORTH:
							p.sendBlockChange(loc, Material.YELLOW_WOOL.createBlockData());
							break;
						case SOUTH:
							p.sendBlockChange(loc, Material.GREEN_WOOL.createBlockData());
							break;
						case WEST:
							p.sendBlockChange(loc, Material.BLUE_WOOL.createBlockData());
							break;
						default:
							p.sendBlockChange(loc, Material.BEDROCK.createBlockData());
							break;
					}
					for(int y = 0; y < 10; y++) {
						PathNode w = m.getNextNode(y);
						if(w == null) return;
						p.sendBlockChange(w.loc.add(0, y+1, 0), Material.AIR.createBlockData());
						w.loc.subtract(0, y+1, 0);
						p.sendBlockChange(w.loc.add(0, y, 0), Material.BEACON.createBlockData());
						w.loc.subtract(0, y, 0);
					}
					loc.subtract(0, 2, 0);
					p.sendBlockChange(nextLoc, d2);
					p.sendBlockChange(nextNode, d3);*/
					//n.renderConnections(p);
				//}
			} else {
				m.getGroup().destroy();
			}
		});
		
		this.MinecartMembers.values().forEach(m -> {
			if (!m.spawned)
				return;
			if (this.validate(m)) {
				VirtualMinecart e = (VirtualMinecart) m.getEntity();
				MinecartGroup g = m.getGroup();

				if(!g.canProceed) {
					e.setMaxSpeed(0.0);
					e.setVelocity(breakVec);
				};

				Location l = e.getLocation();

				Location nextLoc = m.nextCart().getLocation();


				m._mod = l.distance(nextLoc)/1.2;
				if(m.index == g._getLength()/2 && g.doubleUnit) m._mod = l.distance(nextLoc)/1.6;

				Vector vel = nextLoc.subtract(l).toVector();
				if(vel.isZero()) return;
				Block rail = e.getLocation().getBlock();
				if (!rail.getType().equals(Material.RAIL)) {
					if(m.index == g._getLength() - 1) {
						g.onCurve = false;
					}
				} else {
					g.onCurve = true;
				}
				Double speed = 0.0d;
				if (e.getPassengers().size() > 0) {
					speed = g.head().currentSpeed;
					vel = vel.multiply(4);
					vel = vel.divide(new Vector(3, 3, 3));
					e.setMaxSpeed(g.head().currentSpeed*m._mod);
					if(g.onCurve) {
						speed = 0.4;
						e.setMaxSpeed(0.4*m._mod);
					}
				} else {
					speed = g.head().currentSpeed / 4 * 3;
					e.setMaxSpeed(g.head().currentSpeed*3*m._mod/4);
					if(g.onCurve) {
						speed = 0.3;
						e.setMaxSpeed(0.3*m._mod);
					}
				}
				vel = vel.normalize();
				e.setVelocity(vel.multiply(m._mod*speed));
			} else {
				m.getGroup().destroy();
			}
		});
	}
	
}
