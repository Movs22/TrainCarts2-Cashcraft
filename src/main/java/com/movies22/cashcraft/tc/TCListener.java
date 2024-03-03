package com.movies22.cashcraft.tc;

import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;
import com.movies22.cashcraft.tc.pathFinding.PathNode;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

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
        if(TrainCarts.plugin.MemberController.getFromEntity(e) != null) {
        	event.setCancelled(true);
        }
    }
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent event) {
		if(event.getMessage().equals("Cancel ") && TrainCarts.plugin.playerProgress.get(event.getPlayer().getName()) != null) {
			TrainCarts.plugin.playerProgress.remove(event.getPlayer().getName());
			event.getPlayer().sendMessage("§cRoute creation cancelled.");
			event.setCancelled(true);
		};
		if(event.getMessage().equals("Cancel ") && TrainCarts.plugin.speedProgress.get(event.getPlayer().getName()) != null) {
			TrainCarts.plugin.speedProgress.get(event.getPlayer().getName()).cancel(event.getPlayer());
			event.setCancelled(true);
		};
		/*if(event.getMessage().equals("Cancel ") && TrainCarts.plugin.pathFindingProgress.get(event.getPlayer().getName()) != null) {
			TrainCarts.plugin.pathFindingProgress.get(event.getPlayer().getName()).cancel(event.getPlayer());
			event.setCancelled(true);
		};*/

	}

	//Player disconnect
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = (Player) event.getPlayer();
		Entity e = (Entity) p.getVehicle();
		if(e == null) {
			return;
		}
		if(TrainCarts.plugin.playerProgress.get(event.getPlayer().getName()) != null) {
			TrainCarts.plugin.playerProgress.remove(event.getPlayer().getName());
			event.getPlayer().sendMessage("§cRoute creation cancelled.");
		}
        if(TrainCarts.plugin.MemberController.getFromEntity(e) != null) {
        	e.eject();
        	return;
        }
	}
	
	//MINECART collision (Cancelled if TC entity)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
		if(TrainCarts.plugin.MemberController.getFromEntity(event.getVehicle()) != null) {
    		event.setCancelled(true);
		}
    }
	
    //Sign right click (Convert to node if TC)
    private TextComponent[] msg = {};
    @SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
    	Player p = (Player) e.getPlayer();

		if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(TrainCarts.plugin.playerProgress.get(p.getName()) != null) {
				TrainCarts.plugin.playerProgress.get(p.getName()).addBlock(e.getClickedBlock(), true);
				e.setCancelled(true);
				return;
			};
		} else if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if(TrainCarts.plugin.playerProgress.get(p.getName()) != null) {
				TrainCarts.plugin.playerProgress.get(p.getName()).addBlock(e.getClickedBlock(), false);
				e.setCancelled(true);
				return;
			};
		}

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
    					TextComponent connection = new TextComponent(ChatColor.GREEN + " - " + ChatColor.BOLD + "" + ChatColor.WHITE + con.facing + ChatColor.GREEN + " >> " + ChatColor.BOLD + "" + ChatColor.WHITE + con.opositeFacing + ChatColor.GREEN + " - " + ChatColor.YELLOW + (con.getEndNode() != null ? con.getEndNode().getLocationStr() : "§cNULL§e") + "\n");
        				msg = append(msg, connection);
    				});
    				e.getPlayer().spigot().sendMessage(msg);
            		e.setCancelled(true);
            		return;
            	}
				if(e.getItem().getType().equals(Material.COMMAND_BLOCK)) {
					Sign sign = (Sign) e.getClickedBlock().getState();
            		MetroLine a = TrainCarts.plugin.lines.getLine("#GLOBAL");
                	PathNode b = a.getNode(sign.getLocation().clone().add(0.5, 2, 0.5));
                	if(b == null) {
                		e.getPlayer().sendMessage(ChatColor.RED + "Node is invalid!");
                		e.setCancelled(true);
                		return;
                	}
					if(b.getAction() instanceof SignActionPlatform) {
						SignActionPlatform c = (SignActionPlatform) b.getAction();
						TextComponent header = new TextComponent(ChatColor.WHITE + "" + ChatColor.BOLD + "= Config of plat " + c.station.code + "~" + c.platform + " =\n");
    					msg = append(msg, header);
    					TextComponent doors = new TextComponent(ChatColor.GREEN + " - " + ChatColor.BOLD + "" + ChatColor.WHITE + "# of doors " + ChatColor.GREEN + " - " + ChatColor.BOLD + "" + ChatColor.WHITE + c.doorLocs.size() + "\n " + ChatColor.GREEN + "- " + ChatColor.BOLD + "" + ChatColor.WHITE + "Door locs:" + "\n");
        				msg = append(msg, doors);
						c.doorLocs.forEach(con -> {
							TextComponent connection = new TextComponent(ChatColor.GREEN + " - " + ChatColor.BOLD + "" + ChatColor.WHITE + con.getX() + ChatColor.GREEN + "/" + ChatColor.BOLD + "" + ChatColor.WHITE + con.getY() + ChatColor.GREEN + "/" +ChatColor.BOLD + "" + ChatColor.WHITE  + con.getZ() + "\n");
							msg = append(msg, connection);
						});
						TextComponent lights = new TextComponent(ChatColor.GREEN + " - " + ChatColor.BOLD + "" + ChatColor.WHITE + "# of lights " + ChatColor.GREEN + " - " + ChatColor.BOLD + "" + ChatColor.WHITE + c.doorLocs.size() + "\n " + ChatColor.GREEN + "- " + ChatColor.BOLD + "" + ChatColor.WHITE + "Light locs:" + "\n");
        				msg = append(msg, lights);
						c.lightLocs.forEach(con -> {
							TextComponent connection = new TextComponent(ChatColor.GREEN + " - " + ChatColor.BOLD + "" + ChatColor.WHITE + con.getX() + ChatColor.GREEN + "/" + ChatColor.BOLD + "" + ChatColor.WHITE + con.getY() + ChatColor.GREEN + "/" +ChatColor.BOLD + "" + ChatColor.WHITE  + con.getZ() + "\n");
							msg = append(msg, connection);
						});
    					e.getPlayer().spigot().sendMessage(msg);
            			e.setCancelled(true);
					} else {
						e.getPlayer().sendMessage(ChatColor.RED + "Node is invalid!");
                		e.setCancelled(true);
                		return;
					}
					return;
				}
                Sign sign = (Sign) e.getClickedBlock().getState();
                if(sign.getLine(0).startsWith("t:") && (p.hasPermission("tc2.build") || p.isOp())) {
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
