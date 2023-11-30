package com.movies22.cashcraft.tc.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class VirtualMinecart {
   private Entity ent = null;
   private MinecartMember pivot = null;
   private Double offset = 0.0D;
   private Boolean virtualized = false;

   public VirtualMinecart(Entity e, MinecartMember m, Double o) {
      this.ent = e;
      this.pivot = m;
      this.offset = o;
   }

   public Location getLocation() {
      return this.getLocation(false);
   }

   public Location getLocation(Boolean offsetted) {
      if (!this.virtualized) {
         return this.ent.getLocation();
      } else {
         Entity e = this.pivot.getEntity();
         if (!offsetted) {
            return e.getLocation();
         } else {
            Location l = e.getLocation().clone();
            switch(this.pivot.facing) {
            case 1:
               l.add(0.0D, 0.0D, this.offset);
               break;
            case 2:
               l.subtract(this.offset, 0.0D, 0.0D);
               break;
            case 3:
               l.subtract(0.0D, 0.0D, this.offset);
               break;
            case 4:
               l.add(this.offset, 0.0D, 0.0D);
            }

            return l;
         }
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
      return !this.virtualized ? this.ent : null;
   }

   public Boolean load() {
      return this.load(false);
   }

   public Boolean load(Boolean offset) {
      if (this.virtualized) {
         Location l = this.getLocation(offset);
         World w = l.getWorld();
         Entity e = w.spawnEntity(l, EntityType.MINECART);
         this.setVirtualized(false);
         this.ent = e;
      }

      return true;
   }

   public void setPivot(MinecartMember mm) {
      this.pivot = mm;
   }

}
