package com.movies22.cashcraft.tc.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.Station;
import com.movies22.cashcraft.tc.controller.StationStore;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class StationCommand implements CommandExecutor {
	public StationCommand(TrainCarts tc) {
		tc.getCommand("station").setExecutor(this);
		tc.getCommand("station").setTabCompleter(new TabComplete());
	}

	static <T> T[] append(T[] arr, T lastElement) {
		final int N = arr.length;
		arr = java.util.Arrays.copyOf(arr, N + 1);
		arr[N] = lastElement;
		return arr;
	}

	private TextComponent[] msg = {};
	private int i;
	private Boolean stationCheck;
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1)
			return false;
		if (sender.isOp()) {
			if (args[0].equals("create")) {
				if (args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE
							+ "/station create [CODE] [Station Name]");
					return true;
				}
				String code = args[1];
				List<String> a = Arrays.asList(args);
				ArrayList<String> b = new ArrayList<String>(a);
				b.remove(0);
				b.remove(0);
				String name = String.join(" ", b);
				Station s = new Station(code, name);
				TrainCarts.plugin.StationStore.addStation(s);
				sender.sendMessage(ChatColor.GREEN + "Created station " + ChatColor.YELLOW + name + ChatColor.GREEN
						+ " with the station code " + ChatColor.YELLOW + code + ChatColor.GREEN + ".");
				return true;
			} else if (args[0].equals("headcode")) {
				if (args.length < 2) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE
							+ "/station headcode [CODE] <headcode/reset>");
					return true;
				}
				String code = args[1];
				Station s = TrainCarts.plugin.StationStore.getFromCode(code);
				if(args.length == 2) {
					sender.sendMessage(ChatColor.GREEN + "The current headcode letter of " + ChatColor.YELLOW + s.name + ChatColor.GREEN + " is " + ChatColor.YELLOW + s.headcode + ChatColor.GREEN + ".");
					return true;
				}
				if(args[2].equals("reset")) {
					s.headcode = null;
					s.canTerminate = false;
					sender.sendMessage(ChatColor.GREEN + "Reseted the headcode letter of " + ChatColor.YELLOW + s.name + ChatColor.GREEN + ChatColor.GREEN + ".");
					return true;
				} else {
					stationCheck = false;
					TrainCarts.plugin.StationStore.Stations.values().forEach(station -> {
						if(station.headcode != null ) {
							if(station.headcode.equals(args[2])) {
								sender.sendMessage(ChatColor.RED + "The headcode letter " + ChatColor.YELLOW + args[2] + ChatColor.RED + " is already in use by " +  ChatColor.YELLOW + station.name + ChatColor.RED + ".");
								stationCheck = true;
								return;
							}
						}
					});
					if(stationCheck) {
						return true;
					}
					s.headcode = args[2];
					s.canTerminate = true;
					sender.sendMessage(ChatColor.GREEN + "The new headcode letter of " + ChatColor.YELLOW + s.name + ChatColor.GREEN + " is " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + ".");
					return true;
				}
			} else if (args[0].equals("close") && sender.isOp()) {
				if (args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE
							+ "/station close [CODE] <close/open>");
					return true;
				}
				String code = args[1];
				Station s = TrainCarts.plugin.StationStore.getFromCode(code);
				if(args[2].equals("close")) {
					s.closed = true;
					sender.sendMessage(ChatColor.GREEN + "Trains will no longer stop at " + ChatColor.YELLOW + s.name + ChatColor.GREEN + ChatColor.GREEN + ".");
					return true;
				} else {
					s.closed = false;
					sender.sendMessage(ChatColor.GREEN + "Trains will now stop at " + ChatColor.YELLOW + s.name + ChatColor.GREEN + ChatColor.GREEN + ".");
					return true;
				}
			} else if (args[0].equals("remove")) {
				if (args.length < 2) {
					sender.sendMessage(
							ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/station remove [CODE]");
					return true;
				}
				String code = args[1];
				Station s = TrainCarts.plugin.StationStore.getFromCode(code);
				if (s == null) {
					sender.sendMessage(ChatColor.RED + "Station with the code " + args[1] + " was not found.");
					return true;
				}
				TrainCarts.plugin.StationStore.removeStation(code);
				sender.sendMessage(ChatColor.GREEN + "Deleted station " + ChatColor.YELLOW + s.name + ChatColor.GREEN
						+ " with the station code " + ChatColor.YELLOW + code + ChatColor.GREEN + ".");
				return true;
			} else if (args[0].equals("osi")) {
				if (args.length < 4) {
					sender.sendMessage(
							ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/station osi [CODE] [metro/heavy] [OSI String]");
					return true;
				}
				String code = args[1];
				Station s = TrainCarts.plugin.StationStore.getFromCode(code);
				if (s == null) {
					sender.sendMessage(ChatColor.RED + "Station with the code " + args[1] + " was not found.");
					return true;
				}
				if(args[2].equals("station")) {
					String osi = args[3];
					if(osi.equals("none")) {
						osi = "";
					}
					s.station = osi;
					sender.sendMessage(ChatColor.GREEN + "Station " + ChatColor.YELLOW + s.name + ChatColor.GREEN
						+ " was connected to station " + ChatColor.YELLOW + osi + ChatColor.GREEN + " .");
				} else {
					Boolean a = args[2].equals("heavy");
					String osi = args[3];
					if(osi.equals("none")) {
						osi = "";
					}
					s.setOsi(osi, a);
					sender.sendMessage(ChatColor.GREEN + "Station " + ChatColor.YELLOW + s.name + ChatColor.GREEN
						+ " now has the following OSI string: " + ChatColor.YELLOW + osi + ChatColor.GREEN + " for " + (a ? "Heavy Rail" : "Metro") + " services.");
				}
				return true;
			} else if (args[0].equals("teleport")) {
				if (args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE
							+ "/station teleport [CODE] [PLATFORM]");
					return true;
				}
				String code = args[1];
				Station s = TrainCarts.plugin.StationStore.getFromCode(code);
				if (s == null) {
					sender.sendMessage(ChatColor.RED + "Station with the code " + args[1] + " was not found.");
					return true;
				}
				SignActionPlatform plat = s.platforms.get(args[2]);
				if (plat == null) {
					sender.sendMessage(
							ChatColor.RED + "Platform " + args[2] + " in station " + s.name + " was not found.");
					return true;
				}
				sender.sendMessage(ChatColor.GREEN + "Teleporting to " + ChatColor.YELLOW + s.name + ChatColor.GREEN
						+ "~" + ChatColor.YELLOW + args[2] + ChatColor.GREEN + ".");
				Player p = (Player) sender;
				p.teleport(plat.node.loc);
				return true;
			}
		}
		if (args[0].equals("list")) {
			msg = java.util.Arrays.copyOf(msg, 0);
			StationStore ss = TrainCarts.plugin.StationStore;
			TextComponent m1 = new TextComponent(ChatColor.YELLOW + "There's a total of ");
			msg = append(msg, m1);
			TextComponent m2 = new TextComponent(ChatColor.GREEN + "" + ss.Stations.values().size());
			msg = append(msg, m2);
			TextComponent m3 = new TextComponent(ChatColor.YELLOW + " stations: ");
			msg = append(msg, m3);
			TextComponent m4 = new TextComponent(ChatColor.YELLOW + ", ");
			TextComponent m5 = new TextComponent(ChatColor.YELLOW + " and ");
			i = 0;
			ss.Stations.values().forEach(s -> {
				if (i > 0 && i < (ss.Stations.values().size() - 1)) {
					msg = append(msg, m4);
				}
				i++;
				TextComponent clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + s.name);
				TextComponent hover[] = { new TextComponent("Code: " + s.code) };
				clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
				clickable.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/station info " + s.code));
				msg = append(msg, clickable);
				if (i == (ss.Stations.values().size() - 1)) {
					msg = append(msg, m5);
				};
			});
			sender.spigot().sendMessage(msg);
			return true;
		} else if(args[0].equals("info")) {
			msg = java.util.Arrays.copyOf(msg, 0);
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE
						+ "/station info [CODE]");
				return true;
			}
			String c = args[1];
			Station s = TrainCarts.plugin.StationStore.getFromCode(c);
			if (s == null) {
				sender.sendMessage(ChatColor.RED + "Station with the code " + args[1] + " was not found.");
				return true;
			}
			TextComponent header = new TextComponent(ChatColor.WHITE + "" + ChatColor.BOLD + "===== Information of " + s.name + " =====\n");
			msg = append(msg, header);
			TextComponent name = new TextComponent(ChatColor.YELLOW + " - Station name: " + ChatColor.GREEN + s.name + "\n");
			msg = append(msg, name);
			TextComponent code = new TextComponent(ChatColor.YELLOW + " - Station code: " + ChatColor.GREEN + s.code + "\n");
			msg = append(msg, code);
			TextComponent terminus = new TextComponent(ChatColor.YELLOW + " - Terminus: " + ChatColor.GREEN + (s.canTerminate ? "YES" : ChatColor.DARK_RED + "NO")+ "\n");
			msg = append(msg, terminus);
			TextComponent headcode = new TextComponent(ChatColor.YELLOW + " - Headcode letter: " + ChatColor.GREEN + (s.headcode != null ? s.headcode : ChatColor.DARK_RED + "" + ChatColor.ITALIC + "none")+ "\n");
			msg = append(msg, headcode);
			TextComponent plats = new TextComponent(ChatColor.YELLOW + " - " + s.platforms.size() + " Platforms: " + ChatColor.GREEN);
			msg = append(msg, plats);
			if(s.platforms.size() < 1) {
				msg = append(msg, new TextComponent(ChatColor.ITALIC + "None"));
			} else {
				TextComponent m4 = new TextComponent(ChatColor.YELLOW + ", ");
				i = 0;
				s.platforms.values().forEach(plat -> {
					if (i > 0 && i < (s.platforms.values().size())) {
						msg = append(msg, m4);
					}
					i++;
					TextComponent clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + plat.platform + "");
					clickable.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/station teleport " + s.code + " " + plat.platform));
					msg = append(msg, clickable);
				});
			}
			TextComponent trains = new TextComponent(ChatColor.YELLOW + "\n - " + s.groups.size() + " Osi: " + ChatColor.GREEN + s.osi + "§e/§a" + s.hosi + "§e/§a" + s.station + "§e.");
			msg = append(msg, trains);
			TextComponent lines = new TextComponent(ChatColor.YELLOW + "\n - " + s.lines.size() + " Lines: " + ChatColor.GREEN);
			msg = append(msg, lines);
			if(s.lines.size() < 1) {
				msg = append(msg, new TextComponent(ChatColor.ITALIC + "None"));
			} else {
				i = 0;
				TextComponent m4 = new TextComponent(ChatColor.YELLOW + ", ");
				s.lines.forEach(line -> {
					if (i > 0 && i < (s.lines.size())) {
						msg = append(msg, m4);
					}
					i++;
					TextComponent clickable = new TextComponent(ChatColor.of(line.getColour()) + "" + ChatColor.ITALIC + line.getName());
					clickable.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/say " + line.getName()));
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
//		/station info [code] <platform>
//		/station create [code] [station name...]
//		/station delete [code]
//		/station teleport [code] [platform]
//      /station headcode [code] <headcode>
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
					arguments.add("teleport");
					arguments.add("headcode");
					arguments.add("osi");
				}
			} else if (args.length == 2 && (args[0].equals("info")
					|| ( (args[0].equals("remove") || args[0].equals("osi") || args[0].equals("teleport") || args[0].equals("headcode")) && sender.isOp() ) )) {
				TrainCarts.plugin.StationStore.Stations.values().forEach(s -> {
					arguments.add(s.code);
				});
			} else if (args.length == 3 && (args[0].equals("osi") && sender.isOp() )) {
				arguments.add("metro");
				arguments.add("heavy");
				arguments.add("station");
			} else if (args.length == 3 && sender.isOp() && (args[0].equals("teleport") || args[0].equals("info"))) {
				Station a = TrainCarts.plugin.StationStore.getFromCode(args[1]);
				if (a != null) {
					a.platforms.values().forEach(p -> {
						arguments.add(p.platform);
					});
				}
			} else if (args.length == 3 && sender.isOp() && (args[0].equals("headcode"))) {
				ArrayList<String> a = new ArrayList<String>(Arrays.asList("A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"));
				ArrayList<String> b = new ArrayList<String>(a);
				TrainCarts.plugin.StationStore.Stations.values().forEach(s -> {
					if(a.contains(s.headcode)) {
						b.remove(s.headcode);
					}
				});
				b.forEach(c -> {
					arguments.add(c);
				});
				arguments.add("reset");
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