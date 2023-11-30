package com.movies22.cashcraft.tc;

import com.bergerkiller.bukkit.common.PluginBase;
import com.bergerkiller.bukkit.common.Task;
import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.api.Depot;
import com.movies22.cashcraft.tc.api.MetroLines;
import com.movies22.cashcraft.tc.api.MinecartMember;
import com.movies22.cashcraft.tc.api.SpawnerRate;
import com.movies22.cashcraft.tc.api.Station;
import com.movies22.cashcraft.tc.commands.CommandLoader;
import com.movies22.cashcraft.tc.controller.DepotController;
import com.movies22.cashcraft.tc.controller.MinecartMemberController;
import com.movies22.cashcraft.tc.controller.PisController;
import com.movies22.cashcraft.tc.controller.PlayerController;
import com.movies22.cashcraft.tc.controller.SignStore;
import com.movies22.cashcraft.tc.controller.StationStore;
import com.movies22.cashcraft.tc.signactions.SignAction;
import com.movies22.cashcraft.tc.webserver.MainServer;
import com.movies22.cashcraft.tc.webserver.ServerThread;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rail;
import org.bukkit.command.CommandSender;

public class TrainCarts extends PluginBase {
   public static TrainCarts plugin;
   public MetroLines lines;
   private TCListener listener;
   public MinecartMemberController MemberController;
   public StationStore StationStore;
   public DepotController DepotStore;
   public SignStore SignStore;
   public PisController PisController;
   public PlayerController PlayerController;
   public MetroLines.MetroLine global = null;
   public MainServer server;
   public ServerThread serverThread;
   public String version = "2.0";
   public String mcVersion = "1.20.1";
   public String author = "Movies22";
   public Task memberMove;
   public Task spawnerTask;
   public Task memberLoad;
   public Task playerUpdateTask;
   public Timer pisUpdateTask;
   private String s = "";
   private String d = "";
   private FileWriter w2;

   public int getMinimumLibVersion() {
      return 12001;
   }

   public void onLoad() {
      plugin = this;
   }

