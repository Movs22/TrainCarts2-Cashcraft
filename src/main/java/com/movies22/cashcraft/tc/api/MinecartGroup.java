package com.movies22.cashcraft.tc.api;

import com.bergerkiller.bukkit.common.chunk.ForcedChunk;
import com.bergerkiller.bukkit.common.wrappers.LongHashSet;
import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.PathFinding.PathOperation;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.controller.ChunkArea;
import com.movies22.cashcraft.tc.controller.PlayerController;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;
import com.movies22.cashcraft.tc.utils.Despawn;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.chat.ComponentSerializer;
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

public class MinecartGroup {
   private static final LongHashSet chunksBuffer = new LongHashSet(50);
   private String headcode;
   private MetroLines.MetroLine line;
   private int length;
   private PathNode _dest;
   private PathNode _spawn;
   private List<PathOperation> route;
   private List<MinecartMember> members = new ArrayList();
   public List<PathRoute> routes = new ArrayList();
   protected final ChunkArea chunkArea = new ChunkArea();
   private Double targetSpeed = 0.4D;
   private Double previousSpeed = null;
   public boolean despawned = false;
   public int nextRoute = 0;
   public PathRoute currentRoute;
   public int nextTrain = 0;
   public Boolean recording = false;
   public Location lastCurve = null;
   public Boolean virtualized = false;
   public Boolean isEmpty = true;
   public Boolean canProceed = true;
   public SignActionPlatform currentPlat = null;
   public List<ForcedChunk> chunks = new ArrayList();
   String z = "";
   private String label;
   private String c;
   

   public MinecartGroup(MetroLines.MetroLine line, String headcode, int length) {
      this.setLine(line);
      this.setHeadcode(headcode);
      this._setLength(length);
   }

   public Boolean spawn(PathNode node) {
      BlockFace f = node.direction;
      this._spawn = node;
      World w = node.getLocation().getWorld();
      Location l = node.loc.clone();
      Vector vec = new Vector(0.0D, 0.0D, 0.0D);
      switch(f) {
			case EAST:
				vec = new Vector(-1.2, 0.0, 0.0);
				break;
			case NORTH:
				vec = new Vector(0.0, 0.0, 1.2);
				break;
			case SOUTH:
				vec = new Vector(0.0, 0.0, -1.2);
				break;
			case WEST:
				vec = new Vector(1.2, 0.0, 0.0);
				break;
			default:
				break;
		}

      for(int i = 0; i < this._getLength(); ++i) {
         Entity e = w.spawnEntity(l, EntityType.MINECART);
         Minecart m = (Minecart)e;
         m.setSlowWhenEmpty(false);
         MinecartMember mm = new MinecartMember(this, m, i);
         this.addMember(mm);
         l.add(vec);
      }

      this.updateChunkInformation(true, this.despawned);
      this.loadNextRoute(true);
      this.members.forEach((minecart) -> {
         minecart.spawned = true;
         minecart.proceedTo(this._spawn.loc);
      });
      this.line.addTrain(this);
      return true;
   }

   public void addRoute(PathRoute r) {
      this.routes.add(r);
   }

   public PathRoute getNextRoute() {
      return (PathRoute)this.routes.get(0);
   }

   public PathRoute getLastRoute() {
      return (PathRoute)this.routes.get(this.routes.size() - 1);
   }

   public PathRoute loadNextRoute() {
      return this.loadNextRoute(false);
   }

   public PathRoute loadNextRoute(Boolean f) {
      return this.loadNextRoute(f, true);
   }

