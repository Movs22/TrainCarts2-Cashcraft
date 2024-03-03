package com.movies22.cashcraft.tc.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;
import com.movies22.cashcraft.tc.controller.StationStore;
import com.movies22.cashcraft.tc.pathFinding.PathNode;
import com.movies22.cashcraft.tc.pathFinding.PathRoute;
import com.movies22.cashcraft.tc.progress.RouteProgress;
import com.movies22.cashcraft.tc.progress.SpeedProgress;
import com.movies22.cashcraft.tc.api.Station;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class RouteCommand implements CommandExecutor {
	public RouteCommand(TrainCarts tc) {
		tc.getCommand("route").setExecutor(this);
		tc.getCommand("route").setTabCompleter(new TabComplete());
	}

	static <T> T[] append(T[] arr, T lastElement) {
		final int N = arr.length;
		arr = java.util.Arrays.copyOf(arr, N + 1);
		arr[N] = lastElement;
		return arr;
	}
	
	private TextComponent[] msg = {};
	private int i;
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender.isOp()) {
			if(args[0].equals("reroute")) {
				if(args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route reroute [LINE] [ROUTE]");
					return true;
				}
				MetroLine l = TrainCarts.plugin.lines.getLine(args[1]);
				if(l == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1] + ChatColor.RED + ".");
					return true;
				}
				PathRoute r = l.getRoute(args[2]);
				if(r == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[2] + ChatColor.RED + " in the " + args[1] + " line.");
					return true;
				}
				r.calculate();
				sender.sendMessage(ChatColor.GREEN + "Rerouted " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + ".");
				return true;
			} else if (args[0].equals("info")) {
				if(args.length < 2) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route info [ROUTE]");
					return true;
				}
				MetroLine l = TrainCarts.plugin.lines.getLine(args[1].split(":")[0]);
				if(l == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1].split(":")[0] + ChatColor.RED + ".");
					return true;
				}
				PathRoute r = l.getRoute(args[1].split(":")[1]);
				if(r == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[1].split(":")[1] + ChatColor.RED + " in the " + args[1] + " line.");
					return true;
				}
				PathRoute r2 = null;
				if(r.reverse != null && !r.reverse.equals("ECS")) {
					r2 = TrainCarts.plugin.lines.getLine(r.reverse.split(":")[0]).getRoute(r.reverse.split(":")[1]);
				}
				msg = java.util.Arrays.copyOf(msg, 0);
				TextComponent header = new TextComponent(ChatColor.WHITE + "" + ChatColor.BOLD + "===== Information of " + r.name + " =====\n");
				msg = append(msg, header);
				TextComponent m1 = new TextComponent(ChatColor.YELLOW + " - Name: " + ChatColor.GREEN + r.name);
				msg = append(msg, m1);
				TextComponent m2 = new TextComponent(ChatColor.YELLOW + "\n - Line: " + ChatColor.GREEN + r._line.getName());
				msg = append(msg, m2);
				TextComponent m3 = new TextComponent(ChatColor.YELLOW + "\n - Headcode: " + ChatColor.GREEN + "2" + r.stops.get(r.stops.size() - 1).station.headcode + "XX");
				msg = append(msg, m3);
				TextComponent m3a = new TextComponent(ChatColor.YELLOW + "\n - Reverse: " + (r.reverse == null ? ChatColor.DARK_RED + "NO" : (r.reverse.equals("ECS") ? ChatColor.WHITE + "ECS service" : ChatColor.GREEN + "YES" + ChatColor.YELLOW + " (" + ChatColor.GREEN + r2.stops.get(0).station.code + ChatColor.YELLOW + "~" + ChatColor.GREEN + r2.stops.get(0).platform + ChatColor.YELLOW + " > " + ChatColor.GREEN + r2.stops.get(r2.stops.size() - 1).station.code + ChatColor.YELLOW + "~" + ChatColor.GREEN + r2.stops.get(r2.stops.size() - 1).platform + ChatColor.YELLOW + " as " + ChatColor.GREEN + "2" + r2.stops.get(r2.stops.size() - 1).station.headcode + "XX" + ChatColor.YELLOW + ")")));
				msg = append(msg, m3a);
				TextComponent m3b = new TextComponent(ChatColor.YELLOW + "\n - Stations (" + r.stops.size() + "): ");
				msg = append(msg, m3b);
				TextComponent m4 = new TextComponent(ChatColor.YELLOW + ", ");
				TextComponent m5 = new TextComponent(ChatColor.YELLOW + " and ");
				i = 0;
				r.stops.forEach(s -> {
					if (i > 0 && i < (r.stops.size() - 1)) {
						msg = append(msg, m4);
					}
					i++;
					TextComponent clickable;
					if(s == null || s.station == null || s.station.name == null) {
						clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + "ERROR" + "~" + s.platform);
					} else {
						clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + s.station.name + "~" + s.platform);
					}
					TextComponent hover[] = { new TextComponent("Code: " + s.station.code) };
					clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
					clickable.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/station info " + s.station.code));
					msg = append(msg, clickable);
					if (i == (r.stops.size() - 1)) {
						msg = append(msg, m5);
					};
				});
				Location l1 = r.route.get(r.route.size() - 1).locs.get(r.route.get(r.route.size() - 1).locs.size() - 1);
				TextComponent m3c = new TextComponent(ChatColor.YELLOW + "\n - Last location: " + l1.getBlockX() + "/" + l1.getBlockY() + "/" + l1.getBlockZ());
				msg = append(msg, m3c);
				TextComponent footer = new TextComponent(ChatColor.WHITE + "\n" + ChatColor.BOLD + "=====");
				msg = append(msg, footer);
				sender.spigot().sendMessage(msg);
				return true;
			} else if (args[0].equals("speed")) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "You can only run this command as a player!");
					return true;
				}
				if(args.length < 2) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route speed [ROUTE]");
					return true;
				}
				MetroLine l = TrainCarts.plugin.lines.getLine(args[1].split(":")[0]);
				if(l == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1].split(":")[0] + ChatColor.RED + ".");
					return true;
				}
				PathRoute r = l.getRoute(args[1].split(":")[1]);
				if(r == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[1].split(":")[1] + ChatColor.RED + " in the " + args[1] + " line.");
					return true;
				}
				SpeedProgress rp = new SpeedProgress(((Player) sender), l);
				TrainCarts.plugin.speedProgress.put(sender.getName(), rp);
				sender.sendMessage(ChatColor.GREEN + "Route §e" + r.name + " §ahas been loaded.");
				return true;
			} /*else if (args[0].equals("pathfind")) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "You can only run this command as a player!");
					return true;
				}
				if(args.length < 2) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route pathfind [ROUTE]");
					return true;
				}
				MetroLine l = TrainCarts.plugin.lines.getLine(args[1].split(":")[0]);
				if(l == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1].split(":")[0] + ChatColor.RED + ".");
					return true;
				}
				PathRoute r = l.getRoute(args[1].split(":")[1]);
				if(r == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[1].split(":")[1] + ChatColor.RED + " in the " + args[1] + " line.");
					return true;
				}
				PathFindingProgress rp = new PathFindingProgress(((Player) sender), r);
				TrainCarts.plugin.pathFindingProgress.put(sender.getName(), rp);
				sender.sendMessage(ChatColor.GREEN + "Route §e" + r.name + " §ahas been loaded.");
				return true;
			} */else if (args[0].equals("list")) {
				if(args.length < 2) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route list [LINE]");
					return true;
				}
				MetroLine l = TrainCarts.plugin.lines.getLine(args[1]);
				if(l == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1] + ChatColor.RED + ".");
					return true;
				}
				msg = java.util.Arrays.copyOf(msg, 0);
				TextComponent m1 = new TextComponent(ChatColor.YELLOW + "There's a total of ");
				msg = append(msg, m1);
				TextComponent m2 = new TextComponent(ChatColor.GREEN + "" + l.getRoutes().size());
				msg = append(msg, m2);
				TextComponent m3 = new TextComponent(ChatColor.YELLOW + " routes in ");
				msg = append(msg, m3);
				TextComponent m3a = new TextComponent(ChatColor.GREEN + "" + l.getName());
				msg = append(msg, m3a);
				TextComponent m3b = new TextComponent(ChatColor.YELLOW + ": ");
				msg = append(msg, m3b);
				TextComponent m4 = new TextComponent(ChatColor.YELLOW + ", ");
				TextComponent m5 = new TextComponent(ChatColor.YELLOW + " and ");
				i = 0;
				l.getRoutes().values().forEach(route -> {
					if (i > 0 && i < (l.getRoutes().size() - 1)) {
						msg = append(msg, m4);
					}
					i++;
					TextComponent clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + route.name);
					TextComponent hover[] = { new TextComponent("Connections: " + route.route.size()) };
					clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
					msg = append(msg, clickable);
					if (i == (l.getRoutes().size() - 1)) {
						msg = append(msg, m5);
					};
				});
				sender.spigot().sendMessage(msg);
				return true;
			} else if(args[0].equals("create")) {
				if(args.length < 5) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route create [LINE] [ROUTE] [Start~X] [Station1~X] (...) [End~X]");
					return true;
				}
				MetroLine l = TrainCarts.plugin.lines.getLine(args[1]);
				if(l == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1] + ChatColor.RED + ".");
					return true;
				}
				PathRoute r = l.getRoute(args[2]);
				if(r != null) {
					sender.sendMessage(ChatColor.RED + "There's already a route named " + ChatColor.YELLOW + args[2] + ChatColor.RED + " in the " + args[1] + " line.");
					return true;
				}
				StationStore s = TrainCarts.plugin.StationStore;
				List<PathNode> nodes = new ArrayList<PathNode>();
				for(int i = 3; i < args.length; i++) {
					Station station = s.getFromCode(args[i].split("~")[0]);
					if(station == null) {
						sender.sendMessage(ChatColor.RED + "Couldn't find the station/platform " + ChatColor.YELLOW + args[i] + ChatColor.RED + ".");
						return true;
					}
					if(station.platforms.get(args[i].split("~")[1]) == null) {
						sender.sendMessage(ChatColor.RED + "Couldn't find the platform " + ChatColor.YELLOW + args[i].split("~")[1] + ChatColor.RED + " at " + args[i] + ".");
						return true;
					}
					PathNode stationN = station.platforms.get(args[i].split("~")[1]).node;
					if(stationN == null) {
						sender.sendMessage(ChatColor.RED + "Couldn't find the platform " + ChatColor.YELLOW + args[i].split("~")[1] + ChatColor.RED + " at " + args[i] + ".");
						return true;
					}
					nodes.add(stationN);
				}
				if(nodes.size() < 2) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route create [LINE] [ROUTE] [Start~X] [Station1~X] (...) [End~X]");
					return true;
				}
				r = new PathRoute(args[2], nodes, l);
				l.addRoute(r, args[2]);
				sender.sendMessage(ChatColor.GREEN + "Created route " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + " on the " + args[1] + " line with " + ChatColor.YELLOW + r.route.size() + ChatColor.GREEN + " connections.");
				return true;
			} else if(args[0].equals("ccreate")) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "You can only run this command as a player!");
					return true;
				}
				if(args.length < 4) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route ccreate [LINE] [ROUTE] [Start~X]");
					return true;
				}
				MetroLine l = TrainCarts.plugin.lines.getLine(args[1]);
				if(l == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1] + ChatColor.RED + ".");
					return true;
				}
				PathRoute r = l.getRoute(args[2]);
				if(r != null) {
					sender.sendMessage(ChatColor.RED + "There's already a route named " + ChatColor.YELLOW + args[2] + ChatColor.RED + " in the " + args[1] + " line.");
					return true;
				}
				StationStore s = TrainCarts.plugin.StationStore;
				Station station = s.getFromCode(args[3].split("~")[0]);
				if(station == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find the station/platform " + ChatColor.YELLOW + args[3] + ChatColor.RED + ".");
					return true;
				}
				if(station.platforms.get(args[3].split("~")[1]) == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find the platform " + ChatColor.YELLOW + args[3].split("~")[1] + ChatColor.RED + " at " + args[3] + ".");
					return true;
				}
				PathNode stationN = station.platforms.get(args[3].split("~")[1]).node;
				if(stationN == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find the platform " + ChatColor.YELLOW + args[3].split("~")[1] + ChatColor.RED + " at " + args[3] + ".");
					return true;
				}
				((Player) sender).teleport(stationN.loc);
				RouteProgress rp = new RouteProgress(((Player) sender), stationN, l, args[2]);
				TrainCarts.plugin.playerProgress.put(sender.getName(), rp);
				sender.sendMessage(ChatColor.GREEN + "Started creation of route " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + " on the " + args[1] + " line. Left click to add a node and right click to finish the route. Send \"Cancel\" to cancel the route creation.");
				return true;
			} else if(args[0].equals("remove")) {
				if(args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route remove [LINE] [ROUTE]");
					return true;
				}
				MetroLine l = TrainCarts.plugin.lines.getLine(args[1]);
				if(l == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1] + ChatColor.RED + ".");
					return true;
				}
				PathRoute r = l.getRoute(args[2]);
				if(r == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[2] + ChatColor.RED + " in the " + args[1] + " line.");
					return true;
				}
				l.deleteRoute(args[2]);
				sender.sendMessage(ChatColor.GREEN + "Deleted route " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + " on the " + args[1] + " line.");
				return true;
			} else if(args[0].equals("reverse")) {
				if(args.length < 4) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route reverse [LINE] [ROUTE] [ROUTE]");
					return true;
				}
				MetroLine l = TrainCarts.plugin.lines.getLine(args[1]);
				if(l == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1] + ChatColor.RED + ".");
					return true;
				}
				PathRoute r = l.getRoute(args[2]);
				if(r == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[2] + ChatColor.RED + " in the " + args[1] + " line.");
					return true;
				}
				if(args[3].equals("none")) {
					r.reverse = null;
					sender.sendMessage(ChatColor.GREEN + "Trains will" + ChatColor.RED + " be destroyed " + ChatColor.GREEN + "after finishing route " + args[2]);
					return true;
				} else if(args[3].equals("ecs")) {
					r.reverse = "ECS";
					sender.sendMessage(ChatColor.GREEN + "Trains will" + ChatColor.WHITE + " run a ECS service back to the depot " + ChatColor.GREEN + "after finishing route " + args[2]);
					return true;
				}
				MetroLine l2 = TrainCarts.plugin.lines.getLine(args[3].split(":")[0]);
				if(l2 == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[3].split(":")[0] + ChatColor.RED + ".");
					return true;
				}
				PathRoute r2 = l2.getRoute(args[3].split(":")[1]);
				if(r2 == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[3] + ChatColor.RED + " in the " + args[1] + " line.");
					return true;
				}
				r.reverse = args[3];
				sender.sendMessage(ChatColor.GREEN + "Trains will" + ChatColor.YELLOW + " run the " + r2.name + " service " + ChatColor.GREEN + "after finishing route " + args[2]);
				return true;
			}
			return false;
		} else {
			return false;
		}
	}

	private class TabComplete implements TabCompleter {
		@Override
		public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
			ArrayList<String> arguments = new ArrayList<String>();
			ArrayList<String> arguments2 = new ArrayList<String>();
			if (args.length == 1 && sender.isOp()) {
				arguments.add("create");
				arguments.add("ccreate");
				arguments.add("remove");
				arguments.add("list");
				arguments.add("reroute");
				arguments.add("info");
				arguments.add("reverse");
			} else if (args.length == 2 && sender.isOp() && (args[0].equals("create") || args[0].equals("ccreate") || args[0].equals("reroute") || args[0].equals("remove") ||  args[0].equals("reverse") || args[0].equals("list"))) {
				TrainCarts.plugin.lines.getLines().values().forEach(line -> {
					arguments.add(line.getName());
				});
			} else if ( (args.length == 3 && sender.isOp() && (args[0].equals("remove") || args[0].equals("reroute")  || args[0].equals("reverse")))) {
				MetroLine l = TrainCarts.plugin.lines.getLine(args[1]);
				if(l != null) {
					l.getRoutes().keySet().forEach(s -> {
						arguments.add(s);
					});
				}
			} else if (args.length > 3 && sender.isOp() && (args[0].equals("create"))) {
				HashMap<String, Station> ss = TrainCarts.plugin.StationStore.Stations;
				MetroLine l = TrainCarts.plugin.lines.getLine(args[1]);
				if(l != null) {
					ss.values().forEach(s -> {
						s.platforms.values().forEach(plat -> {
							if(plat.node.line.equals(l) || args[1] == "#GLOBAL") {
								arguments.add(s.code + "~" + plat.platform);
							}
						});
					});
				}
			} else if (args.length == 4 && sender.isOp() && (args[0].equals("ccreate"))) {
				HashMap<String, Station> ss = TrainCarts.plugin.StationStore.Stations;
				MetroLine l = TrainCarts.plugin.lines.getLine(args[1]);
				if(l != null) {
					ss.values().forEach(s -> {
						s.platforms.values().forEach(plat -> {
							if(plat.node.line.equals(l) || args[1] == "#GLOBAL") {
								arguments.add(s.code + "~" + plat.platform);
							}
						});
					});
				}
			} else if(args.length == 4 && args[0].equals("reverse")) {
				TrainCarts.plugin.lines.getLines().values().forEach(l -> {
					l.getRoutes().values().forEach(r -> {
						arguments.add(l.getName() + ":" + r.name);
					});
				});
				arguments.add("none");
				arguments.add("ecs");
			}
			if (args.length == 2 && sender.isOp() && (args[0].equals("info"))) {
				TrainCarts.plugin.lines.getLines().values().forEach(l -> {
					l.getRoutes().values().forEach(r -> {
						arguments.add(l.getName() + ":" + r.name);
					});
				});
			}
			arguments.forEach(arg -> {
				if(arg.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
					arguments2.add(arg);
				}
			});
			return arguments2;
		}
	}
}