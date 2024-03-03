package com.movies22.cashcraft.tc.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;
import com.movies22.cashcraft.tc.pathFinding.PathNode;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;
import com.movies22.cashcraft.tc.utils.Colours;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class LineCommand implements CommandExecutor {
	public LineCommand(TrainCarts tc) {
		tc.getCommand("line").setExecutor(this);
		tc.getCommand("line").setTabCompleter(new TabComplete());
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
		if (args.length < 1)
			return false;
		if (sender.isOp()) {
			if (args[0].equals("create")) {
				if (args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE
							+ "/line create [COLOUR] [Line Name]");
					return true;
				}
				String colour = args[1];
				List<String> a = Arrays.asList(args);
				ArrayList<String> b = new ArrayList<String>(a);
				b.remove(0);
				b.remove(0);
				String name = String.join(" ", b);
				if(name.toLowerCase().equals("brown")) {
					sender.sendMessage("WHY ARE YOU MAKING THE SHIT LINE");
					return true;
				}
				if(TrainCarts.plugin.lines.getLine(name) != null) {
					sender.sendMessage(
							ChatColor.RED + "There's already a line named " + ChatColor.YELLOW + name + ChatColor.RED + ".");
					return true;
				}
				TrainCarts.plugin.lines.createLine(name, colour);
				sender.sendMessage(ChatColor.GREEN + "Created line " + ChatColor.of(colour) + name + ChatColor.GREEN
						+ " line.");
				return true;
			} else if (args[0].equals("char")) {
				if (args.length < 3) {
					sender.sendMessage(
							ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/line char [Line Name] [Character]");
					return true;
				}
				String name = args[1];
				String character = args[2];
				MetroLine l = TrainCarts.plugin.lines.getLine(name);
				if (l == null || name.equals("#GLOBAL")) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a line named " + ChatColor.YELLOW + name + ChatColor.RED + ".");
					return true;
				}
				l.setChar(character);
				sender.sendMessage(ChatColor.GREEN + "line " + ChatColor.of(l.getColour()) + l.getName() + ChatColor.GREEN
						+ " now uses the character " + ChatColor.YELLOW + character + ChatColor.GREEN + " for OSI.");
				return true;
			} else if (args[0].equals("remove")) {
				if (args.length < 2) {
					sender.sendMessage(
							ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/line remove [Line Name]");
					return true;
				}
				List<String> a = Arrays.asList(args);
				ArrayList<String> b = new ArrayList<String>(a);
				b.remove(0);
				String name = String.join(" ", b);
				MetroLine l = TrainCarts.plugin.lines.getLine(name);
				if (l == null || name.equals("#GLOBAL")) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a line named " + ChatColor.YELLOW + name + ChatColor.RED + ".");
					return true;
				}
				TrainCarts.plugin.lines.deleteLine(name);
				sender.sendMessage(ChatColor.GREEN + "Deleted line " + ChatColor.of(l.getColour()) + l.getName() + ChatColor.GREEN
						+ " line.");
				return true;
			} else if(args[0].equals("reroute") && sender.isOp()) {
				List<String> a = Arrays.asList(args);
				ArrayList<String> b = new ArrayList<String>(a);
				b.remove(0);
				String name = String.join(" ", b);
				MetroLine l = TrainCarts.plugin.lines.getLine(name);
				if (l == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find a line named " + ChatColor.YELLOW + name + ChatColor.RED + ".");
					return true;
				}
				l.reroute();
				sender.sendMessage(ChatColor.GREEN + "Rerouted " + l.getNodes().size() + " nodes");
				return true;
			}
		}
		if (args[0].equals("list")) {
			msg = java.util.Arrays.copyOf(msg, 0);
			HashMap<String, MetroLine> lines = TrainCarts.plugin.lines.getLines();
			TextComponent m1 = new TextComponent(ChatColor.YELLOW + "There's a total of ");
			msg = append(msg, m1);
			TextComponent m2 = new TextComponent(ChatColor.GREEN + "" + lines.values().size());
			msg = append(msg, m2);
			TextComponent m3 = new TextComponent(ChatColor.YELLOW + " lines: ");
			msg = append(msg, m3);
			TextComponent m4 = new TextComponent(ChatColor.YELLOW + ", ");
			TextComponent m5 = new TextComponent(ChatColor.YELLOW + " and ");
			i = 0;
			lines.values().forEach(s -> {
				if (i > 0 && i < (lines.values().size() - 1)) {
					msg = append(msg, m4);
				}
				i++;
				TextComponent clickable = new TextComponent(ChatColor.ITALIC + "" + ChatColor.GREEN + s.getName());
				TextComponent hover[] = { new TextComponent("Colour: " + s.getColour()) };
				clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
				clickable.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/line info " + s.getName()));
				msg = append(msg, clickable);
				if (i == (lines.values().size() - 1)) {
					msg = append(msg, m5);
				};
			});
			sender.spigot().sendMessage(msg);
			return true;
		} else if(args[0].equals("info")) {
			msg = java.util.Arrays.copyOf(msg, 0);
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE
						+ "/line info [CName]");
				return true;
			}
			List<String> a = Arrays.asList(args);
			ArrayList<String> b = new ArrayList<String>(a);
			b.remove(0);
			String n = String.join(" ", b);
			MetroLine l = TrainCarts.plugin.lines.getLine(n);
			if (l == null) {
				sender.sendMessage(ChatColor.RED + "Couldn't find a line named " + ChatColor.YELLOW + n + ChatColor.RED + ".");
				return true;
			}
			
			TextComponent header = new TextComponent(ChatColor.WHITE + "" + ChatColor.BOLD + "===== Information of " + l.getName() + " =====\n");
			msg = append(msg, header);
			TextComponent name = new TextComponent(ChatColor.YELLOW + " - Line name: " + ChatColor.GREEN + l.getName() + "\n");
			msg = append(msg, name);
			TextComponent colour = new TextComponent(ChatColor.YELLOW + " - Line colour: " + ChatColor.GREEN + l.getColour() + "\n");
			msg = append(msg, colour);
			TextComponent character = new TextComponent(ChatColor.YELLOW + " - Line character: " + ChatColor.GREEN + l.getChar() + "\n");
			msg = append(msg, character);
			TextComponent nodes1 = new TextComponent(ChatColor.YELLOW + " - Nodes: " + ChatColor.GREEN + l.getNodes().size() + "\n");
			msg = append(msg, nodes1);
			Collection<PathNode> nodes = l.getNodes().values();
			nodes.removeIf(node -> (node.getAction().getClass() != SignActionPlatform.class));
			TextComponent nodes2 = new TextComponent(ChatColor.YELLOW + " - Stations/Platforms served (" + nodes.size() + "): ");
			msg = append(msg, nodes2);
			TextComponent m4 = new TextComponent(ChatColor.YELLOW + ", ");
			i = 0;
			nodes.forEach(node -> {
				if (i > 0 && i < (nodes.size())) {
					msg = append(msg, m4);
				}
				i++;
				TextComponent hover[] = { new TextComponent("Location: " + node.loc.getBlockX() + "/" + node.loc.getBlockY() + "/" + node.loc.getBlockZ()) };
				SignActionPlatform plat = (SignActionPlatform) node.getAction();
				TextComponent clickable;
				if(plat.station == null) {
					clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + "[UNKNOWN]");
				} else {
					clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + plat.station.code + "~" + plat.platform);
				}
				clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
				msg = append(msg, clickable);
			});
			TextComponent trains = new TextComponent(ChatColor.YELLOW + "\n - " + l.getTrains().size() + " Trains: " + ChatColor.GREEN);
			msg = append(msg, trains);
			if(l.getTrains().size() < 1) {
				msg = append(msg, new TextComponent(ChatColor.ITALIC + "None"));
			} else {
				l.getTrains().forEach(train -> {
					msg = append(msg, m4);
					TextComponent clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + train.getHeadcode());
					clickable.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/train info " + train.getHeadcode()));
					msg = append(msg, clickable);
				});
			}
			TextComponent routes = new TextComponent(ChatColor.YELLOW + "\n - " + l.getRoutes().size() + " Routes: " + ChatColor.GREEN);
			msg = append(msg, routes);
			if(l.getRoutes().size() < 1) {
				msg = append(msg, new TextComponent(ChatColor.ITALIC + "None"));
			} else {
				i = 0;
				l.getRoutes().values().forEach(route -> {
					if (i > 0 && i < (l.getRoutes().values().size())) {
						msg = append(msg, m4);
					}
					i++;
					TextComponent clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + route.stops.get(0).station.code + " (" + route.stops.get(0).platform + ")"+ " > " + route.stops.get(route.stops.size() - 1).station.code + " (" + route.stops.get(route.stops.size() - 1).platform + ")");
					clickable.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/route info " + route._line.getName() + " " + route.name));
					TextComponent hover[] = { new TextComponent("Name: " + route.name) };
					clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
					msg = append(msg, clickable);
				});
			}
			TextComponent footer = new TextComponent(ChatColor.WHITE + "\n" + ChatColor.BOLD + "=====");
			msg = append(msg, footer);
			sender.spigot().sendMessage(msg);
			return true;
		}
		return false;
	}

	private class TabComplete implements TabCompleter {
//		/station list
//		/station info [code]
//		/station create [code] [station name...]
//		/station delete [code]
//		/station teleport [code] [platform]
		@Override
		public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
			ArrayList<String> arguments = new ArrayList<String>();
			ArrayList<String> arguments2 = new ArrayList<String>();
			if (args.length == 1) {
				arguments.add("list");
				arguments.add("info");
				if (sender.isOp()) {
					arguments.add("create");
					arguments.add("remove");
					arguments.add("reroute");
					arguments.add("char");
				}
			} else if (args.length == 2 && (args[0].equals("info") || args[0].equals("char")
					|| (  (args[0].equals("remove") || args[0].equals("reroute") ) && sender.isOp() ) ) ) {
				TrainCarts.plugin.lines.getLines().values().forEach(l -> {
					arguments.add(l.getName());
				});
			} else if (args.length == 2 && (args[0].equals("create") && sender.isOp() ) ) {
				for(Colours c : Colours.values()) {
					arguments.add(c.colour + " " + String.join("", c.name().split(" ")));
				}
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