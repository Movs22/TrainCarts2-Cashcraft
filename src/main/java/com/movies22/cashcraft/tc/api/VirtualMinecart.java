package com.movies22.cashcraft.tc.api;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;

import com.movies22.cashcraft.tc.TrainCarts;

public class VirtualMinecart {
	private Entity ent = null;
	private MinecartMember pivot = null;
	private Double offset = 0.0d;
	private Boolean virtualized = false;
	public VirtualMinecart(Entity e, MinecartMember m, Double o) {
		this.ent = e;
		this.pivot = m;
		this.offset = o;
		TrainCarts.plugin.getLogger().log(Level.INFO, "Virtualized entity " + e.hashCode() + ". Pivot: " + m.index + ". Offset: " + o );
	};
	
	public Location getLocation() {
		if(!virtualized) {
			return ent.getLocation();
		} else {
			Entity e = pivot.getEntity();
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
					break;
				case WEST:
					l =l2.add(this.offset, 0, 0);
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
		Location l = this.getLocation();
		if(l.getBlock().getType().equals(Material.POWERED_RAIL)) {
			World w = l.getWorld();
			Entity e = w.spawnEntity(l, EntityType.MINECART);
			this.ent = e;
			return true;
		} else {
			return false;
		}
	}
}