   public PathRoute loadNextRoute(Boolean f, Boolean h) {
      if (this.head() != null && this.head().getNextNode() != null) {
         this.head().getNextNode().onBlock = null;
      }

      PathRoute r = ((PathRoute)this.routes.get(0)).clone();
      this.currentRoute = r.clone();
      if (h) {
         String a = "2";
         String b;
         if (r.stops.size() > 0) {
            b = ((SignActionPlatform)r.stops.get(r.stops.size() - 1)).station.headcode;
         } else {
            b = "X";
         }

         if (!r.name.equals("[CACHED ROUTE]") && r.stops.size() > 0) {
            b = ((SignActionPlatform)r.stops.get(r.stops.size() - 1)).station.headcode;
         }

         if (r.name.equals("[CACHED ROUTE]")) {
            a = "0";
            if (this.routes.size() > 1 && ((PathRoute)this.routes.get(1)).stops.size() > 1) {
               b = ((SignActionPlatform)((PathRoute)this.routes.get(1)).stops.get(((PathRoute)this.routes.get(1)).stops.size() - 1)).station.headcode;
            } else if (this.routes.size() > 0 && ((PathRoute)this.routes.get(0)).stops.size() > 1) {
               b = ((SignActionPlatform)((PathRoute)this.routes.get(0)).stops.get(((PathRoute)this.routes.get(0)).stops.size() - 1)).station.headcode;
            } else {
               b = "X";
            }
         }

         if (r._line.getName().equals("Yellow")) {
            a = "1";
         } else if (r._line.getName().equals("Green")) {
            a = "2";
         } else if (r._line.getName().equals("Pink")) {
            a = "3";
         } else if (r._line.getName().equals("Blue")) {
            a = "4";
         } else if (r._line.getName().equals("Orange")) {
            a = "5";
         } else if (r._line.getName().equals("Red")) {
            a = "6";
         } else if (r._line.getName().equals("Purple")) {
            a = "7";
         } else if (r._line.getName().equals("Cyan")) {
            a = "8";
         } else if (r._line.getName().equals("Grey")) {
            a = "9";
         } else {
            a = "0";
         }

         this.headcode = TrainCarts.plugin.DepotStore.getNextHeadcode(a + b);
      }

      this.setRoute(r.route);
      this.route = r.route;
      if (f) {
         this.members.forEach((member) -> {
            member.setLocalRoute(new ArrayList(r.route));
         });
         this.nextRoute = this.members.size();
         this.routes.remove(0);
      } else {
         this.routes.remove(0);
      }

      return r;
   }

   public Boolean destroy() {
      return this.destroy((Despawn)null, (HashMap)null);
   }

   public Boolean destroy(Despawn reason) {
      return this.destroy(reason, (HashMap)null);
   }

   public void keepLoaded(Boolean b) {
      this.updateChunkInformation(true, this.despawned);
      Iterator var3 = this.chunkArea.getAll().iterator();

      while(var3.hasNext()) {
         ChunkArea.OwnedChunk chunk = (ChunkArea.OwnedChunk)var3.next();
         chunk.keepLoaded(b);
      }

   }

   private LongHashSet loadChunksBuffer() {
      chunksBuffer.clear();
      Iterator var2 = this.getMembers().iterator();

      while(var2.hasNext()) {
         MinecartMember mm = (MinecartMember)var2.next();
         if (mm.getEntity() != null) {
            chunksBuffer.add(mm.getEntity().getLocation().getChunk().getX(), mm.getEntity().getLocation().getChunk().getZ());
         }
      }

      return chunksBuffer;
   }

   private void updateChunkInformation(boolean keepChunksLoaded, boolean isRemoving) {
      this.chunkArea.refresh(this._spawn.loc.getWorld(), this.loadChunksBuffer());
      if (keepChunksLoaded) {
         Iterator var4 = this.chunkArea.getAdded().iterator();

         ChunkArea.OwnedChunk chunk;
         while(var4.hasNext()) {
            chunk = (ChunkArea.OwnedChunk)var4.next();
            chunk.keepLoaded(true);
         }

         var4 = this.chunkArea.getAll().iterator();

         while(var4.hasNext()) {
            chunk = (ChunkArea.OwnedChunk)var4.next();
            if (chunk.getDistance() <= 1 && chunk.getPreviousDistance() > 1) {
               chunk.loadChunk();
            }
         }
      }

   }

