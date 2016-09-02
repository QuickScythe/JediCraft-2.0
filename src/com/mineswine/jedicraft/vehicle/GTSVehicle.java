package com.mineswine.jedicraft.vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.dillyg10.gts.commands.CommandJedi;
import com.dillyg10.gts.util.Utils;
import com.google.common.reflect.TypeToken;
import com.mineswine.api.util.Animations;
import com.mineswine.api.util.Animations.AnimationType;
import com.mineswine.api.util.GsonFactory;
import com.mineswine.api.util.ItemStackUtil;
import com.shampaggon.crackshot.CSDirector;

import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import net.minecraft.server.v1_9_R1.DamageSource;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityArmorStand;
import net.minecraft.server.v1_9_R1.EntityHuman;
import net.minecraft.server.v1_9_R1.EntityLiving;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.EntityProjectile;
import net.minecraft.server.v1_9_R1.EntityWolf;
import net.minecraft.server.v1_9_R1.EnumHand;
import net.minecraft.server.v1_9_R1.EnumInteractionResult;
import net.minecraft.server.v1_9_R1.EnumItemSlot;
import net.minecraft.server.v1_9_R1.Material;
import net.minecraft.server.v1_9_R1.MathHelper;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_9_R1.SoundEffect;
import net.minecraft.server.v1_9_R1.Vec3D;
import net.minecraft.server.v1_9_R1.World;
import net.minecraft.server.v1_9_R1.WorldServer;

public class GTSVehicle extends EntityArmorStand implements WASDEntity {
	public static CSDirector director;
	long lastTimeJumped = 0;
	//	public long timestamp = 0L;

	public double forward = 0;
	public double sideways = 0; //Left is positive
	public boolean jump = false;
	public boolean shift = false;
	public float speed = 0.4f;

	public float accellerationRate = 0.0f;
	public float currentAccelleration = 0.0f;
	public float maxAccelleration = 0.0f;
	public int fireballCooldown;
	private final net.minecraft.server.v1_9_R1.ItemStack[] h = new net.minecraft.server.v1_9_R1.ItemStack[5];

	public int life = 0;

	public int health = 0;

	public VehicleType type;

	public EntityPlayer driver;
	public EntityPlayer gunner;
	public List<EntityPlayer> thepassengers;
	public boolean locked = true;
	public int f;
	boolean fr1;

	public GTSVehicle(World world){
		super(world);
	}

