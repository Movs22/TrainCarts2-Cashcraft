package com.movies22.cashcraft.tc.signactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.block.data.type.Jigsaw.Orientation;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.api.Station;
import com.movies22.cashcraft.tc.pis.DynamicPIS;
import com.movies22.cashcraft.tc.pis.PIS;
import com.movies22.cashcraft.tc.pis.PisController;
import com.movies22.cashcraft.tc.pis.PisMode;
import com.movies22.cashcraft.tc.utils.Guides;
import com.movies22.cashcraft.tc.utils.StationAnnouncements;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SignActionPlatform extends SignAction {
	public String platform = null;
	public String name;
	public Station station;
	public long duration;
	public Vector offset;
	public int doors;
	public String n;
	public List<Location> doorLocs = new ArrayList<Location>();
	public List<Location> lightLocs = new ArrayList<Location>();
	public boolean inverted = false;
	private Character headcode;
	public boolean reverse = false;
	public HashMap<Character, PIS> pis = new HashMap<Character, PIS>();
	public DynamicPIS dynPis;
	public PisMode mode;

	@Override
	public SignActionPlatform clone() {
		SignActionPlatform a = new SignActionPlatform();
		a.platform = null;
		a.name = "";
		a.station = null;
		a.duration = 0;
		a.offset = null;
		a.doors = 0;
		a.n = "";
		a.doorLocs = new ArrayList<Location>();
		a.lightLocs = new ArrayList<Location>();
		a.inverted = false;
		a.headcode = null;
		a.reverse = false;
		a.mode = PisMode.NORMAL;
		a.pis = new HashMap<Character, PIS>();
		return a;
	}

	public void setLights(Material light) {
		this.lightLocs.forEach(loc -> {
			loc.getBlock().setType(light);
		});
		if (light.equals(Material.VERDANT_FROGLIGHT)) {
			this.doorLocs.forEach(loc -> {
				loc.clone().subtract(0, 1, 0).getBlock().setType(Material.REDSTONE_TORCH);
			});
		} else if (light.equals(Material.PEARLESCENT_FROGLIGHT)) {
			this.doorLocs.forEach(loc -> {
				loc.clone().subtract(0, 1, 0).getBlock().setType(Material.AIR);
			});
		}
	}

	SignActionPlatform b;
	public Timer groupAnnounceTask;
	int stops = 0;

	public Boolean execute(MinecartGroup group) {
		b = null;
		if (this.station.closed) {
			group.currentRoute.stops.remove(0);
			return true;
		}
		if (group.currentRoute.stops.size() > 0 && group.currentRoute.stops.get(0).equals(this)) {
			group.head().facing = this.node.direction;
			group.canProceed = false;
			stops = group.currentRoute.stops.size();
			group.prevStop = this;
			group.prevTS = group.head()._targetSpeed;
			if (group.currentRoute.stops.size() <= 1) {
				if (this.reverse) {
					// group.unVirtualize(true);
					group.loadNextRoute(false, true);
					group.head().facing = this.node.direction.getOppositeFace();
					group.tail().facing = this.node.direction.getOppositeFace();
					group.head().getEntity().syncPos(this.node.loc);
					group.reverse();
					group.getMembers().forEach(m -> {
						m.proceedTo(this.node.getLocation());
					});
				} else {
					group.loadNextRoute(true);
				}
			}
			headcode = group.getHeadcode().charAt(1);
			group.getMembers().forEach(m -> {
				m.currentSpeed = 0.0d;
				m._targetSpeed = 0.0d;
			});
			PIS pis;
			if (this.pis.get(headcode) != null) {
				pis = this.pis.get(headcode);
			} else {
				pis = TrainCarts.plugin.PisController.getPis(this.station.code + this.platform + headcode);
				this.pis.put(headcode, pis);
			}
			long dur = this.duration;
			pis.setArrived(true);
			if (this.mode == PisMode.DYNAMIC) {
				this.dynPis.addTimer(headcode.toString(), this.station, group.nextTrain, group.getLineColour());
				this.dynPis.setArrived(headcode.toString(), this.station, true, group.getLineColour());
			}
			TrainCarts.plugin.PisController.getPis(this.station.code + this.platform + headcode)
					.addTimer(group.nextTrain);
			pis.delay = 0;
			pis = null;
			n = group.currentRoute.name;
			this.setLights(Material.VERDANT_FROGLIGHT);
			if (!group.virtualized) {
				if (!group.getLine().getName().startsWith("!")) {
					List<String> ann = new ArrayList<String>();
					String c = group.getLine().getChar();
					ann.add("This station is " + this.station.name + ".");
					if (this.station.osi != c && this.station.osi != "" && this.station.osi.length() > 1) {
						ann.add(StationAnnouncements.parseMetro(this.station.osi, group.getLine()));
					}
					if (this.station.hosi != null && !this.station.hosi.equals("")) {
						ann.add(StationAnnouncements.parseRail(this.station.hosi, group.getLine(), (ann.size() > 1)));
					}
					/*
					 * if (this.station.station != "") {
					 * String s = this.station.generateConnection(group.getLine());
					 * ann.add(s);
					 * }
					 */
					if (stops == 1 && !group.getHeadcode().startsWith("0")) {
						ann.add("This train terminates here. All change please.");
					}
					group.announce(ann.get(0), false, ann.get(0).contains("{\"text"));
					ann.remove(0);
					groupAnnounceTask = new Timer();
					groupAnnounceTask.schedule(
							new java.util.TimerTask() {
								@Override
								public void run() {
									if (ann.size() > 0) {
										if (ann.get(0) != null) {
											group.announce(ann.get(0), false, ann.get(0).contains("{\"text"));
										}
										;
										ann.remove(0);
									} else {
										this.cancel();
										groupAnnounceTask = null;
									}
								}
							},
							2500L, 2500L);
				}
			}
			new java.util.Timer().schedule(
					new java.util.TimerTask() {
						@Override
						public void run() {
							depart(group);
							group.canProceed = true;
							group.getMembers().forEach(m -> {
								m._targetSpeed = group.prevTS;
								m._mod = 1.0;
							});
						}
					},
					dur * 1000);
			return true;
		} else {
			group.head().proceedTo(this.node.loc);
			return true;
		}
	}

	public void depart(MinecartGroup g) {
		if (g.currentRoute.stops == null || g.currentRoute == null) {
			return;
		}
		if (g.currentRoute.stops.size() > 0 && g.currentRoute.stops.get(0).equals(this)) {
			g.currentRoute.stops.remove(0);
		}
		if (TrainCarts.plugin.PisController != null) {
			TrainCarts.plugin.PisController.getPis(this.station.code + this.platform + headcode).setArrived(false);
			if (this.mode == PisMode.DYNAMIC) {
				this.dynPis.setArrived(headcode.toString(), this.station, false, g.getLineColour());
			}
		}
		return;
	}

	public Boolean exit(MinecartGroup group) {
		if (group.prevStop == null || !group.prevStop.equals(this))
			return true;
		if (!group.virtualized) {
			this.setLights(Material.PEARLESCENT_FROGLIGHT);
		}
		if (group.currentRoute.name.equals("DESPAWN")) {
			group.destroy();
		}
		if (!group.currentRoute._line.getName().equals("#GLOBAL") && group.currentRoute.stops.size() > 0) {
			if (!group.getLine().getName().startsWith("!")) {
				group.announce("This is " + (group.currentRoute._line.getName().equals("Airport") ? "the " : "a ") + group.currentRoute._line.getName() + " "
						+ (group.currentRoute._line.getName().equals("Airport") ? "Shuttle" : "Line service") + " to "
						+ group.currentRoute.stops.get(group.currentRoute.stops.size() - 1).station.name + ".");
			}
		} else {
			group.eject();
			group.destroy();
		}
		if (group.currentRoute == null) {
			return false;
		}
		if (!group.currentRoute.name.equals("[CACHED ROUTE]") && !group.getLine().getName().startsWith("!")) {
			TimerTask t = new java.util.TimerTask() {
				@Override
				public void run() {
					if (group.currentRoute.stops != null) {
						if (group.currentRoute.stops.size() > 0) {
							if (group.currentRoute.stops.get(0).station.closed) {
								group.announce("The next station is closed.");
							} else {
								group.announce(
										"The next station is " + group.currentRoute.stops.get(0).station.name + ".");
							}
						}
					}
				}
			};
			new java.util.Timer().schedule(t, 3000);

		}
		return true;
	}

	@Override
	public void postParse() {
		try {
			String[] a = this.content.split(" ");
			this.platform = a[1];
			Station b = TrainCarts.plugin.StationStore.getFromCode(a[2]);
			if (b == null) {
				TrainCarts.plugin.getLogger().log(Level.WARNING,
						this.content + " is an invalid SignActionPlatform sign.");
				this.platform = null;
				this.node.line.deleteNode(this.node);
				if (this.node.line != TrainCarts.plugin.global) {
					TrainCarts.plugin.global.deleteNode(this.node);
				}
				return;
			}
			this.station = b;
			b.addPlatform(a[1], this);
			try {
				this.duration = Long.parseLong(a[3]);
			} catch (NumberFormatException e) {
				TrainCarts.plugin.getLogger().log(Level.WARNING,
						this.content + " is an invalid SignActionPlatform sign.");
				this.platform = null;
				this.node.line.deleteNode(this.node);
				if (this.node.line != TrainCarts.plugin.global) {
					TrainCarts.plugin.global.deleteNode(this.node);
				}
				return;
			}
			String[] m = a[4].split(":");
			if (m[0].equals("dyn")) {
				this.mode = PisMode.DYNAMIC;
				this.dynPis = new DynamicPIS((m.length > 1 ? m[1] : this.station.code), this.station.displayName);
			}
			if (a.length > 6) {
				if (a[6].equals("R")) {
					this.reverse = true;
				}

			}
			this.offset = new Vector(0, 0, 0);
			Vector offset;
			Vector addition;
			switch (this.node.direction) {
				case EAST:
					offset = new Vector(0, 1, -2);
					addition = new Vector(3, 0, 0);
					break;
				case NORTH:
					offset = new Vector(-2, 1, 0);
					addition = new Vector(0, 0, -3);
					break;
				case SOUTH:
					offset = new Vector(2, 1, 0);
					addition = new Vector(0, 0, 3);
					break;
				case WEST:
					offset = new Vector(0, 1, 2);
					addition = new Vector(-3, 0, 0);
					break;
				default:
					offset = new Vector(0, 0, 0);
					addition = new Vector(0, 0, 0);
					this.doors = 0;
					break;
			}
			this.doors = Integer.valueOf(a[5]);
			Location z = this.sign.getBlock().getLocation().clone();
			Vector addition2 = addition.clone().divide(new Vector(3, 3, 3));
			Location light = z.clone().subtract(offset).add(0, 2, 0);
			if (light.getBlock().getType().equals(Material.JIGSAW)) {
				doorLocs = new ArrayList<Location>();
				lightLocs = new ArrayList<Location>();
				lightLocs.add(light.clone().subtract(addition2));
				for (int i = 0; i < this.doors; i++) {
					if (light.getBlock().getType().equals(Material.JIGSAW)) {
						doorLocs.add(light.clone());
						light.clone().subtract(0, 1, 0).getBlock().setType(Material.AIR);
					} else {
						break;
					}
					light.getBlock().setType(Material.JIGSAW);
					Jigsaw z2 = (Jigsaw) light.getBlock().getBlockData();
					z2.setOrientation(Orientation.valueOf(this.node.direction.name() + "_UP"));
					light.getBlock().setBlockData(z2);

					Location z3 = light.clone().add(addition2);
					z3.getBlock().setType(Material.JIGSAW);
					Jigsaw z4 = (Jigsaw) z3.getBlock().getBlockData();
					z4.setOrientation(Orientation.valueOf(this.node.direction.getOppositeFace().name() + "_UP"));
					z3.getBlock().setBlockData(z4);
					light.add(addition);
					light.subtract(addition2);
					lightLocs.add(light.clone());
					light.add(addition2);
				}
				lightLocs.forEach(loc -> {
					loc.getBlock().setType(Material.PEARLESCENT_FROGLIGHT);
				});
			}
			light = z.add(offset);
			if (light.getBlock().getType().equals(Material.JIGSAW)) {
				// doorLocs = new ArrayList<Location>();
				// lightLocs = new ArrayList<Location>();
				lightLocs.add(light.clone().subtract(addition2));
				for (int i = 0; i < this.doors; i++) {
					if (light.getBlock().getType().equals(Material.JIGSAW)) {
						doorLocs.add(light.clone());
						light.clone().subtract(0, 1, 0).getBlock().setType(Material.AIR);
					} else {
						break;
					}
					light.getBlock().setType(Material.JIGSAW);
					Jigsaw z2 = (Jigsaw) light.getBlock().getBlockData();
					z2.setOrientation(Orientation.valueOf(this.node.direction.name() + "_UP"));
					light.getBlock().setBlockData(z2);

					Location z3 = light.clone().add(addition2);
					z3.getBlock().setType(Material.JIGSAW);
					Jigsaw z4 = (Jigsaw) z3.getBlock().getBlockData();
					z4.setOrientation(Orientation.valueOf(this.node.direction.getOppositeFace().name() + "_UP"));
					z3.getBlock().setBlockData(z4);
					light.add(addition);
					light.subtract(addition2);
					lightLocs.add(light.clone());
					light.add(addition2);
				}
				lightLocs.forEach(loc -> {
					loc.getBlock().setType(Material.PEARLESCENT_FROGLIGHT);
				});
			}
			//this.setLights(Material.PEARLESCENT_FROGLIGHT);
		} catch (IndexOutOfBoundsException e) {
			TrainCarts.plugin.getLogger().log(Level.WARNING, this.content + " is an invalid SignActionPlatform sign.");
			this.platform = null;
			this.node.line.deleteNode(this.node);
			if (this.node.line != TrainCarts.plugin.global) {
				TrainCarts.plugin.global.deleteNode(this.node);
			}
			return;
		}
		return;
	}

	@Override
	public Boolean match(String s) {
		return s.toLowerCase().equals("t:plat");
	}

	@Override
	public String getAction() {
		return "SignActionPlatform";
	}

	@Override
	public Double getSpeedLimit(MinecartGroup g) {
		if (!this.station.closed && g.currentRoute.stops != null && g.currentRoute.stops.size() > 0
				&& g.currentRoute.stops.get(0).equals(this)) {
			return 0.0;
		} else {
			return null;
		}
	}



	@Override
	public void handleBuild(Player p) {
		TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
		TextComponent clickable = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "PLATFORM");
		TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
		TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to make a train stop at a station.");
		clickable.setClickEvent(
				new ClickEvent(ClickEvent.Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.PLATFORM_SIGN.id));
		p.spigot().sendMessage(m1, clickable, m2, m3);
	}
}
