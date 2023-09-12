package com.movies22.cashcraft.tc;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;

public class TCListener implements Listener {
	TrainCarts plugin;

	public TCListener(TrainCarts plugin) {
		this.plugin = plugin;
	}
	
	static <T> T[] append(T[] arr, T lastElement) {
		final int N = arr.length;
		arr = java.util.Arrays.copyOf(arr, N + 1);
		arr[N] = lastElement;
		return arr;
	}
	
	//MINECART attack (Cancelled if TC entity)
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleDamage(VehicleDamageEvent event) {
        Entity e = (Entity) event.getVehicle();
        if(TrainCarts.plugin.MemberStore.getFromEntity(e) != null) {
        	event.setCancelled(true);
        }
    }
	
	//Plaer disconnect
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = (Player) event.getPlayer();
		Entity e = (Entity) p.getVehicle();
		if(e == null) {
			return;
		}
        if(TrainCarts.plugin.MemberStore.getFromEntity(e) != null) {
        	e.eject();
        	return;
        }
	}
	
	//MINECART collision (Cancelled if TC entity)
    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
    	event.setCancelled(true);
    }
	
    //Sign right click (Convert to node if TC)
    private TextComponent[] msg = {};
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
    	Player p = (Player) e.getPlayer();
        if(e.getClickedBlock().getState() instanceof Sign){
            if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null){
            	if(e.getItem().getType().equals(Material.DEBUG_STICK)) {
            		Sign sign = (Sign) e.getClickedBlock().getState();
            		MetroLine a = TrainCarts.plugin.lines.getLine("#GLOBAL");
                	PathNode b = a.getNode(sign.getLocation().clone().add(0.5, 2, 0.5));
                	if(b == null) {
                		e.getPlayer().sendMessage(ChatColor.RED + "Node is invalid!");
                		e.setCancelled(true);
                		return;
                	}
                	msg = java.util.Arrays.copyOf(msg, 0);
                	TextComponent header = new TextComponent(ChatColor.WHITE + "" + ChatColor.BOLD + "= Destinations from " + b.getLocationStr() + " =\n");
    				msg = append(msg, header);
    				b.connections.forEach(con -> {
    					TextComponent connection = new TextComponent(ChatColor.GREEN + " - " + ChatColor.BOLD + "" + ChatColor.WHITE + con.facing + ChatColor.GREEN + " >> " + ChatColor.BOLD + "" + ChatColor.WHITE + con.opositeFacing + ChatColor.GREEN + " - " + ChatColor.YELLOW + con.getEndNode().getLocationStr());
        				msg = append(msg, connection);
    				});
    				e.getPlayer().spigot().sendMessage(msg);
            		e.setCancelled(true);
            		return;
            	}
                Sign sign = (Sign) e.getClickedBlock().getState();
                if(sign.getLine(0).startsWith("t:") && (p.hasPermission("traincarts.build") || p.isOp())) {
                	TrainCarts.plugin.SignStore.addSign(sign);
                	MetroLine a = TrainCarts.plugin.lines.getLine("#GLOBAL");
                	PathNode b = a.createNode(sign.getLocation().clone().add(0.5, 2, 0.5));
                	if(b.getAction() == null) {
                		a.deleteNode(b);
                		sign.getLocation().getBlock().setType(Material.AIR);
                		p.sendMessage(ChatColor.RED + sign.getLine(0) + " is a non valid TC sign.");
                		return;
                	}
                	b.findNeighbours();
                	b.getAction().handleBuild(p);
                };
            }
        }
    }
}
