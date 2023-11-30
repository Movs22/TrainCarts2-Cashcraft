package com.movies22.cashcraft.tc.commands;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.utils.Colours;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class TC2Command implements CommandExecutor {
   public TC2Command(TrainCarts tc) {
      tc.getCommand("tc2").setExecutor(this);
      tc.getCommand("tc2").setTabCompleter(new TC2Command.TabComplete());
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (args.length == 0) {
         sender.sendMessage(ChatColor.WHITE + "Running TrainCarts 2 on version " + ChatColor.of(Colours.PURPLE.colour) + TrainCarts.plugin.mcVersion + ChatColor.WHITE + "-" + ChatColor.of(Colours.CYAN.colour) + TrainCarts.plugin.version + ChatColor.WHITE + ".");
         return true;
      } else if (args[0].equals("about")) {
         sender.sendMessage(ChatColor.WHITE + "===== About TrainCarts 2 =====\n - Coded by: " + ChatColor.of(Colours.PURPLE.colour) + "Movies22 \n " + ChatColor.WHITE + "- Compiled on: " + ChatColor.of(Colours.CYAN.colour) + "24/10 \n " + ChatColor.ITALIC + "Beige line wen" + ChatColor.WHITE + "\n=====");
         return true;
      } else if (args[0].equals("reroute") && sender.isOp()) {
         TrainCarts.plugin.global.reroute();
         sender.sendMessage(ChatColor.GREEN + "Rerouted " + TrainCarts.plugin.global.getNodes().size() + " nodes");
         return true;
      } else if (args[0].equals("radio") && sender.isOp() && args.length >= 2) {
         Player p = (Player)sender;
         Sound s = Sound.valueOf(args[1]);
         World w = p.getLocation().getWorld();
         TrainCarts.plugin.StationStore.Stations.values().forEach((m) -> {
            m.platforms.values().forEach((plat) -> {
               Location l = plat.node.getLocation();
               w.playSound(l, s, 10.0F, 1.0F);
            });
         });
         sender.sendMessage(ChatColor.GREEN + "Playing " + args[1] + " on ALL nodes");
         return true;
      } else {
         sender.sendMessage(ChatColor.WHITE + "Running TrainCarts 2 on version " + ChatColor.of(Colours.PURPLE.colour) + TrainCarts.plugin.mcVersion + ChatColor.WHITE + "-" + ChatColor.of(Colours.CYAN.colour) + TrainCarts.plugin.version + ChatColor.WHITE + ".");
         return true;
      }
   }

   private class TabComplete implements TabCompleter {
      private TabComplete() {
      }

      public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
         ArrayList<String> arguments = new ArrayList();
         ArrayList<String> arguments2 = new ArrayList();
         if (args.length == 1) {
            arguments.add("about");
            arguments.add("version");
            if (sender.isOp()) {
               arguments.add("reroute");
               arguments.add("radio");
            }
         }

         if (args.length == 2 && args[0].equals("radio")) {
            Sound[] var7 = Sound.values();
            int var8 = var7.length;

            for(int var9 = 0; var9 < var8; ++var9) {
               Sound s = var7[var9];
               arguments.add(s.name());
            }
         }

         arguments.forEach((arg) -> {
            if (arg.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
               arguments2.add(arg);
            }

         });
         return arguments2;
      }

      // $FF: synthetic method
      TabComplete(Object x1) {
         this();
      }
   }
}
