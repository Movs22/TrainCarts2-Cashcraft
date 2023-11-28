package com.movies22.cashcraft.tc.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.api.MinecartMember;
import com.movies22.cashcraft.tc.controller.MinecartMemberController;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;

import net.md_5.bungee.api.ChatColor;

public class TrainCommand implements CommandExecutor {
	public TrainCommand(TrainCarts tc) {
		tc.getCommand("train").setExecutor(this);
		tc.getCommand("train").setTabCompleter(new TabComplete());
	}

	static <T> T[] append(T[] arr, T lastElement) {
		final int N = arr.length;
		arr = java.util.Arrays.copyOf(arr, N + 1);
		arr[N] = lastElement;
		return arr;
	}

	private int trains = 0;
	private int carts = 0;
	private String t;
	private String st;
	List<String> a;
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.isOp()) {
			return false;
		}
		if(args.length == 0) {
			return false;
		}
		if (args[0].equals("list") && sender.isOp()) {
			if (args.length == 2) {
				MetroLine l = TrainCarts.plugin.lines.getLine(args[1]);
				if (l == null) {
					sender.sendMessage(ChatColor.RED + "Line " + args[1] + " wasn't found.");
					return true;
				}
				MinecartMemberController s = TrainCarts.plugin.MemberController;
				int trains = s.getHeads().stream().filter(m -> m.getGroup().getLine().equals(l) && m.virtualized == false).collect(Collectors.toList()).size();
				int trains2 = s.getHeads().stream().filter(m -> m.getGroup().getLine().equals(l) &&m.getGroup().virtualized == true).collect(Collectors.toList()).size();
				int carts = s.getMembers().stream().filter(m -> m.getGroup().getLine().equals(l) &&m.getGroup().virtualized == true).collect(Collectors.toList()).size();
				carts = carts + trains;
				t = "";
				a = new ArrayList<String>();
				s.getHeads().forEach(mm -> {
					if(mm.getGroup().getLine().equals(l)) {
						a.add( ChatColor.of(l.getColour()) + mm.getGroup().getHeadcode());
					}
				});
				Collections.sort(a);
				a.forEach(b -> {
					t = t + b + ", ";
				});
				a.clear();
				sender.sendMessage(ChatColor.GREEN + "There are a total of " + ChatColor.YELLOW + trains
						+ ChatColor.GREEN + "§a trains §a(§e" + trains2 + "§a virtual trains) and " + ChatColor.YELLOW + carts + ChatColor.GREEN
						+ " carts on the " + ChatColor.of(l.getColour()) + l.getName() + "§a line. \n §f" + t);
				return true;
			} else {
				MinecartMemberController s = TrainCarts.plugin.MemberController;
				int trains = s.getHeads().stream().filter(m -> m.getGroup().virtualized == false).collect(Collectors.toList()).size();
				int trains2 = s.getHeads().stream().filter(m -> m.getGroup().virtualized == true).collect(Collectors.toList()).size();
				int carts = s.getMembers().size();
				t = "";
				a = new ArrayList<String>();
				s.getHeads().forEach(mm -> {
					a.add( ChatColor.of(mm.getGroup().getLineColour()) + mm.getGroup().getHeadcode());
				});
				Collections.sort(a);
				a.forEach(b -> {
					t = t + b + ", ";
				});
				a.clear();
				sender.sendMessage(ChatColor.GREEN + "There are a total of " + ChatColor.YELLOW + trains
						+ ChatColor.GREEN + "§a trains §a(§e" + trains2 + "§a virtual trains) and " + ChatColor.YELLOW + carts + ChatColor.GREEN
						+ " carts on the server. \n §f" + t);
				return true;
			}
		} else if (args[0].equals("unload") && sender.isOp()) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You can't run this command!");
				return  true;
			}
			MinecartMember m = this.getTrain(sender);
			if(m == null) {
				return true;
			}
			MinecartGroup g = m.getGroup();
			g.virtualize();
			sender.sendMessage(ChatColor.YELLOW + "This train has been virtualized.");
		} else if (args[0].equals("load") && sender.isOp()) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You can't run this command!");
				return  true;
			}
			MinecartMember m = this.getTrain(sender);
			if(m == null) {
				return true;
			}
			MinecartGroup g = m.getGroup();
			g.unVirtualize();
			sender.sendMessage(ChatColor.YELLOW + "This train has been un-virtualized.");
		} else if (args[0].equals("destination")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You can't run this command! (bozo)");
				return  true;
			}
			MinecartMember m = this.getTrain(sender);
			if(m == null) {
				return true;
			}
			MinecartGroup g = m.getGroup();
			if(g.currentRoute._end.getAction() instanceof SignActionPlatform) {
				sender.sendMessage(ChatColor.YELLOW + "This train is going to §a" + ((SignActionPlatform) g.currentRoute._end.getAction()).station.name + "§e.");
			} else {
				sender.sendMessage(ChatColor.YELLOW + "This train is running an §aECS Service§e.");
			}
			return true;
		} else if (args[0].equals("headcode")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You can't run this command! (bozo)");
				return  true;
			}
			MinecartMember m = this.getTrain(sender);
			if(m == null) {
				return true;
			}
			MinecartGroup g = m.getGroup();
			sender.sendMessage(ChatColor.YELLOW + "This train's headcode is §a" + g.getHeadcode() + "§e.");
			return true;
		} else if (args[0].equals("destroy") && sender.isOp()) { 
			MinecartMember m = null;
			if(args.length > 1) {
				if(args[1].startsWith("t:")) {
					String t = args[1].split(":")[1];
					for(MinecartMember mm : TrainCarts.plugin.MemberController.getHeads()) {
						if(mm.getGroup().getHeadcode().equals(t)) {
							m = mm;
							break;
						}
					};
				}
			} else {
				m = this.getTrain(sender);
				if(m == null) {
					return true;
				}
			}
			
			if(args.length > 1 && args[1].startsWith("l:")) {
				String l = args[1].split(":")[1];
				int i = 0;
				for(MinecartMember h : TrainCarts.plugin.MemberController.getHeads()) {
					if(h.getGroup().getLine().getName().equals(l)) {
						h.getGroup().destroy();
						i++;
					}
				};
				sender.sendMessage("§cDestroyed §e" + i + "§c train" + (i < 2 ? "" : "s") + " on the §e" + l + " line.");
				return true;
			}
			if(args.length > 1 && args[1].equals("all")) {
				int i = 0;
				for(MinecartMember h : TrainCarts.plugin.MemberController.getHeads()) {
					h.getGroup().destroy();
					i++;
				};
				sender.sendMessage("§cDestroyed §e" + i + "§c train" + (i < 2 ? "" : "s") + ".");
				return true;
			}
			m.getGroup().destroy();
			sender.sendMessage("§cDestroyed the selected train.");
			return true;
		} else if (args[0].equals("info")) {
			MinecartMember m = null;
			if(args.length > 1) {
				 for(MinecartMember mm : TrainCarts.plugin.MemberController.getHeads()) {
					 if(mm.getGroup().getHeadcode().equals(args[1])) {
						 m = mm;
						 break;
					 }
				 };
			} else {
				m = this.getTrain(sender);
				if(m == null) {
					return true;
				}
			}
			MinecartGroup g = m.getGroup();
			sender.sendMessage(ChatColor.YELLOW + "Cart index: §a" + g.getMembers().indexOf(m));
			sender.sendMessage(ChatColor.YELLOW + "Train is in memberStore: " + (TrainCarts.plugin.MemberController.getFromEntity(m.getEntity()) != null ? "§aYES" : "§cNO"));
			sender.sendMessage(ChatColor.YELLOW + "Train is virtualized: " + (g.virtualized ? "§aYES" : "§cNO"));
			if(g.currentRoute._end.getAction() instanceof SignActionPlatform) {
				sender.sendMessage(ChatColor.YELLOW + "This train is going to §a" + ((SignActionPlatform) g.currentRoute._end.getAction()).station.name + "§e.");
			} else {
				sender.sendMessage(ChatColor.YELLOW + "This train is running an §aECS Service§e.");
			}
			sender.sendMessage(ChatColor.YELLOW + "This train is currently running at a speed of §a" + m.currentSpeed + "§e/§a" + g.head()._targetSpeed  + "§e.");
			st = "§a";
			for(int i = 0; i < g.currentRoute.stops.size(); i++) {
				SignActionPlatform s = g.currentRoute.stops.get(i);
				st = st + s.station.name;
				if(i == (g.currentRoute.stops.size() - 2)) {
					st = st + "§e and §a";
				} else if(i < (g.currentRoute.stops.size() - 2)) {
					st = st + "§e, §a";
				}
			}
			
			sender.sendMessage(ChatColor.YELLOW + "This train will stop at §a" + st + "§e.");
			sender.sendMessage(ChatColor.YELLOW + "This train will be next at §a" + g.head().getNextNode().getLocationStr() + "§e.");
			return true;
		} else if (args[0].equals("speed")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You can't run this command! (bozo)");
				return  true;
			}
			MinecartMember m = this.getTrain(sender);
			if(m == null) {
				return true;
			}
			MinecartGroup g = m.getGroup();
			sender.sendMessage(ChatColor.YELLOW + "This train is currently running at a speed of §a" + g.head().currentSpeed + "§e/§a" + g.head()._targetSpeed  + "§e.");
			return true;
		} else if (args[0].equals("station")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You can't run this command! (bozo)");
				return  true;
			}
			MinecartMember m = this.getTrain(sender);
			if(m == null) {
				return true;
			}
			MinecartGroup g = m.getGroup();
			st = "§a";
			for(int i = 0; i < g.currentRoute.stops.size(); i++) {
				SignActionPlatform s = g.currentRoute.stops.get(i);
				st = st + s.station.name;
				if(i == (g.currentRoute.stops.size() - 2)) {
					st = st + "§e and §a";
				} else if(i < (g.currentRoute.stops.size() - 2)) {
					st = st + "§e, §a";
				}
			}
			
			sender.sendMessage(ChatColor.YELLOW + "This train will stop at §a" + st + "§e.");
			return true;
		}
		return false;
	}

	private MinecartMember getTrain(CommandSender sender) {
		Player p = (Player) sender;
		if( p.getVehicle() instanceof Minecart) {
			Minecart m = (Minecart) p.getVehicle();
			MinecartMember m2 = TrainCarts.plugin.MemberController.getFromEntity(m);
			if(m2 == null) {
				sender.sendMessage(ChatColor.RED + "You're riding a vanilla minecart. Please run this command only from an automated train!");
				return null;
			}
			return m2;
		} else {
			sender.sendMessage(ChatColor.RED + "You must be on a train to run this command.");
			return null;
		}
	}
	
	private class TabComplete implements TabCompleter {
		@Override
		public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
			ArrayList<String> arguments = new ArrayList<String>();
			ArrayList<String> arguments2 = new ArrayList<String>();
			if (args.length == 1) {
				arguments.add("destination");
				arguments.add("headcode");
				arguments.add("station");
				arguments.add("speed");
				arguments.add("info");
				arguments.add("list");
				if (sender.isOp()) {
					arguments.add("destroy");
					arguments.add("unload");
					arguments.add("load");
				}
			}
			if (args.length == 2 && args[0].equals("list") && sender.isOp()) {
				TrainCarts.plugin.lines.getLines().values().forEach(l -> {
					arguments.add(l.getName());
				});
			}

			if (args.length == 2 && args[0].equals("info")) {
				TrainCarts.plugin.MemberController.getHeads().forEach(m -> {
					arguments.add(m.getGroup().getHeadcode());
				});
			}
			
			if (args.length == 2 && args[0].equals("destroy")) {
				if(args[1].startsWith("t:")) {
					TrainCarts.plugin.MemberController.getHeads().forEach(m -> {
						arguments.add("t:" + m.getGroup().getHeadcode());
					});
				} else if(args[1].startsWith("l:")) {
					TrainCarts.plugin.lines.getLines().values().forEach(l -> {
						arguments.add("l:" + l.getName());
					});
			 	} else {
			 		arguments.add("all");
			 		arguments.add("l:");
			 		arguments.add("t:");
			 	}
			}
			arguments.forEach(arg -> {
				if (arg.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
					arguments2.add(arg);
				}
			});
			return arguments2;
		}
	}

}