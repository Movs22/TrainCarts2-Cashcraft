package com.movies22.cashcraft.tc.commands;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.PathFinding.PathOperation;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.api.MetroLines;
import com.movies22.cashcraft.tc.api.Station;
import com.movies22.cashcraft.tc.controller.StationStore;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class RouteCommand implements CommandExecutor {
   private TextComponent[] msg = new TextComponent[0];
   private int i;

   public RouteCommand(TrainCarts tc) {
      tc.getCommand("route").setExecutor(this);
      tc.getCommand("route").setTabCompleter(new RouteCommand.TabComplete());
   }

   static <T> T[] append(T[] arr, T lastElement) {
      int N = arr.length;
      arr = Arrays.copyOf(arr, N + 1);
      arr[N] = lastElement;
      return arr;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (sender.isOp()) {
         MetroLines.MetroLine l;
         PathRoute r;
         if (args[0].equals("reroute")) {
            if (args.length < 3) {
               sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route reroute [LINE] [ROUTE]");
               return true;
            } else {
               l = TrainCarts.plugin.lines.getLine(args[1]);
               if (l == null) {
                  sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1] + ChatColor.RED + ".");
                  return true;
               } else {
                  r = l.getRoute(args[2]);
                  if (r == null) {
                     sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[2] + ChatColor.RED + " in the " + args[1] + " line.");
                     return true;
                  } else {
                     r.calculate();
                     sender.sendMessage(ChatColor.GREEN + "Rerouted " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + ".");
                     return true;
                  }
               }
            }
         } else {
            TextComponent m3a;
            TextComponent header;
            TextComponent m1;
            TextComponent m2;
            TextComponent m3;
            if (args[0].equals("info")) {
               if (args.length < 2) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route info [ROUTE]");
                  return true;
               } else {
                  l = TrainCarts.plugin.lines.getLine(args[1].split(":")[0]);
                  if (l == null) {
                     sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1].split(":")[0] + ChatColor.RED + ".");
                     return true;
                  } else {
                     r = l.getRoute(args[1].split(":")[1]);
                     if (r == null) {
                        sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[1].split(":")[1] + ChatColor.RED + " in the " + args[1] + " line.");
                        return true;
                     } else {
                        PathRoute r2 = null;
                        if (r.reverse != null && !r.reverse.equals("ECS")) {
                           r2 = TrainCarts.plugin.lines.getLine(r.reverse.split(":")[0]).getRoute(r.reverse.split(":")[1]);
                        }

                        this.msg = (TextComponent[])Arrays.copyOf(this.msg, 0);
                        header = new TextComponent(ChatColor.WHITE + "" + ChatColor.BOLD + "===== Information of " + r.name + " =====\n");
                        this.msg = (TextComponent[])append(this.msg, header);
                        m1 = new TextComponent(ChatColor.YELLOW + " - Name: " + ChatColor.GREEN + r.name);
                        this.msg = (TextComponent[])append(this.msg, m1);
                        m2 = new TextComponent(ChatColor.YELLOW + "\n - Line: " + ChatColor.GREEN + r._line.getName());
                        this.msg = (TextComponent[])append(this.msg, m2);
                        m3 = new TextComponent(ChatColor.YELLOW + "\n - Headcode: " + ChatColor.GREEN + "2" + ((SignActionPlatform)r.stops.get(r.stops.size() - 1)).station.headcode + "XX");
                        this.msg = (TextComponent[])append(this.msg, m3);
                        m3a = new TextComponent(ChatColor.YELLOW + "\n - Reverse: " + (r.reverse == null ? ChatColor.DARK_RED + "NO" : (r.reverse.equals("ECS") ? ChatColor.WHITE + "ECS service" : ChatColor.GREEN + "YES" + ChatColor.YELLOW + " (" + ChatColor.GREEN + ((SignActionPlatform)r2.stops.get(0)).station.code + ChatColor.YELLOW + "~" + ChatColor.GREEN + ((SignActionPlatform)r2.stops.get(0)).platform + ChatColor.YELLOW + " > " + ChatColor.GREEN + ((SignActionPlatform)r2.stops.get(r2.stops.size() - 1)).station.code + ChatColor.YELLOW + "~" + ChatColor.GREEN + ((SignActionPlatform)r2.stops.get(r2.stops.size() - 1)).platform + ChatColor.YELLOW + " as " + ChatColor.GREEN + "2" + ((SignActionPlatform)r2.stops.get(r2.stops.size() - 1)).station.headcode + "XX" + ChatColor.YELLOW + ")")));
                        this.msg = (TextComponent[])append(this.msg, m3a);
                        TextComponent m3b = new TextComponent(ChatColor.YELLOW + "\n - Stations (" + r.stops.size() + "): ");
                        this.msg = (TextComponent[])append(this.msg, m3b);
                        TextComponent m4 = new TextComponent(ChatColor.YELLOW + ", ");
                        TextComponent m5 = new TextComponent(ChatColor.YELLOW + " and ");
                        this.i = 0;
                        r.stops.forEach((sx) -> {
                           if (this.i > 0 && this.i < r.stops.size() - 1) {
                              this.msg = (TextComponent[])append(this.msg, m4);
                           }

                           ++this.i;
                           TextComponent clickable;
                           if (sx != null && sx.station != null && sx.station.name != null) {
                              clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + sx.station.name + "~" + sx.platform);
                           } else {
                              clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + "ERROR~" + sx.platform);
                           }

                           TextComponent[] hover = new TextComponent[]{new TextComponent("Code: " + sx.station.code)};
                           clickable.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, hover));
                           clickable.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, "/station info " + sx.station.code));
                           this.msg = (TextComponent[])append(this.msg, clickable);
                           if (this.i == r.stops.size() - 1) {
                              this.msg = (TextComponent[])append(this.msg, m5);
                           }

                        });
                        Location l1 = (Location)((PathOperation)r.route.get(r.route.size() - 1)).locs.get(((PathOperation)r.route.get(r.route.size() - 1)).locs.size() - 1);
                        TextComponent m3c = new TextComponent(ChatColor.YELLOW + "\n - Last location: " + l1.getBlockX() + "/" + l1.getBlockY() + "/" + l1.getBlockZ());
                        this.msg = (TextComponent[])append(this.msg, m3c);
                        TextComponent footer = new TextComponent(ChatColor.WHITE + "\n" + ChatColor.BOLD + "=====");
                        this.msg = (TextComponent[])append(this.msg, footer);
                        sender.spigot().sendMessage(this.msg);
                        return true;
                     }
                  }
               }
            } else if (args[0].equals("list")) {
               if (args.length < 2) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route list [LINE]");
                  return true;
               } else {
                  l = TrainCarts.plugin.lines.getLine(args[1]);
                  if (l == null) {
                     sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1] + ChatColor.RED + ".");
                     return true;
                  } else {
                     this.msg = (TextComponent[])Arrays.copyOf(this.msg, 0);
                     TextComponent m1 = new TextComponent(ChatColor.YELLOW + "There's a total of ");
                     this.msg = (TextComponent[])append(this.msg, m1);
                     TextComponent m2 = new TextComponent(ChatColor.GREEN + "" + l.getRoutes().size());
                     this.msg = (TextComponent[])append(this.msg, m2);
                     header = new TextComponent(ChatColor.YELLOW + " routes in ");
                     this.msg = (TextComponent[])append(this.msg, header);
                     m1 = new TextComponent(ChatColor.GREEN + "" + l.getName());
                     this.msg = (TextComponent[])append(this.msg, m1);
                     m2 = new TextComponent(ChatColor.YELLOW + ": ");
                     this.msg = (TextComponent[])append(this.msg, m2);
                     m3 = new TextComponent(ChatColor.YELLOW + ", ");
                     m3a = new TextComponent(ChatColor.YELLOW + " and ");
                     this.i = 0;
                     l.getRoutes().values().forEach((route) -> {
                        if (this.i > 0 && this.i < l.getRoutes().size() - 1) {
                           this.msg = (TextComponent[])append(this.msg, m3);
                        }

                        ++this.i;
                        TextComponent clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + route.name);
                        TextComponent[] hover = new TextComponent[]{new TextComponent("Connections: " + route.route.size())};
                        clickable.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, hover));
                        this.msg = (TextComponent[])append(this.msg, clickable);
                        if (this.i == l.getRoutes().size() - 1) {
                           this.msg = (TextComponent[])append(this.msg, m3a);
                        }

                     });
                     sender.spigot().sendMessage(this.msg);
                     return true;
                  }
               }
            } else if (args[0].equals("create")) {
               if (args.length < 5) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route create [LINE] [ROUTE] [Start~X] [Station1~X] (...) [End~X]");
                  return true;
               } else {
                  l = TrainCarts.plugin.lines.getLine(args[1]);
                  if (l == null) {
                     sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1] + ChatColor.RED + ".");
                     return true;
                  } else {
                     r = l.getRoute(args[2]);
                     if (r != null) {
                        sender.sendMessage(ChatColor.RED + "There's already a route named " + ChatColor.YELLOW + args[2] + ChatColor.RED + " in the " + args[1] + " line.");
                        return true;
                     } else {
                        StationStore s = TrainCarts.plugin.StationStore;
                        List<PathNode> nodes = new ArrayList();

                        for(int i = 3; i < args.length; ++i) {
                           Station station = s.getFromCode(args[i].split("~")[0]);
                           if (station == null) {
                              sender.sendMessage(ChatColor.RED + "Couldn't find the station/platform " + ChatColor.YELLOW + args[i] + ChatColor.RED + ".");
                              return true;
                           }

                           if (station.platforms.get(args[i].split("~")[1]) == null) {
                              sender.sendMessage(ChatColor.RED + "Couldn't find the platform " + ChatColor.YELLOW + args[i].split("~")[1] + ChatColor.RED + " at " + args[i] + ".");
                              return true;
                           }

                           PathNode stationN = ((SignActionPlatform)station.platforms.get(args[i].split("~")[1])).node;
                           if (stationN == null) {
                              sender.sendMessage(ChatColor.RED + "Couldn't find the platform " + ChatColor.YELLOW + args[i].split("~")[1] + ChatColor.RED + " at " + args[i] + ".");
                              return true;
                           }

                           nodes.add(stationN);
                        }

                        if (nodes.size() < 2) {
                           sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route create [LINE] [ROUTE] [Start~X] [Station1~X] (...) [End~X]");
                           return true;
                        } else {
                           r = new PathRoute(args[2], nodes, l);
                           l.addRoute(r, args[2]);
                           sender.sendMessage(ChatColor.GREEN + "Created route " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + " on the " + args[1] + " line with " + ChatColor.YELLOW + r.route.size() + ChatColor.GREEN + " connections.");
                           return true;
                        }
                     }
                  }
               }
            } else if (args[0].equals("remove")) {
               if (args.length < 3) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route remove [LINE] [ROUTE]");
                  return true;
               } else {
                  l = TrainCarts.plugin.lines.getLine(args[1]);
                  if (l == null) {
                     sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1] + ChatColor.RED + ".");
                     return true;
                  } else {
                     r = l.getRoute(args[2]);
                     if (r == null) {
                        sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[2] + ChatColor.RED + " in the " + args[1] + " line.");
                        return true;
                     } else {
                        l.deleteRoute(args[2]);
                        sender.sendMessage(ChatColor.GREEN + "Deleted route " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + " on the " + args[1] + " line.");
                        return true;
                     }
                  }
               }
            } else if (args[0].equals("reverse")) {
               if (args.length < 4) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/route reverse [LINE] [ROUTE] [ROUTE]");
                  return true;
               } else {
                  l = TrainCarts.plugin.lines.getLine(args[1]);
                  if (l == null) {
                     sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[1] + ChatColor.RED + ".");
                     return true;
                  } else {
                     r = l.getRoute(args[2]);
                     if (r == null) {
                        sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[2] + ChatColor.RED + " in the " + args[1] + " line.");
                        return true;
                     } else if (args[3].equals("none")) {
                        r.reverse = null;
                        sender.sendMessage(ChatColor.GREEN + "Trains will" + ChatColor.RED + " be destroyed " + ChatColor.GREEN + "after finishing route " + args[2]);
                        return true;
                     } else if (args[3].equals("ecs")) {
                        r.reverse = "ECS";
                        sender.sendMessage(ChatColor.GREEN + "Trains will" + ChatColor.WHITE + " run a ECS service back to the depot " + ChatColor.GREEN + "after finishing route " + args[2]);
                        return true;
                     } else {
                        MetroLines.MetroLine l2 = TrainCarts.plugin.lines.getLine(args[3].split(":")[0]);
                        if (l2 == null) {
                           sender.sendMessage(ChatColor.RED + "Couldn't find a metro line named " + ChatColor.YELLOW + args[3].split(":")[0] + ChatColor.RED + ".");
                           return true;
                        } else {
                           PathRoute r2 = l2.getRoute(args[3].split(":")[1]);
                           if (r2 == null) {
                              sender.sendMessage(ChatColor.RED + "Couldn't find a route named " + ChatColor.YELLOW + args[3] + ChatColor.RED + " in the " + args[1] + " line.");
                              return true;
                           } else {
                              r.reverse = args[3];
                              sender.sendMessage(ChatColor.GREEN + "Trains will" + ChatColor.YELLOW + " run the " + r2.name + " service " + ChatColor.GREEN + "after finishing route " + args[2]);
                              return true;
                           }
                        }
                     }
                  }
               }
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private class TabComplete implements TabCompleter {
      private TabComplete() {
      }

      public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
         ArrayList<String> arguments = new ArrayList();
         ArrayList<String> arguments2 = new ArrayList();
         if (args.length == 1 && sender.isOp()) {
            arguments.add("create");
            arguments.add("remove");
            arguments.add("list");
            arguments.add("reroute");
            arguments.add("info");
            arguments.add("reverse");
         } else if (args.length != 2 || !sender.isOp() || !args[0].equals("create") && !args[0].equals("reroute") && !args[0].equals("remove") && !args[0].equals("reverse") && !args[0].equals("list")) {
            if (args.length == 3 && sender.isOp() && (args[0].equals("remove") || args[0].equals("reroute") || args[0].equals("reverse"))) {
               MetroLines.MetroLine lx = TrainCarts.plugin.lines.getLine(args[1]);
               if (lx != null) {
                  lx.getRoutes().keySet().forEach((s) -> {
                     arguments.add(s);
                  });
               }
            } else if (args.length > 3 && sender.isOp() && args[0].equals("create")) {
               HashMap<String, Station> ss = TrainCarts.plugin.StationStore.Stations;
               MetroLines.MetroLine l = TrainCarts.plugin.lines.getLine(args[1]);
               if (l != null) {
                  ss.values().forEach((s) -> {
                     s.platforms.values().forEach((plat) -> {
                        if (plat.node.line.equals(l) || args[1] == "#GLOBAL") {
                           arguments.add(s.code + "~" + plat.platform);
                        }

                     });
                  });
               }
            } else if (args.length == 4 && args[0].equals("reverse")) {
               TrainCarts.plugin.lines.getLines().values().forEach((lxx) -> {
                  lxx.getRoutes().values().forEach((r) -> {
                     arguments.add(lxx.getName() + ":" + r.name);
                  });
               });
               arguments.add("none");
               arguments.add("ecs");
            }
         } else {
            TrainCarts.plugin.lines.getLines().values().forEach((line) -> {
               arguments.add(line.getName());
            });
         }

         if (args.length == 2 && sender.isOp() && args[0].equals("info")) {
            TrainCarts.plugin.lines.getLines().values().forEach((lxx) -> {
               lxx.getRoutes().values().forEach((r) -> {
                  arguments.add(lxx.getName() + ":" + r.name);
               });
            });
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
