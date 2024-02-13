package com.movies22.cashcraft.tc.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;


public class VirtualMinecart2 {
	private Entity ent = null;
	private MinecartMember pivot = null;
	private Double offset = 0.0d;
	private Boolean virtualized = false;
	private Location loc;
	public VirtualMinecart2(Entity e, MinecartMember m, Double o) {
		this.ent = e;
		this.pivot = m;
		this.offset = o;
	};
	
	public Location getLocation() {
		if(this.getLocation(false) != null) {
			this.loc = this.getLocation(false);
		};
		return this.loc;
	}
	
	public Location getLocation(Boolean b) {
		if(!virtualized) {
			this.loc = ent.getLocation();
			return ent.getLocation();
		} else {
			Entity e = pivot.getEntity();
			if(e == null) return null;
			if(b) {
				return e.getLocation();
			}
			Location l = e.getLocation().clone();
			switch(this.pivot.facing) {
				case EAST:
					l.add(this.offset, 0, 0);
					break;
				case NORTH:
					l.subtract(0, 0, this.offset);
					break;
				case SOUTH:
					l.add(0, 0, this.offset);
					break;
				case WEST:
					l.subtract(this.offset, 0, 0);
					break;
				default:
					break;
				
			}
			this.loc = l;
			return l;
		}
	}

	public void setVelocity(Vector speed) {
		if(this.ent != null && !this.ent.isDead()) {
			this.ent.setVelocity(speed);
		} else {
			this.loc.add(speed);
		}
	}

	public Vector getVelocity() {
		return this.ent.getVelocity();
	}
	
	public void setVirtualized(Boolean b) {
		this.virtualized = b;
	}
	
	public void setOffset(Double o) {
		this.offset = o;
	}
	
	public Boolean getVirtualized() {
		return this.virtualized;
	}
	
	public Entity getEntity() {
		if(!this.virtualized) {
			return ent;
		}
		return null;
	}
	
	public Boolean load() {
		return this.load(false);
	}
	
	public Boolean load(Boolean b) {
		if(this.virtualized) {
			Location l = this.getLocation(!b);
			World w = l.getWorld();
			Entity e = (Entity) w.spawnEntity(l, EntityType.MINECART);
			this.setVirtualized(false);
			this.ent = e;
		}
		return true;
	}
	
	public void setPivot(MinecartMember mm) {
		this.pivot = mm;
	}
}
 
