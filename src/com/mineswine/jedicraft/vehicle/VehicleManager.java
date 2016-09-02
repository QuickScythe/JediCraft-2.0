package com.mineswine.jedicraft.vehicle;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import com.mineswine.jedicraft.Main;
import com.mineswine.jedicraft.vehicle.GTSVehicle.VehicleType;

public class VehicleManager {

	public static Map<Integer,Class<? extends GTSVehicle>> vehicles = new HashMap<Integer,Class<? extends GTSVehicle>>();

	public static void spawnEntity(Integer id, Location where){
		try {
			VehicleType vehicleType = null;
			switch (id){
				case 91:
					vehicleType = VehicleType.SPEEDER;
					break;
				case 92:
					vehicleType = VehicleType.TANK;
					break;
				case 99:
					vehicleType = VehicleType.BIKE;
					break;
				case 55:
					vehicleType = VehicleType.FIGHTER;
					break;
				case 56:
					vehicleType = VehicleType.BOMBER;
					break;
			}
			GTSVehicle e = new GTSVehicle(((CraftWorld)where.getWorld()).getHandle(),vehicleType);
			e.setPosition(where.getX(), where.getY() + 0.2, where.getZ());
			e.world.addEntity(e);
			((LivingEntity)e.getBukkitEntity()).setMaxHealth(e.type.health);
			((LivingEntity)e.getBukkitEntity()).setHealth(e.type.health);
			((GTSVehicle)e).reloadNameTag();
			e.getBukkitEntity().setMetadata("spawn", new FixedMetadataValue(Main.getPlugin(), where));
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
