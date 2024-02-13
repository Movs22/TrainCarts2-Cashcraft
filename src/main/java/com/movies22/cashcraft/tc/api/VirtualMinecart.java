
package com.movies22.cashcraft.tc.api;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;


public class VirtualMinecart implements Minecart {
	private Minecart ent = null;
	public MinecartMember pivot = null;
	private Double offset = 0.0d;
	private UUID uuid = null;
	private Vector mod = new Vector(2, 1, 2);
	private Boolean virtualized = false;
	private Vector velocity = new Vector(0, 0, 0);
	public Double maxSpeed;
	private List<Entity> a = new ArrayList<Entity>();
	private Location loc;
	VirtualMinecart(Entity e, MinecartMember m, Double o) {
		this.ent = (Minecart) e;
		this.uuid = e.getUniqueId();
		this.pivot = m;
		this.offset = o;
		this.loc = e.getLocation();
	};
	
	public Entity getEntity() {
		if(!this.virtualized) {
			return ent;
		}
		return null;
	}

	public Boolean load() {
		return this.load(false);
	}
	
	public Boolean load(Boolean b) {
		if(this.virtualized) {
			Location l = this.getLoc(b);
			World w = l.getWorld();
			Minecart e = (Minecart) w.spawnEntity(l, EntityType.MINECART);
			this.ent = e;
			this.setVirtualized(false);
			this.uuid = e.getUniqueId();
			this.loc = l;
		}
		return true;
	}

	public void setVirtualized(Boolean b) {
		this.virtualized = b;
	}
	
	public void setOffset(Double o) {
		if(this.offset != o) {
			switch(this.pivot.facing) {
				case EAST:
					this.loc.subtract(this.offset-o, 0, 0);
					break;
				case NORTH:
					this.loc.subtract(0, 0, this.offset-o);
					break;
				case SOUTH:
					this.loc.add(0, 0, this.offset-o);
					break;
				case WEST:
					this.loc.add(this.offset-o, 0, 0);
					break;
				default:
					break;
				
		}
		}
		this.offset = o;
	}
	
	public Boolean getVirtualized() {
		return this.virtualized;
	}
	
	public void setPivot(MinecartMember mm) {
		this.pivot = mm;
	}

	@Override
	public boolean eject() {
		if(this.ent == null) return false;
		return this.ent.eject();
	}

	@Override
	public Location getLocation(Location l) {
		return this.getLocation();
	}

	@Override
	public Location getLocation() {
		if(this.ent != null && !this.ent.isDead()) {
			this.loc = this.getLoc();
		};
		return this.getLoc();
	}

