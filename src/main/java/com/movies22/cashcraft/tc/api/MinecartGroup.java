package com.movies22.cashcraft.tc.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bergerkiller.bukkit.common.chunk.ForcedChunk;
import com.bergerkiller.bukkit.common.wrappers.LongHashSet;
import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.PathFinding.PathOperation;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;
import com.movies22.cashcraft.tc.controller.ChunkArea;
import com.movies22.cashcraft.tc.utils.Despawn;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.chat.ComponentSerializer;

public class MinecartGroup {
	private static final LongHashSet chunksBuffer = new LongHashSet(50);
	private String headcode;
	private MetroLine line;
	private int length;
	private PathNode _dest;
	private PathNode _spawn;
	private List<PathOperation> route;
	private List<MinecartMember> members = new ArrayList<MinecartMember>();
	public List<PathRoute> routes = new ArrayList<PathRoute>();
	protected final ChunkArea chunkArea = new ChunkArea();
    private Double targetSpeed = 0.4;
	private Double previousSpeed = null;
	public boolean despawned = false;
	public int nextRoute = 0;
	public PathRoute currentRoute;
	public int nextTrain = 0;
	public Location lastCurve = null;
	public Boolean virtualized = false;
	public List<ForcedChunk> chunks = new ArrayList<ForcedChunk>();
	public MinecartGroup(MetroLine line, String headcode, int length) {
		this.setLine(line);
		this.setHeadcode(headcode);
		this._setLength(length);
		line.addTrain(this);
	}
	
	public Boolean spawn(PathNode node) {
		BlockFace f = node.direction;
		this._spawn = node;
		
		World w = node.getLocation().getWorld();
		Entity e;
		Minecart m;
		MinecartMember mm;
		Location l = node.loc.clone();
		Vector vec = new Vector(0.0, 0.0, 0.0);
		switch(f) {
			case EAST:
				vec = new Vector(-1.5, 0.0, 0.0);
				break;
			case NORTH:
				vec = new Vector(0.0, 0.0, 1.5);
				break;
			case SOUTH:
				vec = new Vector(0.0, 0.0, -1.5);
				break;
			case WEST:
				vec = new Vector(1.5, 0.0, 0.0);
				break;
			default:
				break;
		}
		for (int i = 0; i < this._getLength(); i++) {
			e = w.spawnEntity(l, EntityType.MINECART);
			m = (Minecart) e;
			m.setSlowWhenEmpty(false);
			mm = new MinecartMember(this, m, i);
			this.addMember(mm);
			l.add(vec);
		}
		this.updateChunkInformation(true,  despawned);
		this.loadNextRoute(true);
		this.members.forEach(minecart -> {
			minecart.spawned = true;
		});
		return true;
	}
	
	public void addRoute(PathRoute r) {
		this.routes.add(r);
	}
	
	public PathRoute getNextRoute() {
		return this.routes.get(0);
	}
	
	public PathRoute getLastRoute() {
		return this.routes.get(this.routes.size() - 1);
	}
	String z = "";
	public PathRoute loadNextRoute() {
		return loadNextRoute(false);
	}
	
	public PathRoute loadNextRoute(Boolean f) {
		return this.loadNextRoute(f, true);
	}
	
	public PathRoute loadNextRoute(Boolean f, Boolean h) {
		PathRoute r = this.routes.get(0).clone();
		this.currentRoute = r.clone();
		if(h) {
		String a = "2";
		String b;
		if(r.stops.size() > 0) {
			b = r.stops.get(r.stops.size() - 1).station.headcode;
		} else {
			b = "X";
		}
		if(!r.name.equals("[CACHED ROUTE]")) {
			if(r.stops.size() > 0) {
				b = r.stops.get(r.stops.size() - 1).station.headcode;
			}
		}
		if(r.name.equals("[CACHED ROUTE]")) {
			a = "0";
			if(this.routes.size() > 1 && this.routes.get(1).stops.size() > 1) {
				b = this.routes.get(1).stops.get(this.routes.get(1).stops.size() - 1).station.headcode;
			} else if(this.routes.size() > 0 && this.routes.get(0).stops.size() > 1) {
				b = this.routes.get(0).stops.get(this.routes.get(0).stops.size() - 1).station.headcode;
			} else {
				b = "X";
			}
		} 
		if(r._line.getName().equals("Yellow")) {
			a = "1";
		} else if(r._line.getName().equals("Green")) {
			a = "2";
		} else if(r._line.getName().equals("Pink")) {
			a = "3";
		} else if(r._line.getName().equals("Blue")) {
			a = "4";
		} else if(r._line.getName().equals("Orange")) {
			a = "5";
		} else if(r._line.getName().equals("Red")) {
			a = "6";
		} else if(r._line.getName().equals("Purple")) {
			a = "7";
		} else if(r._line.getName().equals("Cyan")) {
			a = "8";
		} else {
			a = "0";
		}
		this.headcode = TrainCarts.plugin.DepotStore.getNextHeadcode(a + b);
		}
		this.setRoute(r.route);
		this.route = r.route;
		if(f) {
			this.members.forEach(member -> {
				member.setLocalRoute(new ArrayList<PathOperation>(r.route));
			});
			this.nextRoute = this.members.size();
			this.routes.remove(0);
		} else {
			this.routes.remove(0);
		}
		return r;
	}
	