   public void enable() {
      plugin.getLogger().log(Level.INFO, "Enabling TrainCarts...");
      SignAction.init();
      this.lines = new MetroLines();
      this.MemberController = new MinecartMemberController();
      this.StationStore = new StationStore();
      this.DepotStore = new DepotController();
      this.SignStore = new SignStore();
      this.PisController = new PisController();
      this.PlayerController = new PlayerController();
      this.pisUpdateTask = new Timer();
      File stations = new File(this.getDataFolder().getPath() + "/stations.tc2");

      Scanner scan;
      String s;
      String[] l;
      try {
         stations.createNewFile();
         scan = new Scanner(stations);
         scan.useDelimiter("@");

         while(scan.hasNext()) {
            s = scan.next();
            l = s.split("/");
            Station ss = this.StationStore.createStation(l[0], l[1], l[2]);
            if (l.length > 3) {
               if (l[3].equals("null")) {
                  ss.headcode = null;
                  ss.canTerminate = false;
               } else {
                  ss.headcode = l[3];
                  ss.canTerminate = true;
               }
            }

            if (l.length > 5) {
               ss.setOsi(l[5], false);
            }

            if (l.length > 6) {
               ss.setOsi(l[6], true);
            }

            if (l.length > 7) {
               ss.station = l[7];
            }

            if (l.length > 8) {
               ss.closed = l[8].equals("TRUE");
            }
         }

         scan.close();
      } catch (FileNotFoundException var36) {
         var36.printStackTrace();
      } catch (IOException var37) {
         var37.printStackTrace();
      }

      plugin.getLogger().log(Level.INFO, "Loaded " + this.StationStore.Stations.size() + " stations.");
      File a = new File(this.getDataFolder().getPath() + "/signs.tc2");
      File path = new File(this.getDataFolder().getPath() + "/lines/");
      if (this.global == null) {
         this.global = this.lines.createLine("#GLOBAL", "#ffffff");
         plugin.getLogger().log(Level.INFO, "Created #GLOBAL line.");
      }

      File depotsa = new File(this.getDataFolder().getPath() + "/depots.tc2");

      try {
         depotsa.createNewFile();
         scan = new Scanner(depotsa);
         scan.useDelimiter("@");

         while(scan.hasNext()) {
            s = scan.next();
            l = s.split("/");
            this.DepotStore.createDepot(l[0], l[1]);
         }

         scan.close();
      } catch (FileNotFoundException var34) {
         var34.printStackTrace();
      } catch (IOException var35) {
         var35.printStackTrace();
      }

      try {
         a.createNewFile();
         scan = new Scanner(a);
         scan.useDelimiter("@");

         while(scan.hasNext()) {
            s = scan.next();
            l = s.split("/");
            Location loc = new Location(this.getServer().getWorld(l[0]), Double.parseDouble(l[1]), Double.parseDouble(l[2]), Double.parseDouble(l[3]));
            Block z = loc.getBlock();
            if (z.getState() instanceof Sign) {
               Sign sign = (Sign)z.getState();
               this.SignStore.addSign(sign);
               if (loc.add(0.0D, 2.0D, 0.0D).getBlock().getBlockData() instanceof Rail) {
                  PathNode b = this.global.createNode(loc);
                  if (b == null) {
                     loc.getBlock().setType(Material.AIR);
                  } else if (b.getAction() == null) {
                     this.global.deleteNode(b);
                     loc.getBlock().setType(Material.AIR);
                  } else {
                     b.findNeighbours();
                  }
               }
            }
         }

         scan.close();
      } catch (FileNotFoundException var32) {
         var32.printStackTrace();
      } catch (IOException var33) {
         var33.printStackTrace();
      }

      File[] lineFiles = path.listFiles();
      int i;
      File lineFile;
      if (lineFiles != null && lineFiles.length > 0) {
         File[] var13 = lineFiles;
         i = lineFiles.length;

         label188:
         for(int var41 = 0; var41 < i; ++var41) {
            lineFile = var13[var41];

            try {
               scan = new Scanner(lineFile);
               scan.useDelimiter("<");
               String name = null;
               String colour = "#FFFFFF";
               String character = "";
               List<String> routes = new ArrayList();

               for(int z = 0; scan.hasNext(); z++) {
                  s = scan.next();
                  if (z == 0) {
                     name = s;
                  } else if (z == 1) {
                     colour = s;
                  } else if (z == 2 && s.length() == 1) {
                     character = s;
                  } else {
                     routes.add(s);
                  }
               }

               if (name == null) {
                  break;
               }

               MetroLines.MetroLine aline = this.lines.createLine(name, colour);
               Iterator var21 = routes.iterator();

               while(true) {
                  String[] r;
                  String routeName;
                  do {
                     if (!var21.hasNext()) {
                        aline.setChar(character);
                        plugin.getLogger().log(Level.INFO, "Loaded " + aline.getRoutes().size() + " routes to the " + aline.getName() + " line.");
                        continue label188;
                     }

                     String route = (String)var21.next();
                     r = route.split(">");
                     routeName = r[0];
                  } while(r.length < 2);

                  String[] r2 = r[1].split("\\|");
                  List<PathNode> nodes = new ArrayList();

                  for(int z = 0; z < r2.length; ++z) {
                     String[] l2 = r2[z].split("/");
                     if (l2.length >= 4) {
                        PathNode b = this.global.createNode(new Location(this.getServer().getWorld(l2[0]), Double.parseDouble(l2[1]), Double.parseDouble(l2[2]), Double.parseDouble(l2[3])));
                        aline.addNode(b);
                        b.line = aline;
                        nodes.add(b);
                     }
                  }

                  PathRoute routeVar = new PathRoute(routeName, nodes, aline);
                  if (r.length > 2) {
                     if (r[2] != null && !r[2].equals("NULL")) {
                        routeVar.reverse = r[2];
                     } else {
                        routeVar.reverse = null;
                     }
                  } else {
                     routeVar.reverse = null;
                  }

                  aline.addRoute(routeVar, routeName);
               }
            } catch (FileNotFoundException var31) {
               var31.printStackTrace();
            }
         }
      }

      lineFile = new File(this.getDataFolder().getPath() + "/depots.tc2");

      try {
         lineFile.createNewFile();
         scan = new Scanner(lineFile);
         scan.useDelimiter("@");

         while(scan.hasNext()) {
            s = scan.next();
            l = s.split("/");
            Depot ds = this.DepotStore.getFromCode(l[0]);
            i = 2;

            while(i < l.length) {
               MetroLines.MetroLine line = this.lines.getLine(l[i]);
               if (line == null) {
                  plugin.getLogger().log(Level.WARNING, "Line " + l[i] + " is INVALID.");
                  i += 5;
               } else {
                  PathRoute r = line.getRoute(l[i + 1]);
                  if (r == null) {
                     plugin.getLogger().log(Level.WARNING, "Route " + l[i] + ":" + l[i + 1] + " is INVALID.");
                     i += 5;
                  } else {
                     ds.addRoute(r);
                     SpawnerRate sr = new SpawnerRate(Long.parseLong(l[i + 3]), Long.parseLong(l[i + 2]), l[i] + ":" + l[i + 1], l[i + 4].replaceAll("&", "/"));
                     ds.addRouteRate(r, sr);
                     i += 5;
                  }
               }
            }

            plugin.getLogger().log(Level.INFO, l[1] + " - Loaded " + ds.routes.size() + " routes.");
         }

         scan.close();
      } catch (FileNotFoundException var29) {
         var29.printStackTrace();
      } catch (IOException var30) {
         var30.printStackTrace();
      }

      plugin.getLogger().log(Level.INFO, "Loaded " + this.DepotStore.depots.size() + " depots.");
      plugin.getLogger().log(Level.INFO, "Loaded " + this.lines.getLines().size() + " lines.");
      this.register(this.listener = new TCListener(this));
      plugin.getLogger().log(Level.INFO, "Registered one listener: " + this.listener);
      this.pisUpdateTask.schedule(new TimerTask() {
         public void run() {
            TrainCarts.this.PisController.doFixedTick();
         }
      }, 1000L, 2000L);
      this.server = new MainServer();
      this.StationStore.postParse();
      CommandLoader._init();
      this.spawnerTask = new Task(plugin) {
         public void run() {
            TrainCarts.this.DepotStore.doFixedTick();
            TrainCarts.this.PisController.updateSigns();
         }
      };
      this.playerUpdateTask = new Task(plugin) {
         public void run() {
            TrainCarts.this.PlayerController.doFixedTick();
            TrainCarts.this.MemberController.getHeads().forEach((m) -> {
               m.getGroup().checkVirtualization();
            });
         }
      };
      this.memberMove = new Task(plugin) {
         public void run() {
            TrainCarts.this.MemberController.doFixedTick();
         }
      };
      this.memberLoad = new Task(plugin) {
         public void run() {
            TrainCarts.this.MemberController.getHeads().forEach((m) -> {
               m.getGroup().keepLoaded(true);
            });
         }
      };
      this.spawnerTask.start(100L, 20L);
      this.memberMove.start(100L, 2L);
      this.memberLoad.start(100L, 100L);
      this.playerUpdateTask.start(100L, 20L);
      this.serverThread = new ServerThread(this.server);
      this.serverThread.run();
   }

