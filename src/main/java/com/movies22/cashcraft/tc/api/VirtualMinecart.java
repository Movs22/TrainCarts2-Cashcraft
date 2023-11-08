
package com.movies22.cashcraft.tc.api;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class VirtualMinecart {
	private Entity ent = null;
	private MinecartMember pivot = null;
	private Double offset = 0.0d;
	private Boolean virtualized = false;
	public VirtualMinecart(Entity e, MinecartMember m, Double o) {
		this.ent = e;
		this.pivot = m;
		this.offset = o;
	};
	
	public Location getLocation() {
		return this.getLocation(false);
	}
	
	public Location getLocation(Boolean b) {
		if(!virtualized) {
			return ent.getLocation();
		} else {
			Entity e = pivot.getEntity();
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
			return l;
		}
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
