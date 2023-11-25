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
<<<<<<< Updated upstream
=======
		return this.getLocation(false);
	}
	
	public Location getLocation(Boolean offsetted) {
>>>>>>> Stashed changes
		if(!virtualized) {
			return ent.getLocation();
		} else {
			Entity e = pivot.getEntity();
<<<<<<< Updated upstream
			Location l;
			Location l2 = e.getLocation().clone();
			switch(pivot.facing) {
				case NORTH:
					l = l2.add(0, 0, -this.offset);
					break;
				case EAST:
					l = l2.add(-this.offset, 0, 0);
					break;
				case SOUTH:
					l = l2.add(0, 0, this.offset);
=======
			if(!offsetted) {
				return e.getLocation();
			}
			Location l = e.getLocation().clone();
			switch(this.pivot.facing) {
				case EAST:
					l.subtract(this.offset, 0, 0);
					break;
				case NORTH:
					l.add(0, 0, this.offset);
>>>>>>> Stashed changes
					break;
				case SOUTH:
					l.subtract(0, 0, this.offset);
					break;
				case WEST:
<<<<<<< Updated upstream
					l =l2.add(this.offset, 0, 0);
=======
					l.add(this.offset, 0, 0);
>>>>>>> Stashed changes
					break;
				default:
					l = l2;
					break;
			};
			return l;
		}
	}
	
	public void setVirtualized(Boolean b) {
		this.virtualized = b;
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
<<<<<<< Updated upstream
		Location l = this.getLocation();
		World w = l.getWorld();
		Entity e = (Entity) w.spawnEntity(l, EntityType.MINECART);
		this.setVirtualized(false);
		this.ent = e;
=======
		return this.load(false);
	}
	
	public Boolean load(Boolean offset) {
		if(this.virtualized) {
			Location l = this.getLocation(offset);
			World w = l.getWorld();
			Entity e = (Entity) w.spawnEntity(l, EntityType.MINECART);
			this.setVirtualized(false);
			this.ent = e;
		}
>>>>>>> Stashed changes
		return true;
	}
	
	public void setPivot(MinecartMember mm) {
		this.pivot = mm;
	}
}