   public Boolean destroy(Despawn reason, HashMap<String, String> keys) {
      if (!this.despawned) {
         this.chunkArea.reset();
         this.chunks.clear();
         this.despawned = true;
         if (reason != null) {
            this.label = reason.label;
            if (keys != null) {
               keys.keySet().forEach((k) -> {
                  this.label = this.label.replaceAll("\\{" + k + "\\}", (String)keys.get(k));
               });
            }
         }

         this.isEmpty = true;

         MinecartMember m;
         for(Iterator var4 = this.getMembers().iterator(); var4.hasNext(); m.destroy()) {
            m = (MinecartMember)var4.next();
            if (!m.virtualized) {
               m.getEntity().getPassengers().forEach((r) -> {
                  if (r instanceof Player) {
                     this.isEmpty = false;
                     Player p = (Player)r;
                     if (reason != null) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, ComponentSerializer.parse("{\"text\":\"" + this.label + "\", \"color\":\"dark_red\",\"bold\":true}"));
                     }

                     p.playSound(p, Sound.ENTITY_CREEPER_PRIMED, 1.0F, 1.0F);
                  }

               });
               TrainCarts.plugin.MemberController.removeMember(m);
            }
         }

         if (reason != null) {
            TrainCarts.plugin.getServer().getOnlinePlayers().forEach((p) -> {
               p.sendMessage(ChatColor.DARK_RED + "Despawned " + this.headcode + ": " + ChatColor.RED + this.label);
            });
            TrainCarts.plugin.getLogger().log(Level.WARNING, "Despawned " + this.headcode + ": " + this.label);
         }

         if (this.head().getNextNode() != null) {
            this.head().getNextNode().onBlock = null;
         }

