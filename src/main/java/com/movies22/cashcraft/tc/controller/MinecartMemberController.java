package com.movies22.cashcraft.tc.controller;

import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.api.MinecartMember;
import com.movies22.cashcraft.tc.signactions.SignAction;
import com.movies22.cashcraft.tc.signactions.SignActionBlocker;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;
import com.movies22.cashcraft.tc.signactions.SignActionRBlocker;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rail.Shape;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

public class MinecartMemberController extends BaseController {
   private ConcurrentHashMap<UUID, MinecartMember> MinecartMembers = new ConcurrentHashMap();
   private ConcurrentHashMap<UUID, MinecartMember> MinecartHeadMembers = new ConcurrentHashMap();
   private Double speed;
   Boolean b = false;
   // $FF: synthetic field
   private static volatile int[] $SWITCH_TABLE$org$bukkit$block$data$Rail$Shape;

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
         this.MinecartHeadMembers.putIfAbsent(e, m);
      } else {
         this.MinecartMembers.putIfAbsent(e, m);
      }

   }

   public void removeMember(MinecartMember m) {
      if (m.getEntity() != null) {
         if (m.index == 0) {
            this.MinecartHeadMembers.remove(m.getEntity().getUniqueId());
         } else {
            this.MinecartMembers.remove(m.getEntity().getUniqueId());
         }

      }
   }

   public MinecartMember getFromUUID(UUID e) {
      return this.MinecartMembers.get(e) != null ? (MinecartMember)this.MinecartMembers.get(e) : (MinecartMember)this.MinecartHeadMembers.get(e);
   }

   public MinecartMember getFromEntity(Entity e) {
      return this.getFromUUID(e.getUniqueId());
   }

   public Vector rotate(Vector vector, double angle) {
      angle = angle / 180.0D * 3.141592653589793D;
      float x1 = (float)(vector.getX() * Math.cos(angle) - vector.getZ() * Math.sin(angle));
      float z1 = (float)(vector.getX() * Math.sin(angle) + vector.getZ() * Math.cos(angle));
      return new Vector((double)x1, vector.getY(), (double)z1);
   }

   public Boolean validate(MinecartMember m) {
      if (m.virtualized) {
         return true;
      } else {
         Entity e = m.getEntity();
         return !e.isDead() && m != null && m.getGroup() != null && e != null ? true : false;
      }
   }

   public void doFixedTick() {
      this.MinecartHeadMembers.values().forEach((m) -> {
         if (m.spawned) {
            if (this.validate(m)) {
               Minecart e = m.getEntity();
               MinecartGroup g = m.getGroup();
               Location l = e.getLocation();
               Location nextLoc = m.getNextLocation();
               PathNode n = m.getNextNode();
               PathNode n2 = m.getNextNode(1);
               Location nextNode;
               if (n != null) {
                  nextNode = n.loc;
                  if (nextLoc == null) {
                     nextLoc = nextNode;
                  }
               } else {
                  nextNode = l;
                  nextLoc = l;
               }

               Double nd = l.distance(nextNode);
               Double ld = l.distance(nextLoc);
               Double nd2 = 0.0D;
               if (n2 != null) {
                  nd2 = l.distance(n2.loc);
               }

               if (m._targetSpeed >= 0.05D && m.lastAction != null && !m.lastAction.ExitExecuted.contains(g) && m.lastAction != n.getAction()) {
                  m.lastAction.ExitExecuted.add(g);
                  m.lastAction.exit(m.getGroup());
                  m.lastAction.executed.remove(m.getGroup());
                  m.lastAction.node.onBlock = null;
                  m.lastAction = null;
               }

               if (g.currentRoute.stops == null) {
                  return;
               }

               Double z;
               if (g.currentRoute.stops.size() > 0) {
                  PathNode st = ((SignActionPlatform)g.currentRoute.stops.get(0)).node;
                  z = l.distance(st.loc);
                  if (z < 10.0D && st.onBlock != null && !st.onBlock.equals(g) && !st.onBlock.despawned) {
                     g.getMembers().forEach((mm) -> {
                        mm.currentSpeed = Double.MIN_VALUE;
                        mm.setMaxSpeed(0.0D);
                     });
                     g.canProceed = false;
                  } else {
                     if (nd < 20.0D && n.onBlock != null && !n.onBlock.equals(g) && !n.onBlock.despawned && n.onBlock.head().currentSpeed >= 0.05D && n.onBlock.canProceed || n2 != null && nd2 < 10.0D && n2.onBlock != null && !n2.onBlock.equals(g) && !n2.onBlock.despawned && n2.onBlock.head().currentSpeed >= 0.05D && n2.onBlock.canProceed) {
                        g.getMembers().forEach((mm) -> {
                           mm.currentSpeed = Double.MIN_VALUE;
                           mm.setMaxSpeed(0.0D);
                        });
                        g.canProceed = false;
                        return;
                     }

                     if (nd < 20.0D && !g.getHeadcode().startsWith("0") && m._targetSpeed >= 0.0D) {
                        n.onBlock = g;
                        if (n2 != null && nd2 < 10.0D && !g.getHeadcode().startsWith("0") && m._targetSpeed >= 0.0D) {
                           n2.onBlock = g;
                        }

                        g.canProceed = true;
                     } else {
                        g.canProceed = true;
                     }
                  }
               }

               if (nd < 10.0D && n.getAction().getSpeedLimit(g) != null && m._targetSpeed > 0.05D) {
                  this.speed = Math.abs(m._targetSpeed - n.getAction().getSpeedLimit(g)) * ((nd + 2.0D) / 12.0D) + n.getAction().getSpeedLimit(g);
                  m.currentSpeed = this.speed;
               }

               m._mod = 1.0D;
               if (m.currentSpeed < 0.05D) {
                  m.currentSpeed = 0.0D;
               }

               Double x = 0.0D;
               z = 0.0D;
               int i = 0;
               if (nextLoc != null) {
                  x = nextLoc.getX() - l.getX();
                  z = nextLoc.getZ() - l.getZ();
               }

               if (m._targetSpeed > 0.05D) {
                  while(i < 5 && (ld < 2.0D || nd < 1.0D + m._targetSpeed)) {
                     if (nd < 2.0D) {
                        SignAction b = n.getAction();
                        if (!b.executed.contains(g) && !b.getClass().equals(SignActionBlocker.class) && !b.getClass().equals(SignActionRBlocker.class)) {
                           b.ExitExecuted.remove(g);
                           if (b.getSpeedLimit(g) != null) {
                              double s = b.getSpeedLimit(g);
                              g.getMembers().forEach((mm) -> {
                                 mm._targetSpeed = s;
                                 if (!mm.virtualized) {
                                    mm.getEntity().setMaxSpeed(s * mm._mod);
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

                     if (ld >= 2.0D) {
                        break;
                     }

                     m.proceedTo(nextLoc);
                     nextLoc = m.getNextLocation();
                     nextNode = m.getNextNode().loc;
                     if (nextLoc == null) {
                        boolean var19 = true;
                        break;
                     }

                     x = nextLoc.getX() - l.getX();

                     for(z = nextLoc.getZ() - l.getZ(); x == 0.0D && z == 0.0D; ++i) {
                        m.proceedTo(nextLoc);
                        nextLoc = m.getNextLocation();
                        nextNode = m.getNextNode().loc;
                        if (nextLoc == null) {
                           i = 5;
                           break;
                        }

                        x = nextLoc.getX() - l.getX();
                        z = nextLoc.getZ() - l.getZ();
                     }

                     if (n != null) {
                        nextNode = n.loc;
                        if (nextLoc == null) {
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
               if ((double)Math.round(m.currentSpeed * 100.0D) < ts * 100.0D && (double)Math.round(m.currentSpeed * 100.0D) < ts * 100.0D - 5.0D) {
                  m.currentSpeed = m.currentSpeed + 0.05D;
               } else if ((double)Math.round(m.currentSpeed * 100.0D) > ts * 100.0D && (double)Math.round(m.currentSpeed * 100.0D) > ts + 500.0D) {
                  m.currentSpeed = m.currentSpeed - 0.05D;
               }

               this.speed = m.currentSpeed;
               m.setMaxSpeed(m._targetSpeed * m._mod);
               Block rail = e.getLocation().getBlock();
               if (!g.virtualized && l.distance(g.tail().getEntity().getLocation()) > 20.0D) {
                  g.destroy();
                  return;
               }

               if (rail.getType().equals(Material.POWERED_RAIL) || rail.getType().equals(Material.RAIL)) {
                  Rail rail2 = (Rail)rail.getBlockData();
                  if (!rail.getType().equals(Material.RAIL) && !rail2.getShape().name().contains("ASCENDING")) {
                     m.setMaxSpeed(m._targetSpeed * m._mod);
                  } else {
                     m.setMaxSpeed(0.4D);
                     g.lastCurve = rail.getLocation();
                  }

                  switch($SWITCH_TABLE$org$bukkit$block$data$Rail$Shape()[rail2.getShape().ordinal()]) {
                  case 1:
                     x = 0.0D;
                     z = nextLoc.getZ() - l.getZ();
                     if (z > 0.0D) {
                        m.facing = BlockFace.SOUTH;
                     } else {
                        m.facing = BlockFace.NORTH;
                     }
                     break;
                  case 2:
                     x = nextLoc.getX() - l.getX();
                     if (x > 0.0D) {
                        m.facing = BlockFace.EAST;
                     } else {
                        m.facing = BlockFace.WEST;
                     }

                     z = 0.0D;
                     break;
                  case 3:
                     x = nextLoc.getX() - l.getX();
                     z = 0.0D;
                     m.facing = BlockFace.EAST;
                     break;
                  case 4:
                     x = nextLoc.getX() - l.getX();
                     z = 0.0D;
                     m.facing = BlockFace.WEST;
                     break;
                  case 5:
                     x = 0.0D;
                     z = nextLoc.getZ() - l.getZ();
                     m.facing = BlockFace.NORTH;
                     break;
                  case 6:
                     x = 0.0D;
                     z = nextLoc.getZ() - l.getZ();
                     m.facing = BlockFace.SOUTH;
                     break;
                  case 7:
                     x = nextLoc.getX() - l.getX();
                     z = nextLoc.getZ() - l.getZ();
                     if (z > 0.0D) {
                        m.facing = BlockFace.SOUTH;
                     } else {
                        m.facing = BlockFace.EAST;
                     }
                     break;
                  case 8:
                     x = nextLoc.getX() - l.getX();
                     z = nextLoc.getZ() - l.getZ();
                     if (z > 0.0D) {
                        m.facing = BlockFace.SOUTH;
                     } else {
                        m.facing = BlockFace.WEST;
                     }
                     break;
                  case 9:
                     x = nextLoc.getX() - l.getX();
                     z = nextLoc.getZ() - l.getZ();
                     if (z < 0.0D) {
                        m.facing = BlockFace.NORTH;
                     } else {
                        m.facing = BlockFace.WEST;
                     }
                     break;
                  case 10:
                     x = nextLoc.getX() - l.getX();
                     z = nextLoc.getZ() - l.getZ();
                     if (z < 0.0D) {
                        m.facing = BlockFace.NORTH;
                     } else {
                        m.facing = BlockFace.EAST;
                     }
                  }
               }

               Vector vel;
               if (x == 0.0D && z != 0.0D) {
                  vel = new Vector(0.0D, 0.0D, z / Math.abs(z));
               } else if (z == 0.0D && x != 0.0D) {
                  vel = new Vector(x / Math.abs(x), 0.0D, 0.0D);
               } else if (x == 0.0D && z == 0.0D) {
                  vel = new Vector(0, 0, 0);
               } else {
                  vel = new Vector(x / Math.abs(x), 0.0D, z / Math.abs(z));
               }

               this.speed = m.currentSpeed;
               if (e.getPassengers().size() > 0) {
                  vel = vel.multiply(4);
                  vel = vel.divide(new Vector(3, 3, 3));
               }

               e.setVelocity(vel.multiply(this.speed));
            } else {
               m.getGroup().destroy();
            }

         }
      });
      this.MinecartMembers.values().forEach((m) -> {
         if (m.spawned) {
            if (this.validate(m)) {
               Minecart e = m.getEntity();
               MinecartGroup g = m.getGroup();
               if (!g.canProceed) {
                  return;
               }

               Location l = e.getLocation();
               m._targetSpeed = g.head()._targetSpeed;
               m.currentSpeed = g.head().currentSpeed;
               MinecartMember nextCart = m.nextCart();
               if (nextCart == null || nextCart.getEntity() == null) {
                  return;
               }

               m._mod = l.distance(nextCart.getLocation(true)) / 1.2D;
               if (m.currentSpeed < 0.05D) {
                  m.currentSpeed = 0.0D;
               }

               if (m._mod < 0.0D) {
                  m._mod = 0.0D;
               }

               Double x = 0.0D;
               Double z = 0.0D;
               Location nextLoc = m.nextCart().getEntity().getLocation();
               Double ts = g.head()._targetSpeed;
               if ((double)Math.round(m.currentSpeed * 100.0D) < ts * 100.0D) {
                  m.currentSpeed = m.currentSpeed + 0.05D;
               } else if ((double)Math.round(m.currentSpeed * 100.0D) > ts * 100.0D) {
                  m.currentSpeed = m.currentSpeed - 0.05D;
               }

               this.speed = m.currentSpeed;
               m.setMaxSpeed(ts * m._mod);
               Block rail = e.getLocation().subtract(0.0D, 0.0D, 0.0D).getBlock();
               if (rail.getType().equals(Material.POWERED_RAIL) || rail.getType().equals(Material.RAIL)) {
                  Rail rail2 = (Rail)rail.getBlockData();
                  if (rail.getType().equals(Material.RAIL)) {
                     m.setMaxSpeed(0.4D);
                     g.lastCurve = rail.getLocation();
                  } else {
                     m.setMaxSpeed(m._targetSpeed * m._mod);
                  }

                  switch($SWITCH_TABLE$org$bukkit$block$data$Rail$Shape()[rail2.getShape().ordinal()]) {
                  case 1:
                     x = 0.0D;
                     z = nextLoc.getZ() - l.getZ();
                     if (z > 0.0D) {
                        m.facing = BlockFace.SOUTH;
                     } else {
                        m.facing = BlockFace.NORTH;
                     }
                     break;
                  case 2:
                     x = nextLoc.getX() - l.getX();
                     if (x > 0.0D) {
                        m.facing = BlockFace.EAST;
                     } else {
                        m.facing = BlockFace.WEST;
                     }

                     z = 0.0D;
                     break;
                  case 3:
                     x = nextLoc.getX() - l.getX();
                     z = 0.0D;
                     m.facing = BlockFace.EAST;
                     break;
                  case 4:
                     x = nextLoc.getX() - l.getX();
                     z = 0.0D;
                     m.facing = BlockFace.WEST;
                     break;
                  case 5:
                     x = 0.0D;
                     z = nextLoc.getZ() - l.getZ();
                     m.facing = BlockFace.NORTH;
                     break;
                  case 6:
                     x = 0.0D;
                     z = nextLoc.getZ() - l.getZ();
                     m.facing = BlockFace.SOUTH;
                     break;
                  case 7:
                     x = nextLoc.getX() - l.getX();
                     z = nextLoc.getZ() - l.getZ();
                     if (z > 0.0D) {
                        m.facing = BlockFace.SOUTH;
                     } else {
                        m.facing = BlockFace.EAST;
                     }
                     break;
                  case 8:
                     x = nextLoc.getX() - l.getX();
                     z = nextLoc.getZ() - l.getZ();
                     if (z > 0.0D) {
                        m.facing = BlockFace.SOUTH;
                     } else {
                        m.facing = BlockFace.WEST;
                     }
                     break;
                  case 9:
                     x = nextLoc.getX() - l.getX();
                     z = nextLoc.getZ() - l.getZ();
                     if (z < 0.0D) {
                        m.facing = BlockFace.NORTH;
                     } else {
                        m.facing = BlockFace.WEST;
                     }
                     break;
                  case 10:
                     x = nextLoc.getX() - l.getX();
                     z = nextLoc.getZ() - l.getZ();
                     if (z < 0.0D) {
                        m.facing = BlockFace.NORTH;
                     } else {
                        m.facing = BlockFace.EAST;
                     }
                  }
               }

               Vector vel;
               if (x == 0.0D && z != 0.0D) {
                  vel = new Vector(0.0D, 0.0D, z / Math.abs(z));
               } else if (z == 0.0D && x != 0.0D) {
                  vel = new Vector(x / Math.abs(x), 0.0D, 0.0D);
               } else {
                  if (x == 0.0D && z == 0.0D) {
                     new Vector(0, 0, 0);
                     return;
                  }

                  vel = new Vector(x / Math.abs(x), 0.0D, z / Math.abs(z));
               }

               this.speed = g.head().currentSpeed;
               if (this.speed % 0.05D > 0.0D) {
                  this.speed = this.speed - this.speed % 0.05D;
               }

               if (this.speed < 0.05D) {
                  this.speed = 0.0D;
               }

               if (e.getPassengers().size() > 0) {
                  vel = vel.multiply(4);
                  vel = vel.divide(new Vector(3, 3, 3));
               }

               e.setVelocity(vel.multiply(m._mod * this.speed));
            } else {
               m.getGroup().destroy();
            }

         }
      });
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$org$bukkit$block$data$Rail$Shape() {
      int[] var10000 = $SWITCH_TABLE$org$bukkit$block$data$Rail$Shape;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[Shape.values().length];

         try {
            var0[Shape.ASCENDING_EAST.ordinal()] = 3;
         } catch (NoSuchFieldError var10) {
         }

         try {
            var0[Shape.ASCENDING_NORTH.ordinal()] = 5;
         } catch (NoSuchFieldError var9) {
         }

         try {
            var0[Shape.ASCENDING_SOUTH.ordinal()] = 6;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[Shape.ASCENDING_WEST.ordinal()] = 4;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[Shape.EAST_WEST.ordinal()] = 2;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[Shape.NORTH_EAST.ordinal()] = 10;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[Shape.NORTH_SOUTH.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[Shape.NORTH_WEST.ordinal()] = 9;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[Shape.SOUTH_EAST.ordinal()] = 7;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[Shape.SOUTH_WEST.ordinal()] = 8;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$org$bukkit$block$data$Rail$Shape = var0;
         return var0;
      }
   }
}