   public void disable() {
      this.listener = null;
      this.pisUpdateTask.cancel();
      this.PisController.clear();
      this.PisController = null;
      this.playerUpdateTask.stop();
      this.PlayerController = null;
      this.memberMove.stop();
      this.memberMove = null;
      this.memberLoad.stop();
      this.memberLoad = null;
      this.spawnerTask.stop();
      this.spawnerTask = null;
      this.serverThread.interrupt();
      this.s = "";
      List<MinecartMember> z = new ArrayList(this.MemberController.getHeads());
      z.forEach((m) -> {
         m.getGroup().destroy();
      });
      this.SignStore.signs.keySet().forEach((loc) -> {
         this.s = this.s + "@" + loc.getWorld().getName() + "/" + ((double)loc.getBlockX() + 0.5D) + "/" + loc.getBlockY() + ".0/" + ((double)loc.getBlockZ() + 0.5D);
      });
      File a = new File(this.getDataFolder().getPath() + "/signs.tc2");

      FileWriter w;
      try {
         w = new FileWriter(a);
         w.write(this.s);
         w.close();
      } catch (IOException var8) {
         var8.printStackTrace();
      }

      this.s = "";
      this.SignStore.signs.clear();
      this.SignStore = null;
      File linesFolder = new File(this.getDataFolder().getPath() + "/lines/");
      if (!linesFolder.exists()) {
         Boolean b = linesFolder.mkdirs();
         if (b) {
            plugin.getLogger().log(Level.INFO, "Created /lines/");
         }
      }

      this.lines.getLines().keySet().forEach((l) -> {
         MetroLines.MetroLine line = this.lines.getLine(l);
         this.s = "";
         File lineFile = new File(this.getDataFolder().getPath() + "/lines/" + l + ".tc2");
         this.s = line.getName() + "<" + line.getColour() + "<" + line.getChar() + "<";
         line.getRoutes().keySet().forEach((name) -> {
            PathRoute route = line.getRoute(name);
            this.s = this.s + name + ">";
            route.nodes.forEach((node) -> {
               Location loc = node.loc;
               this.s = this.s + loc.getWorld().getName() + "/" + ((double)loc.getBlockX() + 0.5D) + "/" + loc.getBlockY() + ".0/" + ((double)loc.getBlockZ() + 0.5D) + "|";
            });
            if (route.reverse == null) {
               this.s = this.s + ">NULL<";
            } else {
               this.s = this.s + ">" + route.reverse + "<";
            }

         });

         try {
            this.w2 = new FileWriter(lineFile);
            this.w2.write(this.s);
            this.w2.close();
         } catch (IOException var5) {
            var5.printStackTrace();
         }

      });
      this.lines.clearLines();
      this.lines = null;
      this.s = "";
      this.StationStore.Stations.values().forEach((station) -> {
         this.s = this.s + "@" + station.code + "/" + station.name + "/" + station.displayName + "/" + station.headcode + "/" + (station.canTerminate ? "TRUE" : "FALSE") + "/" + station.osi + "/" + station.hosi + "/" + station.station + "/" + (station.closed ? "TRUE" : "FALSE");
      });
      a = new File(this.getDataFolder().getPath() + "/stations.tc2");

      try {
         a.createNewFile();
         w = new FileWriter(a);
         w.write(this.s);
         w.close();
      } catch (IOException var7) {
         var7.printStackTrace();
      }

      this.StationStore.Stations.clear();
      this.StationStore = null;
      this.d = "";
      this.DepotStore.depots.values().forEach((dep) -> {
         this.d = this.d + "@" + dep.code + "/" + dep.name + "/";
         if (dep.routes != null) {
            dep.routes.values().forEach((r) -> {
               SpawnerRate sr = (SpawnerRate)dep.routerate.get(r._line.getName() + ":" + r.name);
               this.d = this.d + r._line.getName() + "/" + r.name + "/" + sr.rate + "/" + sr.offset + "/" + sr.length.replaceAll("/", "&") + "/";
            });
         }

      });
      a = new File(this.getDataFolder().getPath() + "/depots.tc2");

      try {
         a.createNewFile();
         w = new FileWriter(a);
         w.write(this.d);
         w.close();
      } catch (IOException var6) {
         var6.printStackTrace();
      }

      this.MemberController = null;
      this.DepotStore.depots.clear();
      this.DepotStore.headcodes.clear();
      this.DepotStore = null;
   }

   public boolean command(CommandSender sender, String command, String[] args) {
      return false;
   }

   public interface Provider {
      TrainCarts getTrainCarts();
   }
}
