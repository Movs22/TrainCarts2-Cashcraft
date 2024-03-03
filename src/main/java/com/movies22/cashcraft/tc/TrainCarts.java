package com.movies22.cashcraft.tc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rail;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.common.Common;
import com.bergerkiller.bukkit.common.PluginBase;
import com.bergerkiller.bukkit.common.Task;
import com.movies22.cashcraft.tc.api.Depot;
import com.movies22.cashcraft.tc.api.MetroLines;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;
import com.movies22.cashcraft.tc.api.MinecartMember;
import com.movies22.cashcraft.tc.api.SpawnerRate;
import com.movies22.cashcraft.tc.api.Station;
import com.movies22.cashcraft.tc.commands.CommandLoader;
import com.movies22.cashcraft.tc.controller.DepotController;
import com.movies22.cashcraft.tc.controller.MinecartMemberController;
import com.movies22.cashcraft.tc.controller.SignStore;
import com.movies22.cashcraft.tc.controller.StationStore;
import com.movies22.cashcraft.tc.controller.PlayerController;
import com.movies22.cashcraft.tc.signactions.SignAction;
import com.movies22.cashcraft.tc.webserver.MainServer;
import com.movies22.cashcraft.tc.webserver.ServerThread;
import com.movies22.cashcraft.tc.offline.*;
import com.movies22.cashcraft.tc.pathFinding.PathNode;
import com.movies22.cashcraft.tc.pathFinding.PathRoute;
import com.movies22.cashcraft.tc.pis.PisController;
import com.movies22.cashcraft.tc.progress.RouteProgress;
import com.movies22.cashcraft.tc.progress.SpeedProgress;

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
	public MetroLine global = null; 
	public MainServer server;
	public ServerThread serverThread;
	public HashMap<World, OfflineWorld> offlineWorlds;

	public HashMap<String, RouteProgress> playerProgress;
	public HashMap<String, SpeedProgress> speedProgress;
	//public HashMap<String, PathFindingProgress> pathFindingProgress;

	public String version = "2.1.1";
	public String mcVersion = "1.20.2";
	public String author = "Movies22";
	@Override
	public int getMinimumLibVersion() {
		return Common.VERSION;
	}

	@Override
	public void onLoad() {
		plugin = this;
	}

	public Task memberMove;
	public Task spawnerTask;
	public Task memberLoad;
	public Task playerUpdateTask;
	public Timer pisUpdateTask;
	public Boolean debug = true;
	@Override
	public void enable() {
		/*if(getServer().getWorld("Main1") != null && debug == true) {
			getLogger().log(Level.SEVERE, "Attempted to load debug version in main server.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}*/	
		this.offlineWorlds = new HashMap<>();
		this.playerProgress = new HashMap<String, RouteProgress>();
		this.speedProgress = new HashMap<String, SpeedProgress>();
		//this.pathFindingProgress = new HashMap<String, PathFindingProgress>();
		World w = getServer().getWorld("Main1");
		offlineWorlds.put(w, new OfflineWorld(w));
		plugin.getLogger().log(Level.INFO, "Enabling TrainCarts...");
		SignAction.init();
		this.lines = new MetroLines();
		MemberController = new MinecartMemberController();
		StationStore = new StationStore();
		DepotStore = new DepotController();
		SignStore = new SignStore();
		PisController = new PisController();
		PlayerController = new PlayerController();
		pisUpdateTask = new Timer();
		Scanner scan;

		String s;
		String[] l;
		File stations = new File(getDataFolder().getPath() + "/stations.tc2");
		try {
			stations.createNewFile();
			scan = new Scanner(stations);
			scan.useDelimiter("@");
			while (scan.hasNext()) {
				s = scan.next();
				l = s.split("/");
				Station ss = StationStore.createStation(l[0], l[1], l[2]);
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
					ss.setOsi("" /*l[5]*/, false);
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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TrainCarts.plugin.getLogger().log(Level.INFO, "Loaded " + this.StationStore.Stations.size() + " stations.");
		
		Location loc;
		File a = new File(getDataFolder().getPath() + "/signs.tc2");
		File path = new File(getDataFolder().getPath() + "/lines/");
		if (global == null) {
			global = lines.createLine("#GLOBAL", "#ffffff");
			plugin.getLogger().log(Level.INFO, "Created #GLOBAL line.");
		}
		File depotsa = new File(getDataFolder().getPath() + "/depots.tc2");
		try {
			depotsa.createNewFile();
			scan = new Scanner(depotsa);
			scan.useDelimiter("@");
			while (scan.hasNext()) {
				s = scan.next();
				l = s.split("/");
				DepotStore.createDepot(l[0], l[1]);
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			a.createNewFile();
			scan = new Scanner(a);
			scan.useDelimiter("@");
			while (scan.hasNext()) {
				s = scan.next();
				l = s.split("/");
				loc = new Location(getServer().getWorld("Main1"), Double.parseDouble(l[1]), Double.parseDouble(l[2]),
						Double.parseDouble(l[3]));
				Block z = loc.getBlock();
				if (z.getState() instanceof Sign) {
					Sign sign = (Sign) z.getState();
					SignStore.addSign(sign);
					if(loc.add(0, 2, 0).getBlock().getBlockData() instanceof Rail) {
						PathNode b = global.createNode(loc);
					if (b == null) {
						loc.getBlock().setType(Material.AIR);
						continue;
					}
					if (b.getAction() == null) {
						global.deleteNode(b);
						loc.getBlock().setType(Material.AIR);
						continue;
					}
					b.findNeighbours();
					}
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File[] lineFiles = path.listFiles();
		if (lineFiles != null && lineFiles.length > 0) {
			for (File lineFile : lineFiles) {
				try {
					scan = new Scanner(lineFile);
					scan.useDelimiter("<");
					String name = null;
					String colour = "#FFFFFF";
					String character = "";
					List<String> routes = new ArrayList<String>();
					int i = 0;
					while (scan.hasNext()) {
						s = scan.next();
						if (i == 0) {
							name = s;
						} else if (i == 1) {
							colour = s;
						} else if (i == 2 && s.length() == 1) {
							character = s;
						} else {
							routes.add(s);
						}
						i++;
					}
					if (name == null) {
						break;
					}
					MetroLine aline = lines.createLine(name, colour);
					aline.setChar(character);
					for (String route : routes) {
						String[] r = route.split(">");
						String routeName = r[0];
						if (r.length < 2) {
							continue;
						}
						String[] r2 = r[1].split("\\|");
						List<PathNode> nodes = new ArrayList<PathNode>();
						for (int z = 0; z < r2.length; z++) {
							String[] l2 = r2[z].split("/");
							if (l2.length < 4) {
								continue;
							}
							PathNode b = global.createNode(new Location(getServer().getWorld("Main1"),
									Double.parseDouble(l2[1]), Double.parseDouble(l2[2]), Double.parseDouble(l2[3])));
							aline.addNode(b);
							b.line = aline;
							nodes.add(b);
						}
						PathRoute routeVar = new PathRoute(routeName, nodes, aline);
						if (r.length > 2) {
							if (r[2] == null || r[2].equals("NULL")) {
								routeVar.reverse = null;
							} else {
								routeVar.reverse = r[2];
							}
						} else {
							routeVar.reverse = null;
						}
						aline.addRoute(routeVar, routeName);
					}
					plugin.getLogger().log(Level.INFO,
							"Loaded " + aline.getRoutes().size() + " routes to the " + aline.getName() + " line.");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		File depots = new File(getDataFolder().getPath() + "/depots.tc2");
		try {
			depots.createNewFile();
			scan = new Scanner(depots);
			scan.useDelimiter("@");
			while (scan.hasNext()) {
				s = scan.next();
				l = s.split("/");
				Depot ds = DepotStore.getFromCode(l[0]);
				int i = 2;
				while (i < l.length) {
					MetroLine line = lines.getLine(l[i]);
					if (line == null) {
						TrainCarts.plugin.getLogger().log(Level.WARNING, "Line " + l[i] + " is INVALID.");
						i += 5;
						continue;
					}
					PathRoute r = line.getRoute(l[i + 1]);
					if (r == null) {
						TrainCarts.plugin.getLogger().log(Level.WARNING,
								"Route " + l[i] + ":" + l[i + 1] + " is INVALID.");
						i += 5;
						continue;
					}
					ds.addRoute(r);
					SpawnerRate sr = new SpawnerRate(Long.parseLong(l[i + 3]), Long.parseLong(l[i + 2]), l[i] + ":" + l[i + 1], l[i + 4].replaceAll("&", "/"));
					ds.addRouteRate(r, sr);
					i += 5;
				}
				;
				TrainCarts.plugin.getLogger().log(Level.INFO, l[1] + " - Loaded " + ds.routes.size() + " routes.");
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		plugin.getLogger().log(Level.INFO, "Loaded " + this.DepotStore.depots.size() + " depots.");
		plugin.getLogger().log(Level.INFO, "Loaded " + lines.getLines().size() + " lines.");
		this.register(listener = new TCListener(this));
		plugin.getLogger().log(Level.INFO, "Registered one listener: " + listener);
		
		pisUpdateTask.schedule( 
		        new java.util.TimerTask() {
		            @Override
		            public void run() {
		            	PisController.doFixedTick();
		            }
		        }, 
		        1000L, 1000L
		);
		
		this.server = new MainServer();
		
		StationStore.postParse();
		
		CommandLoader._init();
		spawnerTask = new Task(plugin) {
			@Override
			public void run() {
				DepotStore.doFixedTick();
				PisController.updateSigns();
			}
		};
		playerUpdateTask = new Task(plugin) {
			@Override
			public void run() {
				PlayerController.doFixedTick();
				MemberController.getHeads().forEach(m -> {
					m.getGroup().checkVirtualization();
				});
				playerProgress.values().forEach(z -> {
					z.tick();
				});
				speedProgress.values().forEach(z -> {
					z.tick();
				});
				/*pathFindingProgress.values().forEach(z -> {
					z.tick();
				});*/
			}
		};
		memberMove = new Task(plugin) {
			@Override
			public void run() {
				MemberController.doFixedTick();
			}
		};
		
		memberLoad = new Task(plugin) {
			@Override
			public void run() {
				MemberController.getHeads().forEach(m -> {
					m.getGroup().keepLoaded(true);
				});
			}
		};
		spawnerTask.start(100L, 20L);
		memberMove.start(100L, 2L);
		memberLoad.start(100L, 100L);
		playerUpdateTask.start(100L, 5L);
		
		serverThread = new ServerThread(this.server);
		serverThread.run();
	}

	private String s = "";
	private String d = "";
	private FileWriter w2;

	@Override
	public void disable() {
		listener = null;
		this.offlineWorlds.clear();
		this.offlineWorlds = null;
		pisUpdateTask.cancel();
		PisController.clear();
		PisController = null;
		playerUpdateTask.stop();
		PlayerController = null;
		memberMove.stop();
		memberMove = null;
		memberLoad.stop();
		memberLoad = null;
		spawnerTask.stop();
		spawnerTask = null;
		serverThread.interrupt();
		s = "";
		List<MinecartMember> z = new ArrayList<MinecartMember>(MemberController.getHeads());
		z.forEach(m -> {
			m.getGroup().destroy();
		});
		SignStore.signs.keySet().forEach(loc -> {
			s = s + "@" + loc.getWorld().getName() + "/" + (loc.getBlockX() + 0.5) + "/" + (loc.getBlockY()) + ".0/"
					+ (loc.getBlockZ() + 0.5);
		});
		File a = new File(getDataFolder().getPath() + "/signs.tc2");
		FileWriter w;
		try {
			w = new FileWriter(a);
			w.write(s);
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		s = "";
		SignStore.signs.clear();
		SignStore = null;
		File linesFolder = new File(getDataFolder().getPath() + "/lines/");
		if (!linesFolder.exists()) {
			Boolean b = linesFolder.mkdirs();
			if (b == true) {
				TrainCarts.plugin.getLogger().log(Level.INFO, "Created /lines/");
			}
		}
		lines.getLines().keySet().forEach(l -> {
			MetroLine line = lines.getLine(l);
			s = "";
			File lineFile = new File(getDataFolder().getPath() + "/lines/" + l + ".tc2");
			// NAME<COLOUR<LETTER<ROUTE_NAME>NODE|NODE|NODE...
			// NODE -> WORLD/X/Y/Z
			s = line.getName() + "<" + line.getColour() + "<" + line.getChar() + "<";
			line.getRoutes().keySet().forEach(name -> {
				PathRoute route = line.getRoute(name);
				s = s + name + ">";
				route.nodes.forEach(node -> {
					Location loc = node.loc;
					s = s + loc.getWorld().getName() + "/" + (loc.getBlockX() + 0.5) + "/" + loc.getBlockY() + ".0/"
							+ (loc.getBlockZ() + 0.5) + "|";
				});
				if (route.reverse == null) {
					s = s + ">NULL<";
				} else {
					s = s + ">" + route.reverse + "<";
				}
			});
			try {
				w2 = new FileWriter(lineFile);
				w2.write(s);
				w2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		lines.clearLines();
		lines = null;
		s = "";
		StationStore.Stations.values().forEach(station -> {
			s = s + "@" + station.code + "/" + station.name + "/" + station.displayName + "/"  + station.headcode + "/"
					+ (station.canTerminate ? "TRUE" : "FALSE") + "/" + station.osi + "/" + station.hosi + "/" + station.station + "/" + (station.closed ? "TRUE" : "FALSE");
		});
		a = new File(getDataFolder().getPath() + "/stations.tc2");
		try {
			a.createNewFile();
			w = new FileWriter(a);
			w.write(s);
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		StationStore.Stations.clear();
		StationStore = null;
		d = "";
		DepotStore.depots.values().forEach(dep -> {
			d = d + "@" + dep.code + "/" + dep.name + "/";
			if (dep.routes != null) {
				dep.routes.values().forEach(r -> {
					SpawnerRate sr = dep.routerate.get(r._line.getName() + ":" + r.name);
					d = d + r._line.getName() + "/" + r.name + "/" + sr.rate + "/" + sr.offset + "/" + sr.length.replaceAll("/", "&") + "/";
				});
			}
		});
		a = new File(getDataFolder().getPath() + "/depots.tc2");
		try {
			a.createNewFile();
			w = new FileWriter(a);
			w.write(d);
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		MemberController = null;
		DepotStore.depots.clear();
		DepotStore.headcodes.clear();
		DepotStore = null;

	}

	@Override
	public boolean command(CommandSender sender, String command, String[] args) {
		return false;
	}

	public static interface Provider {
		/**
		 * Gets the TrainCarts main plugin instance. From here all of TrainCarts' API
		 * can be accessed
		 *
		 * @return TrainCarts plugin instance
		 */
		TrainCarts getTrainCarts();
	}
}

