package com.movies22.cashcraft.tc.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.utils.Colours;

import net.md_5.bungee.api.ChatColor;

public class TC2Command implements CommandExecutor {
	public TC2Command(TrainCarts tc) {
		tc.getCommand("tc2").setExecutor(this);
		tc.getCommand("tc2").setTabCompleter(new TabComplete());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0) {
			sender.sendMessage(ChatColor.WHITE + "Running TrainCarts 2 on version " + ChatColor.of(Colours.PURPLE.colour) + "1.20.1" + ChatColor.WHITE + "-" + ChatColor.of(Colours.CYAN.colour) + "2.0" + ChatColor.WHITE + ".");
			return true;
		}
		if(args[0].equals("about")) {
			sender.sendMessage(ChatColor.WHITE + "===== About TrainCarts 2 =====\n - Coded by: " + ChatColor.of(Colours.PURPLE.colour) + "Movies22 \n " + ChatColor.WHITE + "- Compiled on: " + ChatColor.of(Colours.CYAN.colour) + "24/10 \n " + ChatColor.ITALIC + "Beige line wen" + ChatColor.WHITE + "\n=====");
			return true;
		} else if(args[0].equals("reroute") && sender.isOp()) {
			TrainCarts.plugin.global.reroute();
			sender.sendMessage(ChatColor.GREEN + "Rerouted " + TrainCarts.plugin.global.getNodes().size() + " nodes");
			return true;
		} else {
			sender.sendMessage(ChatColor.WHITE + "Running TrainCarts 2 on version " + ChatColor.of(Colours.PURPLE.colour) + "1.20.1" + ChatColor.WHITE + "-" + ChatColor.of(Colours.CYAN.colour) + "2.0" + ChatColor.WHITE + ".");
			return true;
		}
	}

	private class TabComplete implements TabCompleter {
		@Override
		public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
			ArrayList<String> arguments = new ArrayList<String>();
			ArrayList<String> arguments2 = new ArrayList<String>();
			if (args.length == 1) {
				arguments.add("about");
				arguments.add("version");
				if (sender.isOp()) {
					arguments.add("reroute");
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