	public GTSVehicle(World world, VehicleType type) {
		super(world);
		if (director == null)
			director = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");
		this.type = type;
		this.health = type.health;
		this.accellerationRate = type.accelerationRate;
		this.maxAccelleration = type.maxAcceleration;
		this.setBasePlate(false);
		this.setGravity(false);
		this.setInvisible(true);
		this.setSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(type.headItem));
		this.setSize(2.0f,2.0f);
		this.thepassengers = new ArrayList<EntityPlayer>();
	}

	public void C(Entity entity){
//		entity.collide(this);
		this.collide(entity);
	}


	public boolean isInteractable(){
		return !this.dead;
	}

	public void m(){
//		this.bL();
		//		if (burning && this.ticksLived % 5 == 0){
		//			Animations.animate(Animations.constructPacket(AnimationType.SMOKE, this.getBukkitEntity().getLocation(), 0.03f, 15, ""));
		//		}
		if (type == VehicleType.BIKE && this.currentAccelleration != 0 && ticksLived % 4 == 0){
			if (fr1)
				this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(type.headItem));
			else
				this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.SPONGE,1,(short)1)));
			fr1 = !fr1;
		}
		for(EnumItemSlot enumItemSlot : EnumItemSlot.values()) {
			net.minecraft.server.v1_9_R1.ItemStack itemstack = this.h[j];
			net.minecraft.server.v1_9_R1.ItemStack itemstack1 = this.getEquipment(enumItemSlot);
			if(!net.minecraft.server.v1_9_R1.ItemStack.matches(itemstack1, itemstack)) {
				((WorldServer)this.world).getTracker().a(this, new PacketPlayOutEntityEquipment(this.getId(), enumItemSlot, itemstack1));
//				if(itemstack != null) {
//					this.c.a(itemstack.B());
//				}
//
//				if(itemstack1 != null) {
//					this.c.b(itemstack1.B());
//				}

				this.h[j] = itemstack1 == null?null:itemstack1.cloneItemStack();
			}
		}
		if (getHealth() > 0 && this.getHealth() <= 2.0f & ticksLived % 5 == 0){
			Animations.animate(Animations.constructPacket(AnimationType.LAVA, this.getBukkitEntity().getLocation(), 0.3f, 5, ""));
		}
		if (fireballCooldown >= 0)
			fireballCooldown--;
		this.yaw = this.driver != null ? this.driver.yaw : this.yaw;
		if (type.flying){
			if(this.driver == null) {
				reset();
				life++;
				if (life == 5*60*20){
					die();
					return;
				}
				forward = 0;
				sideways = 0;
				this.U();
				float percent = (this.currentAccelleration < 0.0f ? (this.currentAccelleration / -0.3f) : (this.currentAccelleration / this.maxAccelleration));
				if(this.onGround){
					if(this.onGround){
						//				_.broadcast(""+this.fallDistance);
						this.motY= -0.00001;
						this.motX=0;
						this.motZ=0;
						this.velocityChanged=true;
						return;
					}
				} else {
					if (this.currentAccelleration < 0.0f) {
						this.currentAccelleration = Utils._m(currentAccelleration, 0.0f, accellerationRate * 1.2f);
					} else if (this.currentAccelleration > 0.0f) {
						this.currentAccelleration = Utils._m_(currentAccelleration, 0.0f, accellerationRate * 1.2f);
					}
					Vector v = this.getBukkitEntity().getLocation().getDirection().multiply(currentAccelleration);
					this.move(v.getX(), -1, v.getZ());
					this.fallDistance = 0f;
					//			this.motY = -0.00001;
					this.motX = 0;
					this.motZ = 0;
					this.velocityChanged = true;
				}
				update(percent);
			} else {
				float percent = (this.currentAccelleration < 0.0f ? (this.currentAccelleration/-0.3f) : (this.currentAccelleration/this.maxAccelleration));
				this.yaw = this.driver.getBukkitEntity().getLocation().getYaw();
				Vector v = this.driver.getBukkitEntity().getLocation().getDirection().setY(0).normalize();
				Vector vSideways = this.driver.getBukkitEntity().getLocation().getDirection().setY(0).normalize().crossProduct(new Vector(0,1,0));
				if(forward > 0) {
					//forward
					this.currentAccelleration = Utils._m(currentAccelleration, maxAccelleration, accellerationRate);
				} else if(forward < 0) {
					//back
					//				v.multiply(-1);
					this.currentAccelleration = Utils._m_(currentAccelleration, -(maxAccelleration/2), accellerationRate*5);
				} else{
					if (this.currentAccelleration < 0.0f){
						this.currentAccelleration = Utils._m(currentAccelleration, 0.0f, accellerationRate*2.0f);
					} else if (this.currentAccelleration > 0.0f){
						this.currentAccelleration = Utils._m_(currentAccelleration, 0.0f, accellerationRate*1.5f);
					}
				}
				if (shift){
					v.setY(-0.8);
				} else if (jump){
					v.setY(1.8);
				} else {
					v.setY(0);
				}

				if(sideways > 0) {
					//left
					v.add(vSideways.multiply((this.currentAccelleration < 0.0f ? 1.0f : -1.0f)));
				}else if(sideways < 0) {
					//right
					v.add(vSideways.multiply((this.currentAccelleration > 0.0f ? 1.0f : -1.0f)));
				}
				//v.setY(-0.4);
				v.multiply(currentAccelleration);
				//v.setY(-0.4);
				//			v.multiply((speed / (burning ? 2 : 1) / (onGround ? 4 : 1)));
				this.move(v.getX(),v.getY(),v.getZ());
				//			//			_.broadcast("A: "+this.positionChanged+" B: "+this.H);
				//						this.motX=v.getX();
				//						this.motZ=v.getZ();
				//
				//						if (positionChanged){
				//							if (ticksLived - lastTimeJumped > 10){
				//			//					this.be();
				//								this.move(v.getX(), 1.2, v.getZ());
				//								this.lastTimeJumped=ticksLived;
				//							}
				//						} else {
				//							this.motY = -0.8;
				//						}
				//						this.motY=-0.8;
				//						this.e((float)sideways,(float)forward);
				//			if (world.getTypeId((int)(locX+v.getBlockX()), (int)(locY+v.getBlockY()), (int)(locZ+v.getBlockZ())) != 0 ||
				//					world.getTypeId((int)(locX-v.getBlockX()), (int)(locY+v.getBlockY()), (int)(locZ+v.getBlockZ())) != 0 ||
				//					world.getTypeId((int)(locX+v.getBlockX()), (int)(locY+v.getBlockY()), (int)(locZ-v.getBlockZ())) != 0 ||
				//					world.getTypeId((int)(locX-v.getBlockX()), (int)(locY+v.getBlockY()), (int)(locZ-v.getBlockZ())) != 0){
				//				_.broadcast("Test");
				//			}
				//	this.lastY = locY;
				//	this.locY = this.lastY+1;
				update(percent);
				if(this.onGround){
					//				_.broadcast(""+this.fallDistance);
					this.motY= -0.00001;
					this.motX=0;
					this.motZ=0;
					this.velocityChanged=true;
					return;
				}
				//			this.fallDistance = 0f;
				if (this.forward==0 && this.sideways ==0 && !jump && !shift ){
					//				if (this.motY == 0) this.motY = -0.05;
					//				this.motY *= -0.05;
					//				if (this.motY ==0) this.motY = 0.093824932732;
					//				this.motY *= 0.3;
					this.motY = -0.6;
					this.motX = 0;
					this.motZ = 0;
					//				this.move(0,motY,0);
					this.e(0,0);
				} else {
					this.motY= -0.00001;
					this.motX=0;
					this.motZ=0;
					this.velocityChanged=true;
				}

				//			this.velocityChanged = true;
			}
		} else {
			if (this.driver == null) {
				forward = 0;
				sideways = 0;
				life++;
				if (life == 5 * 60 * 20) {
					die();
					return;
				}
				this.U();
				this.fallDistance = 0f;
				//			this.motY = -0.00001;
				float percent = (this.currentAccelleration < 0.0f ? (this.currentAccelleration / -0.3f) : (this.currentAccelleration / this.maxAccelleration));
				if (this.currentAccelleration == 0.0f) {
					this.motX = 0;
					this.motZ = 0;
					this.motY = 0;
					this.velocityChanged = true;
					this.move(0, 0, 0);
				} else {
					if (this.currentAccelleration < 0.0f) {
						this.currentAccelleration = Utils._m(currentAccelleration, 0.0f, accellerationRate * 1.2f);
					} else if (this.currentAccelleration > 0.0f) {
						this.currentAccelleration = Utils._m_(currentAccelleration, 0.0f, accellerationRate * 1.2f);
					}
					Vector v = this.getBukkitEntity().getLocation().getDirection().multiply(currentAccelleration);
					this.move(v.getX(), 0, v.getZ());
				}
				this.locY = Math.floor(this.locY);
				update(percent);
				return;
			} else {
				float percent = (this.currentAccelleration < 0.0f ? (this.currentAccelleration / -0.3f) : (this.currentAccelleration / this.maxAccelleration));
				this.yaw = driver.yaw;
				Vector v = driver.getBukkitEntity().getLocation().getDirection().setY(0).normalize();
				Vector vSideways = driver.getBukkitEntity().getLocation().getDirection().setY(0).normalize().crossProduct(new Vector(0, 1, 0));
				if (forward > 0) {
					//forward
					this.currentAccelleration = Utils._m(currentAccelleration, maxAccelleration, accellerationRate);
				} else if (forward < 0) {
					//back
					//				v.multiply(-1);
					this.currentAccelleration = Utils._m_(currentAccelleration, -(maxAccelleration / 2), accellerationRate * 5);
				} else {
					if (this.currentAccelleration < 0.0f) {
						this.currentAccelleration = Utils._m(currentAccelleration, 0.0f, accellerationRate * 2.0f);
					} else if (this.currentAccelleration > 0.0f) {
						this.currentAccelleration = Utils._m_(currentAccelleration, 0.0f, accellerationRate * 1.5f);
					}
				}
				//			if (shift){
				//				v.setY(-0.8);
				//			} else if (jump){
				//				v.setY(1.8);
				//			} else {
				//				v.setY(0);
				//			}
				if (sideways > 0) {
					//left
					v.add(vSideways.multiply((this.currentAccelleration < 0.0f ? 1.0f : -1.0f)));
				} else if (sideways < 0) {
					//right
					v.add(vSideways.multiply((this.currentAccelleration > 0.0f ? 1.0f : -1.0f)));
				}
				//v.setY(-0.4);
				v.multiply(currentAccelleration);

				this.move(v.getX(), 0, v.getZ());
				//			_.broadcast("A: "+this.positionChanged+" B: "+this.H);
				this.motX = v.getX();
				this.motZ = v.getZ();

				//			if (positionChanged){
				//				if (ticksLived - lastTimeJumped > 10){
				//					//					this.be();
				//					this.move(v.getX(), 1.2, v.getZ());
				//					this.lastTimeJumped=ticksLived;
				//				}
				//			} else {
				//				this.motY = -1;
				//			}
				if (positionChanged && this.getBukkitEntity().getLocation().add(0, -1, 0).getBlock().getType() != org.bukkit.Material.AIR) {
					this.move(v.getX(), 1.2, v.getZ());
					this.lastTimeJumped = ticksLived;
				} else {
					this.motY = -0.8;
				}
				this.move(0, -3, 0);
				this.motY = -1;
				this.e((float) sideways, (float) forward);
				this.R = 0.5f;
				//			if (world.getTypeId((int)(locX+v.getBlockX()), (int)(locY+v.getBlockY()), (int)(locZ+v.getBlockZ())) != 0 ||
				//					world.getTypeId((int)(locX-v.getBlockX()), (int)(locY+v.getBlockY()), (int)(locZ+v.getBlockZ())) != 0 ||
				//					world.getTypeId((int)(locX+v.getBlockX()), (int)(locY+v.getBlockY()), (int)(locZ-v.getBlockZ())) != 0 ||
				//					world.getTypeId((int)(locX-v.getBlockX()), (int)(locY+v.getBlockY()), (int)(locZ-v.getBlockZ())) != 0){
				//				_.broadcast("Test");
				//			}
				//	this.lastY = locY;
				//	this.locY = this.lastY+1;
				//				if(this.onGround)
				//					return;
				//				this.fallDistance = 0f;
				//				this.motY = -0.00001;
				//				this.motX = 0;
				//				this.motZ = 0;
				//				this.velocityChanged = true;
				//			if (!positionChanged)	this.locY = Math.floor(this.locY);
				update(percent);
			}
		}

		if (type != VehicleType.TANK) {
			double zTilt = sideways > 0 ? (yaw < lastYaw ? 27 : yaw > lastYaw ? 7 : 20) : sideways < 0 ? (yaw > lastYaw ? -27 : yaw < lastYaw ? -7 : -20) : yaw < lastYaw ? 7 : yaw > lastYaw ? -7 : 0;
			double xTilt = forward > 0 ? 5 : forward < 0 ? -7 : 0;

			setTilt(xTilt, 0, zTilt);
			lastYaw = yaw;
			tilt();
		}
	}


	public void b(NBTTagCompound tag) {
		tag.setInt("life", life);
		super.b(tag);
	}

	public void a(NBTTagCompound tag) {
		life = tag.getInt("life");
		super.a(tag);
	}
	public boolean canSpawn() {
		return true;
	}
	public void die(){
		remove_();
		super.die();
	}
	public void die(DamageSource source){
		remove_();
		super.die(source);
	}
	public void remove_(){
		for (EntityPlayer player : getPeople()){
			dismount(player);
			final String name = player.getName();
			Bukkit.getScheduler().runTaskLater(GTS.instance, new Runnable() {
				public void run() {
//					player.getBukkitEntity().setHealth(0);
					Bukkit.getPlayer(name).setHealth(0.0f);
				}
			}, 1L);
		}
		Animations.animate(Animations.constructPacket(AnimationType.HUGE_EXPOSION, this.getBukkitEntity().getLocation(), 0.3F, 1, ""));
		world.getWorld().playSound(getBukkitEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
	}
	@Override
	public EnumInteractionResult a(EntityHuman human, Vec3D vec3D, net.minecraft.server.v1_9_R1.ItemStack itemStack, EnumHand enumHand){
		if (this.getHealth() <= 0) return  EnumInteractionResult.FAIL;
		if (human.getBukkitEntity().hasMetadata("vehicle")) return EnumInteractionResult.FAIL;
		if (human.getBukkitEntity().hasMetadata("lastOnVehicle") && (System.currentTimeMillis() - human.getBukkitEntity().getMetadata("lastOnVehicle").get(0).asLong()) < 1000) return EnumInteractionResult.FAIL;
		human.getBukkitEntity().setMetadata("lastOnVehicle",new FixedMetadataValue(GTS.instance,System.currentTimeMillis()));
		CraftPlayer player = (CraftPlayer) human.getBukkitEntity();
		Position position = null;
		if (this.driver == null){
			position = Position.DRIVER;
		} else {
			if (locked){
				player.sendMessage("§cThis vehicle is not accepting passengers.");
				return EnumInteractionResult.FAIL;
			}
			if (gunner == null && type.gunner){
				position = Position.GUNNER;
			}
			if (thepassengers.size() < type.maxPassengers){
				position = Position.PASSENGER;
			}
		}
		if (position == null){
			player.sendMessage("§cThe vehicle is full.");
			return EnumInteractionResult.FAIL;
		}

		if (type == VehicleType.BIKE){
			player.sendMessage("§aYou've entered a §fGrevis Bike. §aUse WASD to move around and Spacebar to shoot its weapon.");
		} else if (type == VehicleType.TANK){
			player.sendMessage("§aYou've entered a §fTank. §aUse WASD to move around and Spacebar to shoot its weapon.");
		} else if (type == VehicleType.SPEEDER){
			player.sendMessage("§aYou've entered a §fSpeeder. §aUse WASD to move around.");
		} else if (type == VehicleType.FIGHTER){
			player.sendMessage("§aYou've entered a §fFighter. §aUse WASD to move, Spacebar/Shift to fly up/down and Shift+Space to dismount.");
		} else if (type == VehicleType.BOMBER){
			player.sendMessage("§aYou've entered a §fBomber. §aUse WASD to move, Spacebar/Shift to fly up/down and Shift+Space to dismount.");
		}
		player.sendMessage("§c§lIf your vehicle dies, you die!");
		ride(player.getHandle(),position,false);
		return EnumInteractionResult.PASS;
	}
	public void ride(EntityPlayer player, Position position, boolean alreadyOn){
		if (getBukkitEntity().hasMetadata("spawn"))
			getBukkitEntity().removeMetadata("spawn",GTS.instance);
		if (!alreadyOn){
			player.getBukkitEntity().setMetadata("vehicle", new FixedMetadataValue(GTS.instance,this));
			Map<Integer,ItemStack> items = new HashMap<Integer, ItemStack>();
			for (int i = 0; i < player.getBukkitEntity().getInventory().getSize(); i++){
				if (player.getBukkitEntity().getInventory().getItem(i) == null) continue;
				items.put(i, CraftItemStack.asBukkitCopy(ItemStackUtil.removeSlot(player.getBukkitEntity().getInventory().getItem(i))));
			}
//			PlayerProfile profile = GTSScoreboard.get(player.getBukkitEntity()).profile;
//			profile.addSetting(new PlayerSetting(profile,"gts.lastinv", GsonFactory.getCompactGson().toJson(items)));
			player.getBukkitEntity().setMetadata("lastinv",new FixedMetadataValue(GTS.instance,GsonFactory.getCompactGson().toJson(items)));
			NCPExemptionManager.exemptPermanently(player.getBukkitEntity());
		}
		player.getBukkitEntity().getInventory().clear();
		if (player.equals(this.driver)) this.driver = null;
		if (player.equals(this.gunner)) this.gunner = null;
		if (thepassengers.contains(player)) thepassengers.remove(player);
		if (position == Position.DRIVER) {
			this.driver = player;
			if (locked) player.getBukkitEntity().getInventory().setItem(8,ItemStackUtil.create(org.bukkit.Material.ENDER_PEARL,1,0,"§c§lLocked."));
			else player.getBukkitEntity().getPlayer().getInventory().setItem(8, ItemStackUtil.create(org.bukkit.Material.EYE_OF_ENDER, 1, 0, "§a§lUnlocked"));
		} else {
			player.getBukkitEntity().getInventory().setItem(8,null);
		}
		if (position == Position.GUNNER){
			this.gunner = player;
		}
		if (position == Position.PASSENGER) this.thepassengers.add(player);
		refreshAll();
//		player.playerConnection.sendPacket();
//		for (EntityPlayer player1 : getPeople()){
//			player1.getBukkitEntity().showPlayer(player.getBukkitEntity());
//			player.getBukkitEntity().showPlayer(player1.getBukkitEntity());
//		}
		player.startRiding(this);
//		for (Player player1 : Bukkit.getOnlinePlayers()){
//			player.playerConnection.sendPacket(new PacketPlayOutMount(this));
//		}

//		player.stopRiding();
//		this.startRiding(player);
//		this.o(player);
//		this.a(player,false);
//		EntityBoat boat;
//		this.getBukkitEntity().setPassenger(player.getBukkitEntity());
//		player.playerConnection.sendPacket(new PacketPlayOutAttachEntity(player, this));

		if (!alreadyOn)
			for (Player player1 : Bukkit.getOnlinePlayers())
					player1.hidePlayer(player.getBukkitEntity());
		ItemStack gun = CommandJedi.getCSItem(""+type+"_"+position);
		if (gun != null)
			player.getBukkitEntity().getInventory().setItem(0,gun);
		reloadNameTag();
	}

	public void dismount(EntityPlayer player){
		NCPExemptionManager.unexempt(player.getBukkitEntity());
		player.stopRiding();
//		player.playerConnection.sendPacket(new PacketPlayOutAttachEntity(player, null));
//		player.setPosition(locX,locY,locZ);
		player.getBukkitEntity().removeMetadata("vehicle", GTS.instance);
		player.getBukkitEntity().getInventory().clear();
		Map<Integer,ItemStack> items = GsonFactory.getCompactGson().fromJson(player.getBukkitEntity().getMetadata("lastinv").get(0).asString(), new TypeToken<Map<Integer,ItemStack>>(){}.getType());
		for (Entry<Integer,ItemStack> itemStackEntry : items.entrySet()){
			player.getBukkitEntity().getInventory().setItem(itemStackEntry.getKey(),itemStackEntry.getValue());
		}
		player.getBukkitEntity().removeMetadata("lastinv",GTS.instance);
		if (player.equals(driver)){
			this.driver = null;
		}
		else if (player.equals(gunner)) this.gunner = null;
		else thepassengers.remove(player);
		refreshAll();
		for (Player player1 : Bukkit.getOnlinePlayers())
			player1.showPlayer(player.getBukkitEntity());
		player.getBukkitEntity().setExp(0.0f);
		player.getBukkitEntity().setLevel(0);
		reloadNameTag();
	}

	public static void main(String[] args){
		System.out.println((int)101*Math.random());
	}

	public void update(float percent){
		for (EntityPlayer player : getPeople()){
			player.getBukkitEntity().setLevel((int) Math.ceil((double) (percent * 100)));
			player.getBukkitEntity().setExp(percent);
//			player.setPosition(this.locX, this.locY+this.length, this.locZ);
		}
	}

	protected boolean q(Entity entity){
		return true;
	}

	public boolean a(Material m) {
		return false;
	}

	public boolean dE(DamageSource damagesource, float amount){
		if(this.isInvulnerable(damagesource)) {
			return false;
		} else if(this.world.isClientSide) {
			return false;
		} else {
			this.ticksFarFromPlayer = 0;
			if(this.getHealth() <= 0.0F) {
				return false;
			} else {
				this.aB = 1.5F;
				boolean flag = true;
				if((float)this.noDamageTicks > (float)this.maxNoDamageTicks / 2.0F) {
					if(f <= this.lastDamage) {
						return false;
					}

					if(!this.damageEntity0(damagesource, f - this.lastDamage)) {
						return false;
					}

					this.lastDamage = f;
					flag = false;
				} else {
					this.getHealth();
					if(!this.damageEntity0(damagesource, f)) {
						return false;
					}

					this.lastDamage = f;
					this.noDamageTicks = this.maxNoDamageTicks;
					this.hurtTicks = this.av = 10;
				}

				this.az = 0.0F;
				Entity entity = damagesource.getEntity();
				if(entity != null) {
					if(entity instanceof EntityLiving) {
						this.b((EntityLiving)((EntityLiving)entity));
					}

					if(entity instanceof EntityHuman) {
						this.lastDamageByPlayerTime = 100;
						this.killer = (EntityHuman)entity;
					} else if(entity instanceof EntityWolf) {
						EntityWolf s = (EntityWolf)entity;
						if(s.isTamed()) {
							this.lastDamageByPlayerTime = 100;
							this.killer = null;
						}
					}
				}

				if(flag) {
					this.world.broadcastEntityEffect(this, (byte)2);
					if(damagesource != DamageSource.DROWN) {
						this.ao();
					}
//					EntityBoat bOat;

					if(entity != null) {
						double d0 = entity.locX - this.locX;

						double d1;
						for(d1 = entity.locZ - this.locZ; d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
							d0 = (Math.random() - Math.random()) * 0.01D;
						}

						this.az = (float)(MathHelper.b(d1, d0) * 180.0D / 3.1415927410125732D - (double)this.yaw);
						this.a(entity, f, d0, d1);
					} else {
						this.az = (float)((int)(Math.random() * 2.0D) * 180);
					}
				}

				if(this.getHealth() <= 0.0F) {
					SoundEffect s1 = this.bS();
					if(flag && s1 != null) {
						this.a(s1, this.cd(), this.ce());
					}

					this.die(damagesource);
				} else {
					SoundEffect s1 = this.bS();
					if(flag && s1 != null) {
						this.a(s1, this.cd(), this.ce());
					}
				}

				return true;
			}
		}
	}
	public boolean damageEntity(DamageSource d, float amount){
		if (d == DamageSource.FALL) return false;
		for (EntityPlayer player : getPeople()){
			if (player.equals(d.getEntity())) return false;
		}
		if (d.getEntity() != null && d.getEntity() instanceof EntityProjectile && ((EntityProjectile)d.getEntity()).getShooter() instanceof EntityPlayer){
			for (EntityPlayer player : getPeople()){
				if (player.equals(((EntityProjectile)d.getEntity()).getShooter())) return false;
			}
			String[] node = director.itemParentNode(((Player)((EntityProjectile)d.getEntity()).getShooter().getBukkitEntity()).getItemInHand(),((Player)((EntityProjectile)d.getEntity()).getShooter().getBukkitEntity()));
			if (node.length > 0){
				director.dealDamage(d.getEntity().getBukkitEntity(),(LivingEntity)this.getBukkitEntity(),null,node[0]);
			}
		}
		this.noDamageTicks=0;
		boolean b = dE(d, (float)amount);
		this.reloadNameTag();
		return b;
	}

	public void refreshAll(){
		for (EntityPlayer person: getPeople())
			refresh(person);
	}

	public void refresh(EntityPlayer player){
		Player bp = player.getBukkitEntity();
		if (driver != null){
			bp.getInventory().setItem(2, ItemStackUtil.setSkullOwner(ItemStackUtil.create(org.bukkit.Material.SKULL_ITEM,1,3,"§e§lDRIVER: §f"+driver.getName()),driver.getName()));
		} else {
			bp.getInventory().setItem(2,ItemStackUtil.create(org.bukkit.Material.STAINED_GLASS_PANE,1, locked ? DyeColor.RED.getWoolData() : DyeColor.GREEN.getWoolData(), locked ? "§c§lLocked" : "§a§lClick to become: §f§ldriver"));
		}

		if (gunner != null){
			bp.getInventory().setItem(3,ItemStackUtil.setSkullOwner(ItemStackUtil.create(org.bukkit.Material.SKULL_ITEM,1,3,"§e§lGUNNER: §f"+gunner.getName()),gunner.getName()));
		} else if (type.gunner){
			bp.getInventory().setItem(3,ItemStackUtil.create(org.bukkit.Material.STAINED_GLASS_PANE,1, locked ? DyeColor.RED.getWoolData() : DyeColor.GREEN.getWoolData(), locked ? "§c§lLocked" : "§a§lClick to become: §f§lgunner"));
		}

		for (int i = 0; i < type.maxPassengers; i++){
			if (thepassengers.size() > i){
				bp.getInventory().setItem(4+i,ItemStackUtil.setSkullOwner(ItemStackUtil.create(org.bukkit.Material.SKULL_ITEM,1,3,"§e§lPASSENGER: §f"+ thepassengers.get(i).getName()), thepassengers.get(i).getName()));
			} else {
				bp.getInventory().setItem(4+i,ItemStackUtil.create(org.bukkit.Material.STAINED_GLASS_PANE,1, locked ? DyeColor.RED.getWoolData() : DyeColor.GREEN.getWoolData(), locked ? "§c§lLocked" : "§a§lClick to become: §f§lpassenger"));
			}
		}
	}

	public List<EntityPlayer> getPeople(){
		ArrayList<EntityPlayer> players = new ArrayList<EntityPlayer>();
		if (driver != null) players.add(driver);
		if (gunner != null) players.add(gunner);
		players.addAll(thepassengers);
		return players;
	}

	@Override
	public String getName() {
		return type.name();
	}

	@Override
	public void left() {
		sideways=1;
	}


	@Override
	public void right() {
		sideways=-1;
	}


	@Override
	public void jump() {
//		if (type == VehicleType.BIKE){
//			new BukkitRunnable(){
//				public void run(){
//					if (fireballCooldown > 0) {
//						if (fireballCooldown % 5 == 0) ((Player)passenger.getBukkitEntity()).sendMessage("§eWalker missles §9are on a cooldown for another §b "+(fireballCooldown/20)+" seconds.");
//						return;
//					}
//					Player p = (Player)passenger.getBukkitEntity();
//					for (int i = 0; i < 2; i++){
//						Vector sideways = null;
//						if (i == 0) {
//							sideways = new Vector(-0.75,0,0.75);
//						}
//						if (i == 1){
//							sideways = new Vector(0.75,0,-0.75);
//						}
//						Vector force = p.getLocation().getDirection().multiply(0.3);
//						Vector l = p.getEyeLocation().toVector().add(p.getLocation().getDirection().multiply(new Vector(1.3,1,1.3))).add(sideways);
//						CustomFirework fw = new CustomFirework(((CraftWorld)p.getWorld()).getHandle(), ((CraftPlayer)p).getHandle(), force.getX(), force.getY(), force.getZ());
//						fw.setPosition(l.getX(),l.getY(),l.getZ());
//						fw.shoot(force.getX(), force.getY(), force.getZ(), 1.0f, 1.0f);
//						fw.world.addEntity(fw);
//						fw.shooter = ((CraftPlayer)p).getHandle();
//						for (Player pp : Bukkit.getOnlinePlayers()){
//							((CraftPlayer)pp).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(fw.getId()));
//						}
//					}
//					world.getWorld().playSound(getBukkitEntity().getLocation(), Sound.WITHER_SHOOT, 1.0f, 2.0f);
//					fireballCooldown = (GTS.config.walkerFireballCooldown*20)-1;
//
//				}
//			}.runTask(GTS.instance);
//		} else if (type == VehicleType.TANK){
//			Bukkit.getScheduler().runTaskLater(GTS.instance, new Runnable(){
//				public void run(){
//					if (fireballCooldown > 0){
//						if (fireballCooldown % 5 == 0) ((Player)passenger.getBukkitEntity()).sendMessage("§eTank fireball §9is on a cooldown for another §b "+(fireballCooldown/20)+" seconds.");
//						return;
//					}
//					((Player)passenger.getBukkitEntity()).launchProjectile(Fireball.class);
//					fireballCooldown = (GTS.config.tankFireballCooldown*20)-1;
//					world.getWorld().playSound(getBukkitEntity().getLocation(), Sound.WITHER_SHOOT, 1.0f, 1.0f);
//				}
//			}, 1);
//		} else {
//			jump = true;
//		}
		jump = true;
	}


	@Override
	public void shift() {
//		((Player)passenger.getBukkitEntity()).setLevel(0);
//		((Player)passenger.getBukkitEntity()).setExp(0);
//		this.getBukkitEntity().eject();
		shift=true;
	}

	@Override
	public void forward() {
		this.forward=1;
	}

	@Override
	public void backward() {
		this.forward=-1;
	}

	@Override
	public void reset(){
		forward=0;
		sideways=0;
		jump=false;
		shift=false;
	}

	//
	//	@Override
	//	public void ride(Player p) {
	//		LazerPlugin.playSound(p.getLocation(), "mob.irongolem.hit-1.0-1.0");
	//		this.team = _._(p);
	//		((CraftPlayer)p).getHandle().playerConnection.sendPacket(new PacketPlayOutAttachEntity(0,((CraftPlayer)p).getHandle(),this));
	//		for (Player pp : Bukkit.getOnlinePlayers()){
	//			pp.hidePlayer(p);
	//		}
	//		reloadNameTag();
	//		LazerPlugin.addPlayerToVehicle(p);
	//	}
	//
	//	@Override
	//	public void dismount(final Player p) {
	//		LazerPlugin.playSound(p.getLocation(), "mob.irongolem.hit-1.0-1.0");
	//		p.setExp(0.0f);
	//		p.setLevel(0);
	//		if (!dead)
	//			for (Player pp : Bukkit.getOnlinePlayers()){
	//				if (pp != null && p != null) pp.showPlayer(p);
	//			}
	//		VehicleUtils.vehicles.remove(p);
	//		if (driver == p)
	//			setDriver(null);
	//		if (gunner == p)
	//			setGunner(null);
	//		for (int i = 0; i < getMaxPassengers(); i++){
	//			if (thepassengers[i] != null && thepassengers[i].equals(p)) {
	//				setPassenger(null,i);
	//				break;
	//			}
	//		}
	//		((CraftPlayer)p).getHandle().playerConnection.sendPacket(new PacketPlayOutAttachEntity(0,((CraftPlayer)p).getHandle(),null));
	//		p.teleport(this.getBukkitEntity().getLocation());
	//		reloadNameTag();
	//		LazerPlugin.removePlayerFromVehicle(p);
	//		if (!SW.shuttingDown && !dead){
	//			Bukkit.getScheduler().runTaskLater(SW.instance, new Runnable(){
	//				public void run(){
	//					StoredPlayerData d = SW.instance.stored.get(p);
	//					Inventory inv = d.playerInventory;
	//					for (int i = 0; i < inv.getSize(); i++){
	//						if (i == 8) continue;
	//						p.getInventory().setItem(i, inv.getItem(i) == null ? new ItemStack(0) : inv.getItem(i));
	//					}
	//				}
	//			}, 1);
	//		}
	//		if (!hasDriver() && !hasGunner() && !hasPassengers()){
	//			team = -1;
	//			reloadNameTag();
	//		}
	//	}

	public void reloadNameTag(){
		String health = "§9[";
		double percent = this.getHealth()/((CraftLivingEntity)this.getBukkitEntity()).getMaxHealth();
		for (double i = 0.0; i <= 1.0; i+=0.1){
			if (i <= percent){
				health+="§a|";
			} else {
				health+="§c|";
			}
		}
		health+="§9]";
		List<EntityPlayer> people = getPeople();
		String title = people.size() == 0 ? type.toString() : "§e ";
		for (int i = 0 ; i < people.size(); i++)
			title += people.get(i).getName()+ (i == people.size() - 1 ? "" : ", ");
		((LivingEntity)this.getBukkitEntity()).setCustomName("§f§l" + title + " " + health);
		((LivingEntity)this.getBukkitEntity()).setCustomNameVisible(true);
	}

	private double currentXRot, currentYRot, currentZRot, targetXRot, targetYRot, targetZRot;
	private double lerpSpeed = 0.2;

	private void setTilt (double xRot, double yRot, double zRot) {
		targetXRot = xRot;
		targetYRot = yRot;
		targetZRot = zRot;
	}

	private void tilt () {
		((ArmorStand)getBukkitEntity()).setHeadPose(new EulerAngle(Math.toRadians(currentXRot = lerp(currentXRot, targetXRot, lerpSpeed)),
				Math.toRadians(currentYRot = lerp(currentYRot, targetYRot, lerpSpeed)),
				Math.toRadians(currentZRot = lerp(currentZRot, targetZRot, lerpSpeed))));
	}
	private double lerp (double from, double to, double step) {
		return from + (to - from) * step;
	}

	public static enum Position{
		DRIVER,
		GUNNER,
		PASSENGER;
	}


	public static enum VehicleType {

		SPEEDER(false,40,new ItemStack(org.bukkit.Material.DRAGON_EGG,1),0.01f,0.7f,false,1),
		TANK(false,66,new ItemStack(org.bukkit.Material.HUGE_MUSHROOM_1,1),0.035f,0.2f,true,2),
		BIKE(false,50,new ItemStack(org.bukkit.Material.SPONGE,1),0.02f,0.5f,false,0),
		FIGHTER(true,54,new ItemStack(org.bukkit.Material.HUGE_MUSHROOM_2,1),0.006f,0.8f,false,1),
		BOMBER(true,60,new ItemStack(org.bukkit.Material.COMMAND,1),0.005f,0.5f,true,0);

		public boolean flying, gunner;
		int maxPassengers, health;
		org.bukkit.inventory.ItemStack headItem;
		float accelerationRate, maxAcceleration;
		private VehicleType(boolean flying, int health, org.bukkit.inventory.ItemStack headItem, float accelerationRate, float maxAcceleration, boolean gunner, int maxPassengers){
			this.flying = flying;
			this.headItem = headItem;
			this.accelerationRate = accelerationRate;
			this.maxAcceleration = maxAcceleration;
			this.gunner = gunner;
			this.maxPassengers = maxPassengers;
			this.health = health;
		}
	}
}
