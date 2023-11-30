package com.movies22.cashcraft.tc;

import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.api.MetroLines;
import java.util.Arrays;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

public class TCListener implements Listener {
   TrainCarts plugin;
   private TextComponent[] msg = new TextComponent[0];

   public TCListener(TrainCarts plugin) {
      this.plugin = plugin;
   }

   static <T> T[] append(T[] arr, T lastElement) {
      int N = arr.length;
      arr = Arrays.copyOf(arr, N + 1);
      arr[N] = lastElement;
      return arr;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onVehicleDamage(VehicleDamageEvent event) {
      Entity e = event.getVehicle();
      if (TrainCarts.plugin.MemberController.getFromEntity(e) != null) {
         event.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player p = event.getPlayer();
      Entity e = p.getVehicle();
      if (e != null) {
         if (TrainCarts.plugin.MemberController.getFromEntity(e) != null) {
            e.eject();
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
      event.setCancelled(true);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      Player p = e.getPlayer();
      if (e.getClickedBlock().getState() instanceof Sign && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null) {
         Sign sign;
         MetroLines.MetroLine a;
         PathNode b;
         if (e.getItem().getType().equals(Material.DEBUG_STICK)) {
            sign = (Sign)e.getClickedBlock().getState();
            a = TrainCarts.plugin.lines.getLine("#GLOBAL");
            b = a.getNode(sign.getLocation().clone().add(0.5D, 2.0D, 0.5D));
            if (b == null) {
               e.getPlayer().sendMessage(ChatColor.RED + "Node is invalid!");
               e.setCancelled(true);
               return;
            }

            this.msg = (TextComponent[])Arrays.copyOf(this.msg, 0);
            TextComponent header = new TextComponent(ChatColor.WHITE + "" + ChatColor.BOLD + "= Destinations from " + b.getLocationStr() + " =\n");
            this.msg = (TextComponent[])append(this.msg, header);
            b.connections.forEach((con) -> {
               TextComponent connection = new TextComponent(ChatColor.GREEN + " - " + ChatColor.BOLD + "" + ChatColor.WHITE + con.facing + ChatColor.GREEN + " >> " + ChatColor.BOLD + "" + ChatColor.WHITE + con.opositeFacing + ChatColor.GREEN + " - " + ChatColor.YELLOW + con.getEndNode().getLocationStr());
               this.msg = (TextComponent[])append(this.msg, connection);
            });
            e.getPlayer().spigot().sendMessage(this.msg);
            e.setCancelled(true);
            return;
         }

         sign = (Sign)e.getClickedBlock().getState();
         if (sign.getLine(0).startsWith("t:") && (p.hasPermission("traincarts.build") || p.isOp())) {
            TrainCarts.plugin.SignStore.addSign(sign);
            a = TrainCarts.plugin.lines.getLine("#GLOBAL");
            b = a.createNode(sign.getLocation().clone().add(0.5D, 2.0D, 0.5D));
            if (b.getAction() == null) {
               a.deleteNode(b);
               sign.getLocation().getBlock().setType(Material.AIR);
               p.sendMessage(ChatColor.RED + sign.getLine(0) + " is a non valid TC sign.");
               return;
            }

            b.findNeighbours();
            b.getAction().handleBuild(p);
         }
      }

   }
}
