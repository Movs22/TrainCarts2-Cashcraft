package com.movies22.cashcraft.tc.commands;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.Station;
import com.movies22.cashcraft.tc.controller.StationStore;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class StationCommand implements CommandExecutor {
   private TextComponent[] msg = new TextComponent[0];
   private int i;
   private Boolean stationCheck;

   public StationCommand(TrainCarts tc) {
      tc.getCommand("station").setExecutor(this);
      tc.getCommand("station").setTabCompleter(new StationCommand.TabComplete());
   }

   static <T> T[] append(T[] arr, T lastElement) {
      int N = arr.length;
      arr = Arrays.copyOf(arr, N + 1);
      arr[N] = lastElement;
      return arr;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (args.length < 1) {
         return false;
      } else {
         String code;
         Station s;
         if (sender.isOp()) {
            List a;
            String osi;
            ArrayList b;
            if (args[0].equals("create")) {
               if (args.length < 3) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/station create [CODE] [Station Name]");
                  return true;
               }

               code = args[1];
               a = Arrays.asList(args);
               b = new ArrayList(a);
               b.remove(0);
               b.remove(0);
               osi = String.join(" ", b);
               TrainCarts.plugin.StationStore.createStation(code, osi, osi);
               sender.sendMessage(ChatColor.GREEN + "Created station " + ChatColor.YELLOW + osi + ChatColor.GREEN + " with the station code " + ChatColor.YELLOW + code + ChatColor.GREEN + ".");
               return true;
            }

            if (args[0].equals("display")) {
               if (args.length < 3) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/station display [CODE] [Station Display Name]");
                  return true;
               }

               code = args[1];
               a = Arrays.asList(args);
               b = new ArrayList(a);
               b.remove(0);
               b.remove(0);
               Station s = TrainCarts.plugin.StationStore.getFromCode(code);
               String name = String.join(" ", b);
               if (name.equals("reset")) {
                  s.displayName = s.name;
                  sender.sendMessage(ChatColor.GREEN + "Reseted the display name of " + ChatColor.YELLOW + s.name + ChatColor.GREEN + ChatColor.GREEN + ".");
                  return true;
               }

               s.displayName = name;
               sender.sendMessage(ChatColor.GREEN + "Changed the display name of " + ChatColor.YELLOW + s.name + ChatColor.GREEN + " to " + ChatColor.YELLOW + s.displayName + ChatColor.GREEN + ".");
               return true;
            }

            if (args[0].equals("headcode")) {
               if (args.length < 2) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/station headcode [CODE] <headcode/reset>");
                  return true;
               }

               code = args[1];
               s = TrainCarts.plugin.StationStore.getFromCode(code);
               if (args.length == 2) {
                  sender.sendMessage(ChatColor.GREEN + "The current headcode letter of " + ChatColor.YELLOW + s.name + ChatColor.GREEN + " is " + ChatColor.YELLOW + s.headcode + ChatColor.GREEN + ".");
                  return true;
               }

               if (args[2].equals("reset")) {
                  s.headcode = null;
                  s.canTerminate = false;
                  sender.sendMessage(ChatColor.GREEN + "Reseted the headcode letter of " + ChatColor.YELLOW + s.name + ChatColor.GREEN + ChatColor.GREEN + ".");
                  return true;
               }

               this.stationCheck = false;
               TrainCarts.plugin.StationStore.Stations.values().forEach((station) -> {
                  if (station.headcode != null && station.headcode.equals(args[2])) {
                     sender.sendMessage(ChatColor.RED + "The headcode letter " + ChatColor.YELLOW + args[2] + ChatColor.RED + " is already in use by " + ChatColor.YELLOW + station.name + ChatColor.RED + ".");
                     this.stationCheck = true;
                  }
               });
               if (this.stationCheck) {
                  return true;
               }

               s.headcode = args[2];
               s.canTerminate = true;
               sender.sendMessage(ChatColor.GREEN + "The new headcode letter of " + ChatColor.YELLOW + s.name + ChatColor.GREEN + " is " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + ".");
               return true;
            }

            if (args[0].equals("close") && sender.isOp()) {
               if (args.length < 3) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/station close [CODE] <close/open>");
                  return true;
               }

               code = args[1];
               s = TrainCarts.plugin.StationStore.getFromCode(code);
               if (args[2].equals("close")) {
                  s.closed = true;
                  sender.sendMessage(ChatColor.GREEN + "Trains will no longer stop at " + ChatColor.YELLOW + s.name + ChatColor.GREEN + ChatColor.GREEN + ".");
                  return true;
               }

               s.closed = false;
               sender.sendMessage(ChatColor.GREEN + "Trains will now stop at " + ChatColor.YELLOW + s.name + ChatColor.GREEN + ChatColor.GREEN + ".");
               return true;
            }

            if (args[0].equals("remove")) {
               if (args.length < 2) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/station remove [CODE]");
                  return true;
               }

               code = args[1];
               s = TrainCarts.plugin.StationStore.getFromCode(code);
               if (s == null) {
                  sender.sendMessage(ChatColor.RED + "Station with the code " + args[1] + " was not found.");
                  return true;
               }

               TrainCarts.plugin.StationStore.removeStation(code);
               sender.sendMessage(ChatColor.GREEN + "Deleted station " + ChatColor.YELLOW + s.name + ChatColor.GREEN + " with the station code " + ChatColor.YELLOW + code + ChatColor.GREEN + ".");
               return true;
            }

            if (args[0].equals("osi")) {
               if (args.length < 4) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/station osi [CODE] [metro/heavy] [OSI String]");
                  return true;
               }

               code = args[1];
               s = TrainCarts.plugin.StationStore.getFromCode(code);
               if (s == null) {
                  sender.sendMessage(ChatColor.RED + "Station with the code " + args[1] + " was not found.");
                  return true;
               }

               if (args[2].equals("station")) {
                  String osi = args[3];
                  if (osi.equals("none")) {
                     osi = "";
                  }

                  s.station = osi;
                  sender.sendMessage(ChatColor.GREEN + "Station " + ChatColor.YELLOW + s.name + ChatColor.GREEN + " was connected to station " + ChatColor.YELLOW + osi + ChatColor.GREEN + " .");
               } else {
                  Boolean a = args[2].equals("heavy");
                  osi = args[3];
                  if (osi.equals("none")) {
                     osi = "";
                  }

                  s.setOsi(osi, a);
                  sender.sendMessage(ChatColor.GREEN + "Station " + ChatColor.YELLOW + s.name + ChatColor.GREEN + " now has the following OSI string: " + ChatColor.YELLOW + osi + ChatColor.GREEN + " for " + (a ? "Heavy Rail" : "Metro") + " services.");
               }

               return true;
            }

            if (args[0].equals("teleport")) {
               if (args.length < 3) {
                  sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/station teleport [CODE] [PLATFORM]");
                  return true;
               }

               code = args[1];
               s = TrainCarts.plugin.StationStore.getFromCode(code);
               if (s == null) {
                  sender.sendMessage(ChatColor.RED + "Station with the code " + args[1] + " was not found.");
                  return true;
               }

               SignActionPlatform plat = (SignActionPlatform)s.platforms.get(args[2]);
               if (plat == null) {
                  sender.sendMessage(ChatColor.RED + "Platform " + args[2] + " in station " + s.name + " was not found.");
                  return true;
               }

               sender.sendMessage(ChatColor.GREEN + "Teleporting to " + ChatColor.YELLOW + s.name + ChatColor.GREEN + "~" + ChatColor.YELLOW + args[2] + ChatColor.GREEN + ".");
               Player p = (Player)sender;
               p.teleport(plat.node.loc);
               return true;
            }
         }

         TextComponent header;
         TextComponent name;
         TextComponent code;
         TextComponent terminus;
         if (args[0].equals("list")) {
            this.msg = (TextComponent[])Arrays.copyOf(this.msg, 0);
            StationStore ss = TrainCarts.plugin.StationStore;
            TextComponent m1 = new TextComponent(ChatColor.YELLOW + "There's a total of ");
            this.msg = (TextComponent[])append(this.msg, m1);
            header = new TextComponent(ChatColor.GREEN + "" + ss.Stations.values().size());
            this.msg = (TextComponent[])append(this.msg, header);
            name = new TextComponent(ChatColor.YELLOW + " stations: ");
            this.msg = (TextComponent[])append(this.msg, name);
            code = new TextComponent(ChatColor.YELLOW + ", ");
            terminus = new TextComponent(ChatColor.YELLOW + " and ");
            this.i = 0;
            ss.Stations.values().forEach((sx) -> {
               if (this.i > 0 && this.i < ss.Stations.values().size() - 1) {
                  this.msg = (TextComponent[])append(this.msg, code);
               }

               ++this.i;
               TextComponent clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + sx.name);
               TextComponent[] hover = new TextComponent[]{new TextComponent("Code: " + sx.code)};
               clickable.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, hover));
               clickable.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, "/station info " + sx.code));
               this.msg = (TextComponent[])append(this.msg, clickable);
               if (this.i == ss.Stations.values().size() - 1) {
                  this.msg = (TextComponent[])append(this.msg, terminus);
               }

            });
            sender.spigot().sendMessage(this.msg);
            return true;
         } else if (args[0].equals("info")) {
            this.msg = (TextComponent[])Arrays.copyOf(this.msg, 0);
            if (args.length < 2) {
               sender.sendMessage(ChatColor.RED + "Missing arguments: " + ChatColor.WHITE + "/station info [CODE]");
               return true;
            } else {
               code = args[1];
               s = TrainCarts.plugin.StationStore.getFromCode(code);
               if (s == null) {
                  sender.sendMessage(ChatColor.RED + "Station with the code " + args[1] + " was not found.");
                  return true;
               } else {
                  header = new TextComponent(ChatColor.WHITE + "" + ChatColor.BOLD + "===== Information of " + s.name + " =====\n");
                  this.msg = (TextComponent[])append(this.msg, header);
                  name = new TextComponent(ChatColor.YELLOW + " - Station name: " + ChatColor.GREEN + s.name + "\n");
                  this.msg = (TextComponent[])append(this.msg, name);
                  code = new TextComponent(ChatColor.YELLOW + " - Station code: " + ChatColor.GREEN + s.code + "\n");
                  this.msg = (TextComponent[])append(this.msg, code);
                  terminus = new TextComponent(ChatColor.YELLOW + " - Terminus: " + ChatColor.GREEN + (s.canTerminate ? "YES" : ChatColor.DARK_RED + "NO") + "\n");
                  this.msg = (TextComponent[])append(this.msg, terminus);
                  TextComponent headcode = new TextComponent(ChatColor.YELLOW + " - Headcode letter: " + ChatColor.GREEN + (s.headcode != null ? s.headcode : ChatColor.DARK_RED + "" + ChatColor.ITALIC + "none") + "\n");
                  this.msg = (TextComponent[])append(this.msg, headcode);
                  TextComponent plats = new TextComponent(ChatColor.YELLOW + " - " + s.platforms.size() + " Platforms: " + ChatColor.GREEN);
                  this.msg = (TextComponent[])append(this.msg, plats);
                  TextComponent trains;
                  if (s.platforms.size() < 1) {
                     this.msg = (TextComponent[])append(this.msg, new TextComponent(ChatColor.ITALIC + "None"));
                  } else {
                     trains = new TextComponent(ChatColor.YELLOW + ", ");
                     this.i = 0;
                     s.platforms.values().forEach((platx) -> {
                        if (this.i > 0 && this.i < s.platforms.values().size()) {
                           this.msg = (TextComponent[])append(this.msg, trains);
                        }

                        ++this.i;
                        TextComponent clickable = new TextComponent(ChatColor.GREEN + "" + ChatColor.ITALIC + platx.platform + "");
                        clickable.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, "/station teleport " + s.code + " " + platx.platform));
                        this.msg = (TextComponent[])append(this.msg, clickable);
                     });
                  }

                  trains = new TextComponent(ChatColor.YELLOW + "\n - " + s.groups.size() + " Osi: " + ChatColor.GREEN + s.osi + "§e/§a" + s.hosi + "§e/§a" + s.station + "§e.");
                  this.msg = (TextComponent[])append(this.msg, trains);
                  TextComponent lines = new TextComponent(ChatColor.YELLOW + "\n - " + s.lines.size() + " Lines: " + ChatColor.GREEN);
                  this.msg = (TextComponent[])append(this.msg, lines);
                  TextComponent footer;
                  if (s.lines.size() < 1) {
                     this.msg = (TextComponent[])append(this.msg, new TextComponent(ChatColor.ITALIC + "None"));
                  } else {
                     this.i = 0;
                     footer = new TextComponent(ChatColor.YELLOW + ", ");
                     s.lines.forEach((line) -> {
                        if (this.i > 0 && this.i < s.lines.size()) {
                           this.msg = (TextComponent[])append(this.msg, footer);
                        }

                        ++this.i;
                        TextComponent clickable = new TextComponent(ChatColor.of(line.getColour()) + "" + ChatColor.ITALIC + line.getName());
                        clickable.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, "/say " + line.getName()));
                        this.msg = (TextComponent[])append(this.msg, clickable);
                     });
                  }

                  footer = new TextComponent(ChatColor.WHITE + "\n" + ChatColor.BOLD + "=====");
                  this.msg = (TextComponent[])append(this.msg, footer);
                  sender.spigot().sendMessage(this.msg);
                  return true;
               }
            }
         } else {
            return false;
         }
      }
   }

   private class TabComplete implements TabCompleter {
      private TabComplete() {
      }

      public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
         ArrayList<String> arguments = new ArrayList();
         ArrayList<String> arguments2 = new ArrayList();
         if (args.length == 1) {
            arguments.add("list");
            arguments.add("info");
            if (sender.isOp()) {
               arguments.add("create");
               arguments.add("remove");
               arguments.add("teleport");
               arguments.add("headcode");
               arguments.add("display");
               arguments.add("close");
               arguments.add("osi");
            }
         } else if (args.length == 2 && (args[0].equals("info") || (args[0].equals("remove") || args[0].equals("osi") || args[0].equals("teleport") || args[0].equals("headcode") || args[0].equals("display") || args[0].equals("close")) && sender.isOp())) {
            TrainCarts.plugin.StationStore.Stations.values().forEach((s) -> {
               arguments.add(s.code);
            });
         } else if (args.length == 3 && args[0].equals("osi") && sender.isOp()) {
            arguments.add("metro");
            arguments.add("heavy");
            arguments.add("station");
         } else if (args.length == 3 && args[0].equals("close") && sender.isOp()) {
            arguments.add("close");
            arguments.add("open");
         } else if (args.length == 3 && sender.isOp() && (args[0].equals("teleport") || args[0].equals("info"))) {
            Station ax = TrainCarts.plugin.StationStore.getFromCode(args[1]);
            if (ax != null) {
               ax.platforms.values().forEach((p) -> {
                  arguments.add(p.platform);
               });
            }
         } else if (args.length == 3 && sender.isOp() && args[0].equals("headcode")) {
            ArrayList<String> a = new ArrayList(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"));
            ArrayList<String> b = new ArrayList(a);
            TrainCarts.plugin.StationStore.Stations.values().forEach((s) -> {
               if (a.contains(s.headcode)) {
                  b.remove(s.headcode);
               }

            });
            b.forEach((c) -> {
               arguments.add(c);
            });
            arguments.add("reset");
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
