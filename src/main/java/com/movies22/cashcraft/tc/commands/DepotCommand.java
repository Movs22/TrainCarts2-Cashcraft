package com.movies22.cashcraft.tc.commands;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.api.Depot;
import com.movies22.cashcraft.tc.api.MetroLines;
import com.movies22.cashcraft.tc.api.SpawnerRate;
import com.movies22.cashcraft.tc.controller.DepotController;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class DepotCommand implements CommandExecutor {
   private TextComponent[] msg = new TextComponent[0];
   private int i = 0;

   public DepotCommand(TrainCarts tc) {
      tc.getCommand("depot").setExecutor(this);
      tc.getCommand("depot").setTabCompleter(new DepotCommand.TabComplete());
   }

   static <T> T[] append(T[] arr, T lastElement) {
      int N = arr.length;
      arr = Arrays.copyOf(arr, N + 1);
      arr[N] = lastElement;
      return arr;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (sender.isOp()) {
         String code;
         ArrayList b;
         if (args[0].equals("create")) {
            if (args.length < 3) {
               sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/depot create [CODE] [Depot Name]");
               return true;
            }

            code = args[1];
            List<String> a = Arrays.asList(args);
            b = new ArrayList(a);
            b.remove(0);
            b.remove(0);
            String name = String.join(" ", b);
            Depot d = new Depot(code, name);
            TrainCarts.plugin.DepotStore.addDepot(d);
            sender.sendMessage(ChatColor.GREEN + "Created depoted " + ChatColor.YELLOW + name + ChatColor.GREEN + " with code " + ChatColor.YELLOW + code + ChatColor.GREEN + ".");
            return true;
         }

         Depot d;
         if (args[0].equals("remove")) {
            if (args.length < 2) {
               sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/depot remove [CODE]");
               return true;
            }

            code = args[1];
            d = TrainCarts.plugin.DepotStore.getFromCode(code);
            if (d == null) {
               sender.sendMessage(ChatColor.RED + "Depot with the code " + args[0] + " was not found.");
               return true;
            }

            TrainCarts.plugin.DepotStore.removeDepot(code);
            sender.sendMessage(ChatColor.GREEN + "Deleted depot " + ChatColor.YELLOW + d.name + ChatColor.GREEN + " with code " + ChatColor.YELLOW + code + ChatColor.GREEN + " (Served " + ChatColor.YELLOW + d.routes.size() + ChatColor.GREEN + " routes).");
            return true;
         }

         if (args[0].equals("route")) {
            if (args.length < 3) {
               sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/depot route [CODE] [add/remove]");
               return true;
            }

            code = args[1];
            d = TrainCarts.plugin.DepotStore.getFromCode(code);
            if (d == null) {
               sender.sendMessage(ChatColor.RED + "Depot with the code " + args[0] + " was not found.");
               return true;
            }

            MetroLines.MetroLine l;
            PathRoute r;
            if (args[2].equals("add")) {
               if (args.length < 7) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/depot route [CODE] add [route] [offset] [frequency] [length]");
                  return true;
               }

               l = TrainCarts.plugin.lines.getLine(args[3].split(":")[0]);
               if (l == null) {
                  sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[3].split(":")[0] + ChatColor.RED + ".");
                  return true;
               }

               r = l.getRoute(args[3].split(":")[1]);
               if (r == null) {
                  sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[3].split(":")[0] + ChatColor.RED + " in the " + args[1] + " line.");
                  return true;
               }

               String[] o = args[4].split(":");
               if (o.length != 2) {
                  sender.sendMessage(ChatColor.RED + "" + args[4] + " is an invalid spawn offset.");
                  return true;
               }

               long offset = Long.parseLong(o[0]) * 60L + Long.parseLong(o[1]);
               String[] rt = args[5].split(":");
               if (rt.length != 2) {
                  sender.sendMessage(ChatColor.RED + "" + args[5] + " is an invalid spawn rate.");
                  return true;
               }

               if (args[6].split("/").length != 3) {
                  sender.sendMessage(ChatColor.RED + "" + args[6] + " is an train length. Make sure that its X/X/X");
                  return true;
               }

               long rate = Long.parseLong(rt[0]) * 60L + Long.parseLong(rt[1]);
               d.addRoute(r);
               d.addRouteRate(r, new SpawnerRate(offset * 1000L, rate * 1000L, args[3], args[6]));
               sender.sendMessage(ChatColor.GREEN + "Added route " + ChatColor.YELLOW + args[3] + ChatColor.GREEN + " to depot " + ChatColor.YELLOW + d.name + ChatColor.GREEN + " (Frequency: " + ChatColor.YELLOW + "trains every " + rate + " seconds" + ChatColor.GREEN + ").");
               return true;
            }

            if (args[2].equals("remove")) {
               if (args.length < 4) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/depot route [CODE] remove [route]");
                  return true;
               }

               l = TrainCarts.plugin.lines.getLine(args[3].split(":")[0]);
               if (l == null) {
                  sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[3].split(":")[0] + ChatColor.RED + ".");
                  return true;
               }

               r = l.getRoute(args[3].split(":")[1]);
               if (r == null) {
                  sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[3].split(":")[0] + ChatColor.RED + " in the " + args[1] + " line.");
                  return true;
               }

               d.removeRouteRate(r);
               d.removeRoute(r);
               sender.sendMessage(ChatColor.GREEN + "Removed route " + ChatColor.YELLOW + args[3] + ChatColor.GREEN + " from depot " + ChatColor.YELLOW + d.name + ChatColor.GREEN + ".");
               return true;
            }

            return true;
         }

         TextComponent header;
         TextComponent name;
         TextComponent colour;
         if (args[0].equals("info")) {
            if (args.length < 2) {
               sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/depot info [CODE]");
               return true;
            }

            code = args[1];
            d = TrainCarts.plugin.DepotStore.getFromCode(code);
            if (d == null) {
               sender.sendMessage(ChatColor.RED + "Depot with the code " + args[0] + " was not found.");
               return true;
            }

            b = new ArrayList();
            d.routerate.values().forEach((cx) -> {
               long n = LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toInstant().toEpochMilli() - 3600000L;
               if (cx.getNextSpawnTime() != null) {
                  int z = (int)cx.getNextSpawnTime()._timestamp;
                  b.add((z - (int)n) / 1000);
               }

            });
            Collections.sort(b);
            this.msg = (TextComponent[])Arrays.copyOf(this.msg, 0);
            header = new TextComponent(ChatColor.WHITE + "" + ChatColor.BOLD + "= Information of " + d.name + " =\n");
            this.msg = (TextComponent[])append(this.msg, header);
            name = new TextComponent(ChatColor.YELLOW + " - Depot name: " + ChatColor.GREEN + d.name + "\n");
            this.msg = (TextComponent[])append(this.msg, name);
            colour = new TextComponent(ChatColor.YELLOW + " - Depot code: " + ChatColor.GREEN + d.code);
            this.msg = (TextComponent[])append(this.msg, colour);
            String c;
            if (b.size() == 0) {
               c = "§cNO SERVICES";
            } else {
               c = Integer.toString((Integer)b.get(0)) + " seconds";
            }

            TextComponent nt = new TextComponent(ChatColor.YELLOW + "\n - Next train in: " + ChatColor.GREEN + c);
            this.msg = (TextComponent[])append(this.msg, nt);
            TextComponent lanes = new TextComponent(ChatColor.YELLOW + "\n - Depot lanes: " + ChatColor.GREEN);
            this.msg = (TextComponent[])append(this.msg, lanes);
            TextComponent m4 = new TextComponent(ChatColor.YELLOW + ", ");
            if (d.lanes.values().size() < 1) {
               this.msg = (TextComponent[])append(this.msg, new TextComponent(ChatColor.ITALIC + "None"));
            } else {
               this.i = 0;
               d.lanes.values().forEach((lane) -> {
                  if (this.i > 0 && this.i < d.routes.size()) {
                     this.msg = (TextComponent[])append(this.msg, m4);
                  }

                  ++this.i;
                  TextComponent lane2 = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + lane.lane);
                  this.msg = (TextComponent[])append(this.msg, lane2);
               });
            }

            TextComponent routes = new TextComponent(ChatColor.YELLOW + "\n - " + d.routes.size() + " Routes: " + ChatColor.GREEN);
            this.msg = (TextComponent[])append(this.msg, routes);
            if (d.routes.size() < 1) {
               this.msg = (TextComponent[])append(this.msg, new TextComponent(ChatColor.ITALIC + "None"));
            } else {
               this.i = 0;
               d.routes.values().forEach((route) -> {
                  if (this.i > 0 && this.i < d.routes.size()) {
                     this.msg = (TextComponent[])append(this.msg, m4);
                  }

                  ++this.i;
                  TextComponent clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + ((SignActionPlatform)route.stops.get(0)).station.code + "~" + ((SignActionPlatform)route.stops.get(0)).platform + " > " + ((SignActionPlatform)route.stops.get(route.stops.size() - 1)).station.code + "~" + ((SignActionPlatform)route.stops.get(route.stops.size() - 1)).platform);
                  clickable.setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/route info " + route._line.getName() + " " + route.name));
                  TextComponent[] hover = new TextComponent[]{new TextComponent("Name: " + route.name)};
                  clickable.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, hover));
                  this.msg = (TextComponent[])append(this.msg, clickable);
               });
            }

            TextComponent footer = new TextComponent(ChatColor.WHITE + "\n" + ChatColor.BOLD + "=====");
            this.msg = (TextComponent[])append(this.msg, footer);
            sender.spigot().sendMessage(this.msg);
            return true;
         }

         if (args[0].equals("list")) {
            this.msg = (TextComponent[])Arrays.copyOf(this.msg, 0);
            DepotController ds = TrainCarts.plugin.DepotStore;
            TextComponent m1 = new TextComponent(ChatColor.YELLOW + "There's a total of ");
            this.msg = (TextComponent[])append(this.msg, m1);
            TextComponent m2 = new TextComponent(ChatColor.GREEN + "" + ds.depots.values().size());
            this.msg = (TextComponent[])append(this.msg, m2);
            header = new TextComponent(ChatColor.YELLOW + " depots: ");
            this.msg = (TextComponent[])append(this.msg, header);
            name = new TextComponent(ChatColor.YELLOW + ", ");
            colour = new TextComponent(ChatColor.YELLOW + " and ");
            this.i = 0;
            ds.depots.values().forEach((dx) -> {
               if (this.i > 0 && this.i < ds.depots.values().size() - 1) {
                  this.msg = (TextComponent[])append(this.msg, name);
               }

               ++this.i;
               TextComponent clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + dx.name);
               TextComponent[] hover = new TextComponent[]{new TextComponent("Code: " + dx.code)};
               clickable.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, hover));
               clickable.setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/depot info " + dx.code));
               this.msg = (TextComponent[])append(this.msg, clickable);
               if (this.i == ds.depots.values().size() - 1) {
                  this.msg = (TextComponent[])append(this.msg, colour);
               }

            });
            sender.spigot().sendMessage(this.msg);
            return true;
         }
      }

      return false;
   }

   private class TabComplete implements TabCompleter {
      private TabComplete() {
      }

      public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
         ArrayList<String> arguments = new ArrayList();
         ArrayList<String> arguments2 = new ArrayList();
         if (args.length == 1 && sender.isOp()) {
            arguments.add("create");
            arguments.add("list");
            arguments.add("info");
            arguments.add("remove");
            arguments.add("route");
         }

         if (args.length == 2 && (args[0].equals("remove") || args[0].equals("info") || args[0].equals("route")) && sender.isOp()) {
            TrainCarts.plugin.DepotStore.depots.values().forEach((dx) -> {
               arguments.add(dx.code);
            });
         }

         if (args.length == 3 && args[0].equals("route") && sender.isOp()) {
            arguments.add("add");
            arguments.add("remove");
         }

         Depot d;
         if (args.length == 4 && args[0].equals("route") && args[2].equals("add")) {
            if (sender.isOp()) {
               d = TrainCarts.plugin.DepotStore.getFromCode(args[1]);
               if (d != null) {
                  TrainCarts.plugin.lines.getLines().values().forEach((l) -> {
                     l.getRoutes().values().forEach((r) -> {
                        if (!d.routes.values().contains(r)) {
                           arguments.add(l.getName() + ":" + r.name);
                        }

                     });
                  });
               }
            }
         } else if (args.length == 4 && args[0].equals("route") && args[2].equals("remove") && sender.isOp()) {
            d = TrainCarts.plugin.DepotStore.getFromCode(args[1]);
            if (d != null) {
               d.routes.values().forEach((r) -> {
                  arguments.add(r._line.getName() + ":" + r.name);
               });
            }
         }

         if (args.length == 5 && args[0].equals("route") && args[2].equals("add")) {
            if (args[4].length() == 2) {
               arguments.add(args[4] + ":");
            } else if (args[4].length() < 5) {
               arguments.add(args[4] + "0");
               arguments.add(args[4] + "1");
               arguments.add(args[4] + "2");
               arguments.add(args[4] + "3");
               arguments.add(args[4] + "4");
               arguments.add(args[4] + "5");
               if (args[4].length() == 1 || args[4].length() == 4) {
                  arguments.add(args[4] + "6");
                  arguments.add(args[4] + "7");
                  arguments.add(args[4] + "8");
                  arguments.add(args[4] + "9");
               }
            }
         }

         if (args.length == 6 && args[0].equals("route") && args[2].equals("add")) {
            if (args[5].length() == 2) {
               arguments.add(args[5] + ":");
            } else if (args[5].length() < 5) {
               arguments.add(args[5] + "0");
               arguments.add(args[5] + "1");
               arguments.add(args[5] + "2");
               arguments.add(args[5] + "3");
               arguments.add(args[5] + "4");
               arguments.add(args[5] + "5");
               if (args[5].length() == 1 || args[5].length() == 4) {
                  arguments.add(args[5] + "6");
                  arguments.add(args[5] + "7");
                  arguments.add(args[5] + "8");
                  arguments.add(args[5] + "9");
               }
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