	@Override
	public List<Entity> getPassengers() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getPassengers();
		};
		return a;
	}

	@Override
	public boolean isDead() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.isDead();
		};
		return false;
	}

	@Override
	public boolean isEmpty() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.isEmpty();
		};
		return true;
	}

	public void syncY(Double y) {
		if(this.ent == null) {
			this.loc.setY(y);
		}
	}

	public void changeY(Double y) {
		if(this.ent == null) {
			this.loc.add(0, y, 0);
		}
	}

	@Override
	public void remove() {
		if(this.ent != null && !this.ent.isDead()) {
			this.ent.remove();
			this.ent = null;
		};
	}

	@Override
	public double getMaxSpeed() {
		return this.maxSpeed;
	}

	@Override
	public void setMaxSpeed(double arg0) {
		this.maxSpeed = arg0;
		if(this.ent != null && !this.ent.isDead()) {
			this.ent.setMaxSpeed(arg0);
		};
	}

	@Override
	public Vector getVelocity() {
		return this.velocity.divide(mod);
	}


	@Override
	public void setVelocity(Vector arg0) {
		this.velocity = arg0;
		if(this.ent != null && !this.ent.isDead()) {
			this.ent.setVelocity(arg0);
		} else {
			this.loc.add(arg0.multiply(mod));
		};
	}


	public Location getLoc() {
		if(this.ent != null && !this.ent.isDead()) {
			this.loc = this.ent.getLocation();
			return this.ent.getLocation();
		};
		return this.getLoc(false);
	}

	public Location getLoc(Boolean b) {
		if(this.pivot.getEntity() == this) {
			if(b) {
				Location l = this.loc.clone();
					switch(this.pivot.facing) {
						case EAST:
							l.add(this.offset, 0, 0);
							break;
						case NORTH:
							l.subtract(0, 0, this.offset);
							break;
						case SOUTH:
							l.add(0, 0, this.offset);
							break;
						case WEST:
							l.subtract(this.offset, 0, 0);
							break;
						default:
							break;
					
				}
				return l;
			} else {
				return this.loc;
			}
		} else {
			if(b) {
				Location l = this.pivot.getLocation().clone();
					switch(this.pivot.facing) {
						case EAST:
							l.add(this.offset, 0, 0);
							break;
						case NORTH:
							l.subtract(0, 0, this.offset);
							break;
						case SOUTH:
							l.add(0, 0, this.offset);
							break;
						case WEST:
							l.subtract(this.offset, 0, 0);
							break;
						default:
							break;
					
				}
				this.loc = l;
				return l;
			} else {
				return this.pivot.getLocation();
			}
		}
	}

	@Override
	public boolean addPassenger(Entity arg0) {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.addPassenger(arg0);
		};
		return false;
	}

	@Override
	public boolean addScoreboardTag(String arg0) {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.addScoreboardTag(arg0);
		};
		return false;
	}

	public void syncPos(Location l) {
		this.loc = l;
	}

	@Override
	public Entity copy() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.copy();
		};
		return null;
	}

	@Override
	public Entity copy(Location arg0) {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.copy(arg0);
		};
		return null;
	}

	@Override
	public EntitySnapshot createSnapshot() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.createSnapshot();
		};
		return null;
	}

	@Override
	public BoundingBox getBoundingBox() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getBoundingBox();
		};
		return null;
	}

	@Override
	public int getEntityId() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getEntityId();
		};
		return -1;
	}

	@Override
	public BlockFace getFacing() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getFacing();
		};
		return null;
	}

	@Override
	public float getFallDistance() {
		return 0f;
	}

	@Override
	public int getFireTicks() {
		return 0;
	}

	@Override
	public int getFreezeTicks() {
		return 0;
	}

	@Override
	public double getHeight() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getHeight();
		};
		return this.loc.getY();
	}

	@Override
	public EntityDamageEvent getLastDamageCause() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getLastDamageCause();
		};
		return null;
	}

	@Override
	public int getMaxFireTicks() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getMaxFireTicks();
		};
		return 0;
	}

	@Override
	public int getMaxFreezeTicks() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getMaxFreezeTicks();
		};
		return 0;
	}

	@Override
	public List<Entity> getNearbyEntities(double arg0, double arg1, double arg2) {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getNearbyEntities(arg0, arg1, arg2);
		};
		return null;
	}

	@Override
	public Entity getPassenger() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getPassenger();
		};
		return null;
	}

	@Override
	public PistonMoveReaction getPistonMoveReaction() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getPistonMoveReaction();
		};
		return null;
	}

	@Override
	public int getPortalCooldown() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getPortalCooldown'");
	}

	@Override
	public Pose getPose() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getPose'");
	}

	@Override
	public Set<String> getScoreboardTags() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getScoreboardTags'");
	}

	@Override
	public Server getServer() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getServer'");
	}

	@Override
	public SpawnCategory getSpawnCategory() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getSpawnCategory'");
	}

	@Override
	public Sound getSwimHighSpeedSplashSound() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getSwimHighSpeedSplashSound'");
	}

	@Override
	public Sound getSwimSound() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getSwimSound'");
	}

	@Override
	public Sound getSwimSplashSound() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getSwimSplashSound'");
	}

	@Override
	public int getTicksLived() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getTicksLived'");
	}

	@Override
	public Set<Player> getTrackedBy() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getTrackedBy'");
	}

	@Override
	public EntityType getType() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getType'");
	}

	@Override
	public UUID getUniqueId() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getUniqueId();
		};
		return this.uuid;
	}

	@Override
	public Entity getVehicle() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getVehicle'");
	}

	@Override
	public double getWidth() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getWidth'");
	}

	@Override
	public World getWorld() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getWorld'");
	}

	@Override
	public boolean hasGravity() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'hasGravity'");
	}

	@Override
	public boolean isCustomNameVisible() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isCustomNameVisible'");
	}

	@Override
	public boolean isFrozen() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isFrozen'");
	}

	@Override
	public boolean isGlowing() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isGlowing'");
	}

	@Override
	public boolean isInWater() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isInWater'");
	}

	@Override
	public boolean isInWorld() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isInWorld'");
	}

	@Override
	public boolean isInsideVehicle() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isInsideVehicle'");
	}

	@Override
	public boolean isInvulnerable() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isInvulnerable'");
	}

	@Override
	public boolean isOnGround() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.isOnGround();
		};
		return false;
	}

	@Override
	public boolean isPersistent() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isPersistent'");
	}

	@Override
	public boolean isSilent() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isSilent'");
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isValid'");
	}

	@Override
	public boolean isVisibleByDefault() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isVisibleByDefault'");
	}

	@Override
	public boolean isVisualFire() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isVisualFire'");
	}

	@Override
	public boolean leaveVehicle() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'leaveVehicle'");
	}

	@Override
	public void playEffect(EntityEffect arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'playEffect'");
	}

	@Override
	public boolean removePassenger(Entity arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'removePassenger'");
	}

	@Override
	public boolean removeScoreboardTag(String arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'removeScoreboardTag'");
	}

	@Override
	public void setCustomNameVisible(boolean arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setCustomNameVisible'");
	}

	@Override
	public void setFallDistance(float arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setFallDistance'");
	}

	@Override
	public void setFireTicks(int arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setFireTicks'");
	}

	@Override
	public void setFreezeTicks(int arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setFreezeTicks'");
	}

	@Override
	public void setGlowing(boolean arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setGlowing'");
	}

	@Override
	public void setGravity(boolean arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setGravity'");
	}

	@Override
	public void setInvulnerable(boolean arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setInvulnerable'");
	}

	@Override
	public void setLastDamageCause(EntityDamageEvent arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setLastDamageCause'");
	}

	@Override
	public boolean setPassenger(Entity arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setPassenger'");
	}

	@Override
	public void setPersistent(boolean arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setPersistent'");
	}

	@Override
	public void setPortalCooldown(int arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setPortalCooldown'");
	}

	@Override
	public void setRotation(float arg0, float arg1) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setRotation'");
	}

	@Override
	public void setSilent(boolean arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setSilent'");
	}

	@Override
	public void setTicksLived(int arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setTicksLived'");
	}

	@Override
	public void setVisibleByDefault(boolean arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setVisibleByDefault'");
	}

	@Override
	public void setVisualFire(boolean arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setVisualFire'");
	}

	@Override
	public Spigot spigot() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'spigot'");
	}

	@Override
	public boolean teleport(Location arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'teleport'");
	}

	@Override
	public boolean teleport(Entity arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'teleport'");
	}

	@Override
	public boolean teleport(Location arg0, TeleportCause arg1) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'teleport'");
	}

	@Override
	public boolean teleport(Entity arg0, TeleportCause arg1) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'teleport'");
	}

	@Override
	public List<MetadataValue> getMetadata(String arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getMetadata'");
	}

	@Override
	public boolean hasMetadata(String arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'hasMetadata'");
	}

	@Override
	public void removeMetadata(String arg0, Plugin arg1) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'removeMetadata'");
	}

	@Override
	public void setMetadata(String arg0, MetadataValue arg1) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setMetadata'");
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getName'");
	}

	@Override
	public void sendMessage(String arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
	}

	@Override
	public void sendMessage(String... arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
	}

	@Override
	public void sendMessage(UUID arg0, String arg1) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
	}

	@Override
	public void sendMessage(UUID arg0, String... arg1) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'addAttachment'");
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getEffectivePermissions'");
	}

	@Override
	public boolean hasPermission(String arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'hasPermission'");
	}

	@Override
	public boolean hasPermission(Permission arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'hasPermission'");
	}

	@Override
	public boolean isPermissionSet(String arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isPermissionSet'");
	}

	@Override
	public boolean isPermissionSet(Permission arg0) {
		if(this.ent != null && !this.ent.isDead()) {
			return this.isPermissionSet(arg0);
		};
		return false;
	}

	@Override
	public void recalculatePermissions() {
		if(this.ent != null && !this.ent.isDead()) {
			this.ent.recalculatePermissions();
		};
	}

	@Override
	public void removeAttachment(PermissionAttachment arg0) {
		if(this.ent != null && !this.ent.isDead()) {
			this.ent.removeAttachment(arg0);
		};
	}

	@Override
	public boolean isOp() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.isOp();
		};
		return false;
	}

	@Override
	public void setOp(boolean arg0) {
		if(this.ent != null && !this.ent.isDead()) {
			this.ent.setOp(arg0);
		};
	}

	@Override
	public String getCustomName() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getCustomName();
		};
		return null;
	}

	@Override
	public void setCustomName(String arg0) {
		if(this.ent != null && !this.ent.isDead()) {
			this.ent.setCustomName(arg0);
		};
	}

	@Override
	public PersistentDataContainer getPersistentDataContainer() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getPersistentDataContainer();
		};
		return null;
	}

	@Override
	public double getDamage() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getDamage();
		};
		return 0.0d;
	}

	@Override
	public Vector getDerailedVelocityMod() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getDerailedVelocityMod();
		};
		return null;
	}

	@Override
	public MaterialData getDisplayBlock() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getDisplayBlock();
		};
		return null;
	}

	@Override
	public BlockData getDisplayBlockData() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getDisplayBlockData();
		};
		return null;
	}

	@Override
	public int getDisplayBlockOffset() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getDisplayBlockOffset();
		};
		return 0;
	}

	@Override
	public Vector getFlyingVelocityMod() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.getFlyingVelocityMod();
		};
		return null;
	}

	@Override
	public boolean isSlowWhenEmpty() {
		if(this.ent != null && !this.ent.isDead()) {
			return this.ent.isSlowWhenEmpty();
		};
		return false;
	}

	@Override
	public void setDamage(double arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setDamage'");
	}

	@Override
	public void setDerailedVelocityMod(Vector arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setDerailedVelocityMod'");
	}

	@Override
	public void setDisplayBlock(MaterialData arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setDisplayBlock'");
	}

	@Override
	public void setDisplayBlockData(BlockData arg0) {
		if(this.ent != null && !this.ent.isDead()) {
			this.ent.setDisplayBlockData(arg0);
		};
	}

	@Override
	public void setDisplayBlockOffset(int arg0) {
		if(this.ent != null && !this.ent.isDead()) {
			this.ent.setDisplayBlockOffset(arg0);
		};
	}

	@Override
	public void setFlyingVelocityMod(Vector arg0) {
		if(this.ent != null && !this.ent.isDead()) {
			this.ent.setFlyingVelocityMod(arg0);
		};
	}

	@Override
	public void setSlowWhenEmpty(boolean arg0) {
		if(this.ent != null && !this.ent.isDead()) {
			this.ent.setSlowWhenEmpty(arg0);
		};
	}
}