         this.line.removeTrain(this);
         this._dest = null;
         this._spawn = null;
         this.chunks.clear();
         this.chunks = null;
         this.currentRoute.clear();
         this.route = null;
         this.routes = null;
         return true;
      } else {
         return false;
      }
   }

   public Boolean eject() {
      Iterator var2 = this.getMembers().iterator();

      while(var2.hasNext()) {
         MinecartMember m = (MinecartMember)var2.next();
         m.eject();
      }

      return true;
   }

   public void setLine(MetroLines.MetroLine l) {
      this.line = l;
   }

   public MetroLines.MetroLine getLine() {
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
      return this.length;
   }

   public void _setLength(int length) {
      this.length = length;
   }

   public List<MinecartMember> getMembers() {
      return this.members;
   }

   public void addMember(MinecartMember m) {
      this.members.add(m);
   }

   public void reverse() {
      this.nextRoute = 0;
      this.members.forEach((m) -> {
         TrainCarts.plugin.MemberController.removeMember(m);
      });
      List<MinecartMember> a = new ArrayList(this.members);
      this.members.clear();

      for(int i = a.size() - 1; i >= 0; --i) {
         ((MinecartMember)a.get(i)).index = a.size() - i - 1;
         this.members.add((MinecartMember)a.get(i));
      }

      this.members.forEach((m) -> {
         m.setPivot(this.head());
         m.setOffset((double)m.index * 1.5D);
         TrainCarts.plugin.MemberController.addMember(m);
      });
      if (this.head().getNextNode() != null) {
         this.head().getNextNode().onBlock = null;
      }

      this.head().lastAction = this.tail().lastAction;
      this.head().lastAction.executed.remove(this);
      this.tail().lastAction = null;
   }

   public void removeMember(MinecartMember m) {
      int i = this.members.indexOf(m);
      List<MinecartMember> mm = new ArrayList();

      int a;
      for(a = 0; a < i; ++a) {
         mm.set(a, (MinecartMember)this.members.get(a));
      }

      this.members.remove(m);

      for(a = i + 1; a < this.members.size(); ++a) {
         mm.set(a - 1, (MinecartMember)this.members.get(a));
      }

      this.members = mm;
   }

   public MinecartMember getMember(int i) {
      return (MinecartMember)this.members.get(i);
   }

   public void setMaxSpeed(Double maxSpeed) {
      this.targetSpeed = maxSpeed;
   }

   public PathNode _getSpawn() {
      return this._spawn;
   }

   public void _setSpawn(PathNode dest) {
      this._spawn = dest;
   }

   public PathNode _getDest() {
      return this._dest;
   }

   public void setDestination(PathNode dest) {
      this._dest = dest;
   }

   public List<PathOperation> getRoute() {
      return this.route;
   }

   public MinecartMember head() {
      if (this.members.size() == 0) {
         int i;
         for(i = 0; this.members.size() == 0 && i < 100; ++i) {
         }

         if (i > 99) {
            return null;
         }
      }

      return (MinecartMember)this.members.get(0);
   }

   public MinecartMember tail() {
      return this.tail(0);
   }

   public MinecartMember tail(int z) {
      if (this.members.size() == 0) {
         int i;
         for(i = 0; this.members.size() == 0 && i < 100; ++i) {
         }

         if (i > 99) {
            return null;
         }
      }

      if (this.members.size() - 1 - z > 0) {
         return (MinecartMember)this.members.get(this.members.size() - 1 - z);
      } else {
         return this.members.size() - 1 > 0 ? this.tail(0) : null;
      }
   }

   public void setRoute(List<PathOperation> route) {
      this.members.forEach((m) -> {
         m.setLocalRoute(new ArrayList(route));
      });
   }

   public Double getTargetSpeed() {
      return this.targetSpeed;
   }

   public void setTargetSpeed(Double targetSpeed) {
      this.targetSpeed = targetSpeed;
   }

   public Double getPreviousSpeed() {
      return this.previousSpeed;
   }

   public void setPreviousSpeed(Double previousSpeed) {
      this.previousSpeed = previousSpeed;
   }

   public void announce(String a) {
      this.announce(a, false, false);
   }

   public String toString() {
      return this.headcode + " | " + this.hashCode();
   }

   public void announce(String a, Boolean s, Boolean r) {
      if (!this.virtualized) {
         this.c = a;
         if (r && !a.endsWith("]")) {
            this.c = this.c + "]";
         }

         this.members.forEach((m) -> {
            m.getEntity().getPassengers().forEach((e) -> {
               if (e instanceof Player) {
                  final Player p = (Player)e;
                  if (r) {
                     p.spigot().sendMessage(ChatMessageType.ACTION_BAR, ComponentSerializer.parse(this.c));
                  } else {
                     p.spigot().sendMessage(ChatMessageType.ACTION_BAR, ComponentSerializer.parse("{\"text\":\"" + a + "\", \"color\":\"" + this.line.getColour() + "\"}"));
                  }

                  if (!s) {
                     p.playSound(p, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0F, 1.0F);
                     (new Timer()).schedule(new TimerTask() {
                        public void run() {
                           p.playSound(p, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0F, 0.85F);
                        }
                     }, 300L);
                  }
               } else {
                  Damageable e2 = (Damageable)e;
                  e2.damage(Double.MAX_VALUE);
               }

            });
         });
      }
   }

   public void virtualize() {
      if (this.head().currentSpeed > 0.05D) {
         this.head().spawned = false;
         this.virtualized = true;

         for(int i = 1; i < this.members.size(); ++i) {
            MinecartMember mm = (MinecartMember)this.members.get(i);
            mm.spawned = false;
            TrainCarts.plugin.MemberController.removeMember(mm);
            mm.virtualize();
         }

         this.head().spawned = true;
      }

   }

   public void unVirtualize() {
      this.unVirtualize(false);
   }

   public void unVirtualize(Boolean useOffset) {
      for(int i = 1; i < this.members.size(); ++i) {
         MinecartMember mm = (MinecartMember)this.members.get(i);
         if (mm.virtualized) {
            mm.load(useOffset);
         }
      }

      this.virtualized = false;
      this.members.forEach((mmx) -> {
         mmx.spawned = true;
      });
   }

   public void checkVirtualization() {
      if (this.head().spawned) {
         Location l = this.head().getEntity().getLocation();
         PlayerController p = TrainCarts.plugin.PlayerController;
         if (p.hasToLoad(l)) {
            if (this.virtualized) {
               if (this.head()._targetSpeed == 0.0D) {
                  this.unVirtualize(true);
                  if (this.currentPlat != null) {
                     this.currentPlat.setLights(Material.VERDANT_FROGLIGHT);
                  }
               } else {
                  this.unVirtualize();
               }
            }
         } else if (!this.virtualized) {
            this.virtualize();
         }

      }
   }
}