	public Boolean destroy() {
		return destroy(null, null);
	}

	public Boolean destroy(Despawn reason) {
		return destroy(reason, null);	
	}
	
	public void keepLoaded(Boolean b) {
		this.updateChunkInformation(true,  despawned);
		for (ChunkArea.OwnedChunk chunk : this.chunkArea.getAll()) {
            chunk.keepLoaded(b);
        }
	}
	
	
	
	private LongHashSet loadChunksBuffer() {
        chunksBuffer.clear();
        for (MinecartMember mm : this.getMembers()) {
        	if(!mm.virtualized) {
        		chunksBuffer.add(mm.getEntity().getLocation().getChunk().getX(), mm.getEntity().getLocation().getChunk().getZ());
        	}
        }
        return chunksBuffer;
    }
	
	private void updateChunkInformation(boolean keepChunksLoaded, boolean isRemoving) {
            this.chunkArea.refresh(this._spawn.loc.getWorld(), this.loadChunksBuffer());
            if (keepChunksLoaded) {
                for (ChunkArea.OwnedChunk chunk : this.chunkArea.getAdded()) {
                    chunk.keepLoaded(true);
                }
                for (ChunkArea.OwnedChunk chunk : this.chunkArea.getAll()) {
                    if (chunk.getDistance() <= 1 && chunk.getPreviousDistance() > 1) {
                        chunk.loadChunk();
                    }
                }
            }
    }
	
	
	private String label;
	public Boolean destroy(Despawn reason, HashMap<String, String> keys) {
		if(!despawned) {
			this.chunkArea.reset();
			this.chunks.clear();
			despawned = true;
			if(reason != null ) {
				label = reason.label;
				if(keys != null) {
					keys.keySet().forEach(k -> {
						label = label.replaceAll("\\{" + k + "\\}", keys.get(k));
					});
				}
			}
		for (MinecartMember m : this.getMembers()) {
			m.getEntity().getPassengers().forEach(r -> {
				if (r instanceof Player) {
					Player p = (Player) r;
					if(reason != null ) {
						p.spigot().sendMessage(ChatMessageType.ACTION_BAR, ComponentSerializer.parse("{\"text\":\"" + label + "\", \"color\":\"dark_red\",\"bold\":true}"));
					}
					p.playSound(p, Sound.ENTITY_CREEPER_PRIMED, 1, 1);
				}
			});
			TrainCarts.plugin.MemberStore.removeMember(m);;
			m.destroy();
		}
		if(reason != null ) {
		TrainCarts.plugin.getServer().getOnlinePlayers().forEach(p -> {
			p.sendMessage(ChatColor.DARK_RED + "Despawned " + this.headcode + ": "+  ChatColor.RED + label);
		});
		TrainCarts.plugin.getLogger().log(Level.WARNING, "Despawned " + this.headcode + ": " + label);
		}
		if(this.head().getNextNode() != null) {
			this.head().getNextNode().onBlock = null;
		}
			this.line.removeTrain(this);
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean eject() {
		for (MinecartMember m : this.getMembers()) {
			m.eject();
		}
		return true;
	}

	public void setLine(MetroLine l) {
		this.line = l;
	}

	public MetroLine getLine() {
		return this.line;
	}

	public String getLineColour() {
		return this.line.getColour();
	}

	public void setHeadcode(String s) {
		this.headcode = s;
	}

	public String getHeadcode() {
		return this.headcode;
	}

	public int _getLength() {
		return length;
	}

	public void _setLength(int length) {
		this.length = length;
	}

	public List<MinecartMember> getMembers() {
		return members;
	}

	public void addMember(MinecartMember m) {
		this.members.add(m);
	}

	public void reverse() {
		this.nextRoute = 0;
		this.members.forEach(m -> {
			TrainCarts.plugin.MemberStore.removeMember(m);
		});
		List<MinecartMember> a = new ArrayList<MinecartMember>(this.members);
		this.members.clear();
		for(int i = (a.size() - 1); i >= 0; i--) {
			a.get(i).index = (a.size() - i - 1);
			this.members.add(a.get(i));
		}
		this.members.forEach(m -> {
			TrainCarts.plugin.MemberStore.addMember(m);
		});
		this.head().lastAction = this.tail().lastAction;
		this.tail().lastAction = null;
	}
	
	public void removeMember(MinecartMember m) {
		int i = this.members.indexOf(m);

		List<MinecartMember> mm = new ArrayList<MinecartMember>();

		for (int a = 0; a < i; a++) {
			mm.set(a, this.members.get(a));
		}

		this.members.remove(m);

		for (int a = i + 1; a < this.members.size(); a++) {
			mm.set((a - 1), this.members.get(a));
		}

		this.members = mm;
	}

	public MinecartMember getMember(int i) {
		return this.members.get(i);
	}

	public void setMaxSpeed(Double maxSpeed) {
		this.targetSpeed = maxSpeed;
	}

	public PathNode _getSpawn() {
		return _spawn;
	}

	public void _setSpawn(PathNode dest) {
		this._spawn = dest;
	}

	public PathNode _getDest() {
		return _dest;
	}

	public void setDestination(PathNode dest) {
		this._dest = dest;
	}

	public List<PathOperation> getRoute() {
		return route;
	}
	public MinecartMember head() {
		if(this.members.size() == 0) {
			int i = 0;
			while(this.members.size() == 0 && i < 100) {
				i++;
			}
			if(i > 99) {
				return null;
			}
		}
		return this.members.get(0);
	}
	
	public MinecartMember tail() {
		if(this.members.size() == 0) {
			int i = 0;
			while(this.members.size() == 0 && i < 100) {
				i++;
			}
			if(i > 99) {
				return null;
			}
		}
		return this.members.get(this.members.size() - 1);
	}
	public void setRoute(List<PathOperation> route) {
		this.members.forEach(m -> {
			m.setLocalRoute(new ArrayList<PathOperation>(route));
		});
	}

	public Double getTargetSpeed() {
		return targetSpeed;
	}

	public void setTargetSpeed(Double targetSpeed) {
		this.targetSpeed = targetSpeed;
	}

	public Double getPreviousSpeed() {
		return previousSpeed;
	}

	public void setPreviousSpeed(Double previousSpeed) {
		this.previousSpeed = previousSpeed;
	}
	
	
	public void announce(String a) {
		announce(a, false, false);
	}
	
	@Override
	public String toString() {
		return this.headcode + " | " + this.hashCode();
	}
	private String c;
	public void announce(String a, Boolean s, Boolean r) {
		c = a;
		if(r && !a.endsWith("]")) {
				c = c + "]";
		}
		this.members.forEach(m -> {
			m.getEntity().getPassengers().forEach(e -> {
				if(e instanceof Player) {
				Player p = (Player) e;
				if(r) {
					p.spigot().sendMessage(ChatMessageType.ACTION_BAR, ComponentSerializer.parse(c));
				} else {
					p.spigot().sendMessage(ChatMessageType.ACTION_BAR, ComponentSerializer.parse("{\"text\":\"" + a + "\", \"color\":\"" + this.line.getColour() + "\"}"));
				}
				if(!s) {
					p.playSound(p, Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
					new java.util.Timer().schedule(new java.util.TimerTask() {
						@Override
						public void run() {
							p.playSound(p, Sound.BLOCK_NOTE_BLOCK_CHIME, 1, (float) 0.85);
						}
					}, 300);
				} 
				} else {
					Damageable e2 = (Damageable) e;
					e2.damage(Double.MAX_VALUE);
				}
			});
		});
	}
	
	public void virtualize() {
		this.head().spawned = false;
		this.virtualized = true;
		for(int i = 1; i < this.members.size(); i++) {
			MinecartMember mm = this.members.get(i);
			mm.spawned = false;
			TrainCarts.plugin.getLogger().log(Level.INFO, "Virtualizing..." + mm.index + "/" + i );
			mm.virtualize();
		}
		this.head().spawned = true;

	}
	
	public void unVirtualize() {
		for(int i = 1; i < this.members.size(); i++) {
			MinecartMember mm = this.members.get(i);
			if(mm.virtualized) {
				Boolean b = mm.load();
				if(!b) {
					return;
				}
			}
		}
		this.virtualized = false;
	}
}
