package com.mineswine.jedicraft.vehicle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import com.mineswine.jedicraft.utils.SerializationHelper;

import java.util.Map;


@SerializableAs("VehicleSpawn")
public class VehicleSpawn implements ConfigurationSerializable {

	public Vector loc;
	public int spawnTime;
	public int mobId;
	public transient int tick;
	
	public VehicleSpawn(Vector loc, int mobId, int spawnTime){
		this.loc = loc;
		this.spawnTime = spawnTime;
		this.mobId = mobId;
	}
	
	public VehicleSpawn(Map<String,Object> data){
		SerializationHelper.deserialize(getClass(), false, this, data, true);
		tick = 5*60;
	}
	
	public void run(){
		Location l = loc.toLocation(Bukkit.getWorld("TAttoine2"));
		tick++;
		if (l.getChunk().isLoaded()){
			for (Entity e : l.getChunk().getEntities()){
				if (e.getType() == EntityType.ARMOR_STAND){
					if (e.getLocation().distance(l) < 2) {
//						e.remove();

						return;
					}
				}
			}
		} else {
			return;
		}
		if (tick >= 5*60){
			for (Entity e : l.getChunk().getEntities()){
				if (e.getType() == EntityType.ARMOR_STAND){
					if (e.getLocation().distance(l) < 2) {
						e.remove();
					}
				}
			}
			VehicleManager.spawnEntity(mobId, l);
			tick = 0;
		}
	}

	@Override
	public Map<String, Object> serialize() {
		return SerializationHelper.serialize(getClass(), false, this);
	}
}
