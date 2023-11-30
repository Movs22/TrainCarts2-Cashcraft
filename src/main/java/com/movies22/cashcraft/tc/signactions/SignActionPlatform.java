package com.movies22.cashcraft.tc.signactions;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.api.Station;
import com.movies22.cashcraft.tc.controller.PisController;
import com.movies22.cashcraft.tc.utils.Guides;
import com.movies22.cashcraft.tc.utils.StationAnnouncements;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.block.data.type.Jigsaw.Orientation;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SignActionPlatform extends SignAction {
   public String platform = null;
   public String name;
   public Station station;
   public long duration;
   public Vector offset;
   public int doors;
   public String n;
   public List<Location> doorLocs = new ArrayList();
   public List<Location> lightLocs = new ArrayList();
   public boolean inverted = false;
   private Character headcode;
   public boolean reverse = false;
   public HashMap<Character, PisController.PIS> pis = new HashMap();
   SignActionPlatform b;
   public Timer groupAnnounceTask;
   int stops = 0;


   public SignActionPlatform clone() {
      SignActionPlatform a = new SignActionPlatform();
      a.platform = null;
      a.name = "";
      a.station = null;
      a.duration = 0L;
      a.offset = null;
      a.doors = 0;
      a.n = "";
      a.doorLocs = new ArrayList();
      a.lightLocs = new ArrayList();
      a.inverted = false;
      a.headcode = null;
      a.reverse = false;
      a.pis = new HashMap();
      return a;
   }

   public void setLights(Material light) {
      this.lightLocs.forEach((loc) -> {
         loc.getBlock().setType(light);
      });
      if (light.equals(Material.VERDANT_FROGLIGHT)) {
         this.doorLocs.forEach((loc) -> {
            loc.clone().subtract(0.0D, 1.0D, 0.0D).getBlock().setType(Material.REDSTONE_TORCH);
         });
      } else if (light.equals(Material.PEARLESCENT_FROGLIGHT)) {
         this.doorLocs.forEach((loc) -> {
            loc.clone().subtract(0.0D, 1.0D, 0.0D).getBlock().setType(Material.AIR);
         });
      }

   }

   public Boolean execute(final MinecartGroup group) {
      this.b = null;
      group.currentPlat = this;
      if (this.station.closed) {
         group.currentRoute.stops.remove(0);
         return true;
      } else if (group.currentRoute.stops.size() > 0 && ((SignActionPlatform)group.currentRoute.stops.get(0)).equals(this)) {
         this.stops = group.currentRoute.stops.size();
         group.head().facing = this.node.direction;
         if (group.currentRoute.stops.size() <= 1) {
            if (this.reverse) {
               group.head().facing = this.node.direction.getOppositeFace();
               group.unVirtualize(true);
               group.loadNextRoute(false, true);
               group.reverse();
               group.getMembers().forEach((m) -> {
                  m.proceedTo(this.node.getLocation());
               });
            } else {
               group.loadNextRoute(true);
            }
         }

         this.headcode = group.getHeadcode().charAt(1);
         group.getMembers().forEach((m) -> {
            m.currentSpeed = 0.0D;
            m._targetSpeed = 0.0D;
         });
         PisController.PIS pis;
         if (this.pis.get(this.headcode) != null) {
            pis = (PisController.PIS)this.pis.get(this.headcode);
         } else {
            pis = TrainCarts.plugin.PisController.getPis(this.station.code + this.platform + this.headcode);
            this.pis.put(this.headcode, pis);
         }

         long dur = this.duration;
         pis.setArrived(true);
         TrainCarts.plugin.PisController.getPis(this.station.code + this.platform + this.headcode).addTimer(group.nextTrain);
         pis.delay = 0;
         pis = null;
         this.n = group.currentRoute.name;
         if (!group.virtualized) {
            this.setLights(Material.VERDANT_FROGLIGHT);
         }

         if (!group.virtualized) {
            final List<String> ann = new ArrayList();
            String c = group.getLine().getChar();
            ann.add("This station is " + this.station.name + ".");
            if (this.station.osi != c && this.station.osi != "" && this.station.osi.length() > 1) {
               ann.add(StationAnnouncements.parseMetro(this.station.osi, group.getLine()));
            }

            if (this.station.hosi != null && !this.station.hosi.equals("")) {
               ann.add(StationAnnouncements.parseRail(this.station.hosi, group.getLine(), ann.size() > 2));
            }

            if (this.station.station != "") {
               String s = this.station.generateConnection(group.getLine());
               ann.add(s);
            }

            if (this.stops == 1) {
               ann.add("This train terminates here. All change please.");
            }

            group.announce((String)ann.get(0), false, ((String)ann.get(0)).contains("{\"text"));
            ann.remove(0);
            this.groupAnnounceTask = new Timer();
            this.groupAnnounceTask.schedule(new TimerTask() {
               public void run() {
                  if (ann.size() > 0) {
                     if (ann.get(0) != null) {
                        group.announce((String)ann.get(0), false, ((String)ann.get(0)).contains("{\"text"));
                     }

                     ann.remove(0);
                  } else {
                     this.cancel();
                     SignActionPlatform.this.groupAnnounceTask = null;
                  }

               }
            }, 2500L, 2500L);
         }

         (new Timer()).schedule(new TimerTask() {
            public void run() {
               SignActionPlatform.this.depart(group);
               group.getMembers().forEach((m) -> {
                  m._targetSpeed = 0.6D;
                  m._mod = 1.0D;
               });
            }
         }, dur * 1000L);
         return true;
      } else {
         group.head().proceedTo(this.node.loc);
         return true;
      }
   }

   public void depart(MinecartGroup g) {
      if (g.currentRoute.stops != null && g.currentRoute != null) {
         if (g.currentRoute.stops.size() > 0 && ((SignActionPlatform)g.currentRoute.stops.get(0)).equals(this)) {
            g.currentRoute.stops.remove(0);
         }

         if (TrainCarts.plugin.PisController != null) {
            TrainCarts.plugin.PisController.getPis(this.station.code + this.platform + this.headcode).setArrived(false);
         }

      }
   }

   public Boolean exit(final MinecartGroup group) {
      if (!group.virtualized) {
         this.setLights(Material.PEARLESCENT_FROGLIGHT);
      }

      if (group.currentRoute.name.equals("DESPAWN")) {
         group.destroy();
      }

      if (!group.currentRoute._line.getName().equals("#GLOBAL") && group.currentRoute.stops.size() > 0) {
         group.announce("This is a " + group.currentRoute._line.getName() + " Line service to " + ((SignActionPlatform)group.currentRoute.stops.get(group.currentRoute.stops.size() - 1)).station.name + ".");
      } else {
         group.eject();
         group.destroy();
      }

      if (group.currentRoute == null) {
         return false;
      } else {
         if (!group.currentRoute.name.equals("[CACHED ROUTE]")) {
            TimerTask t = new TimerTask() {
               public void run() {
                  if (group.currentRoute.stops != null && group.currentRoute.stops.size() > 0) {
                     if (((SignActionPlatform)group.currentRoute.stops.get(0)).station.closed) {
                        group.announce("The next station is closed.");
                     } else {
                        group.announce("The next station is " + ((SignActionPlatform)group.currentRoute.stops.get(0)).station.name + ".");
                     }
                  }

               }
            };
            (new Timer()).schedule(t, 3000L);
         }

         group.currentPlat = null;
         return true;
      }
   }

   public void postParse() {
      try {
         String[] a = this.content.split(" ");
         this.platform = a[1];
         Station b = TrainCarts.plugin.StationStore.getFromCode(a[2]);
         if (b == null) {
            TrainCarts.plugin.getLogger().log(Level.WARNING, this.content + " is an invalid SignActionPlatform sign.");
            this.platform = null;
            this.node.line.deleteNode(this.node);
            if (this.node.line != TrainCarts.plugin.global) {
               TrainCarts.plugin.global.deleteNode(this.node);
            }

         } else {
            this.station = b;
            b.addPlatform(a[1], this);

            try {
               this.duration = Long.parseLong(a[3]);
            } catch (NumberFormatException var13) {
               TrainCarts.plugin.getLogger().log(Level.WARNING, this.content + " is an invalid SignActionPlatform sign.");
               this.platform = null;
               this.node.line.deleteNode(this.node);
               if (this.node.line != TrainCarts.plugin.global) {
                  TrainCarts.plugin.global.deleteNode(this.node);
               }

               return;
            }

            String[] c = a[4].split("/");
            if (a.length > 6 && a[6].equals("R")) {
               this.reverse = true;
            }

            this.offset = new Vector(Integer.valueOf(c[0]), Integer.valueOf(c[1]), Integer.valueOf(c[2]));
            Vector offset;
            Vector addition;
            switch(this.node.direction) {
            case NORTH:
               offset = new Vector(-2, 1, 0);
               addition = new Vector(0, 0, -3);
               break;
            case EAST:
               offset = new Vector(0, 1, -2);
               addition = new Vector(3, 0, 0);
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
            }

            this.doors = Integer.valueOf(a[5]);
            Location z = this.sign.getBlock().getLocation().clone();
            Vector addition2 = addition.clone().divide(new Vector(3, 3, 3));
            Location light = z.subtract(offset).add(0.0D, 2.0D, 0.0D);
            int i;
            Jigsaw z2;
            Location z3;
            Jigsaw z4;
            if (!light.getBlock().getType().equals(Material.JIGSAW) && !light.getBlock().getType().equals(Material.PUMPKIN) && !light.getBlock().getType().equals(Material.VERDANT_FROGLIGHT) && !light.getBlock().getType().equals(Material.PEARLESCENT_FROGLIGHT)) {
               light.add(offset).add(offset).subtract(0.0D, 2.0D, 0.0D);
               if (light.getBlock().getType().equals(Material.JIGSAW) || light.getBlock().getType().equals(Material.PUMPKIN) || light.getBlock().getType().equals(Material.VERDANT_FROGLIGHT) || light.getBlock().getType().equals(Material.PEARLESCENT_FROGLIGHT)) {
                  this.doorLocs = new ArrayList();
                  this.lightLocs = new ArrayList();
                  this.lightLocs.add(light.clone().subtract(addition2));

                  for(i = 0; i < this.doors && (light.getBlock().getType().equals(Material.JIGSAW) || light.getBlock().getType().equals(Material.PUMPKIN) || light.getBlock().getType().equals(Material.VERDANT_FROGLIGHT) || light.getBlock().getType().equals(Material.PEARLESCENT_FROGLIGHT)); ++i) {
                     this.doorLocs.add(light.clone());
                     light.clone().subtract(0.0D, 1.0D, 0.0D).getBlock().setType(Material.AIR);
                     light.getBlock().setType(Material.JIGSAW);
                     z2 = (Jigsaw)light.getBlock().getBlockData();
                     z2.setOrientation(Orientation.valueOf(this.node.direction.name() + "_UP"));
                     light.getBlock().setBlockData(z2);
                     z3 = light.clone().add(addition2);
                     z3.getBlock().setType(Material.JIGSAW);
                     z4 = (Jigsaw)z3.getBlock().getBlockData();
                     z4.setOrientation(Orientation.valueOf(this.node.direction.getOppositeFace().name() + "_UP"));
                     z3.getBlock().setBlockData(z4);
                     light.add(addition);
                     light.subtract(addition2);
                     this.lightLocs.add(light.clone());
                     light.add(addition2);
                  }

                  this.lightLocs.forEach((loc) -> {
                     loc.getBlock().setType(Material.PEARLESCENT_FROGLIGHT);
                  });
               }
            } else {
               this.doorLocs = new ArrayList();
               this.lightLocs = new ArrayList();
               this.lightLocs.add(light.clone().subtract(addition2));

               for(i = 0; i < this.doors && (light.getBlock().getType().equals(Material.JIGSAW) || light.getBlock().getType().equals(Material.PUMPKIN) || light.getBlock().getType().equals(Material.VERDANT_FROGLIGHT) || light.getBlock().getType().equals(Material.PEARLESCENT_FROGLIGHT)); ++i) {
                  this.doorLocs.add(light.clone());
                  light.clone().subtract(0.0D, 1.0D, 0.0D).getBlock().setType(Material.AIR);
                  light.getBlock().setType(Material.JIGSAW);
                  z2 = (Jigsaw)light.getBlock().getBlockData();
                  z2.setOrientation(Orientation.valueOf(this.node.direction.name() + "_UP"));
                  light.getBlock().setBlockData(z2);
                  z3 = light.clone().add(addition2);
                  z3.getBlock().setType(Material.JIGSAW);
                  z4 = (Jigsaw)z3.getBlock().getBlockData();
                  z4.setOrientation(Orientation.valueOf(this.node.direction.getOppositeFace().name() + "_UP"));
                  z3.getBlock().setBlockData(z4);
                  light.add(addition);
                  light.subtract(addition2);
                  this.lightLocs.add(light.clone());
                  light.add(addition2);
               }

               this.lightLocs.forEach((loc) -> {
                  loc.getBlock().setType(Material.PEARLESCENT_FROGLIGHT);
               });
            }
         }
      } catch (IndexOutOfBoundsException var14) {
         TrainCarts.plugin.getLogger().log(Level.WARNING, this.content + " is an invalid SignActionPlatform sign.");
         this.platform = null;
         this.node.line.deleteNode(this.node);
         if (this.node.line != TrainCarts.plugin.global) {
            TrainCarts.plugin.global.deleteNode(this.node);
         }

      }
   }

   public Boolean match(String s) {
      return s.toLowerCase().equals("t:plat");
   }

   public String getAction() {
      return "SignActionPlatform";
   }

   public Double getSpeedLimit(MinecartGroup g) {
      return !this.station.closed ? 0.0D : null;
   }

   public void handleBuild(Player p) {
      TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
      TextComponent clickable = new TextComponent("" + ChatColor.BLUE + ChatColor.UNDERLINE + "PLATFORM");
      TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
      TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to make a train stop at a station.");
      clickable.setClickEvent(new ClickEvent(Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.PLATFORM_SIGN.id));
      p.spigot().sendMessage(new BaseComponent[]{m1, clickable, m2, m3});
   }
}
