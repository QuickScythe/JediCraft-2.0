package com.mineswine.jedicraft.vehicle;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.mineswine.jedicraft.Main;

import net.minecraft.server.v1_9_R1.PacketPlayInSteerVehicle;

/**
 * Represents a listener for reading player input (W, A, S, D, Shift, Space)
 * <b>Requires ProtocolLibrary</b>
 * @authors dillyg10, Cameron Witcher
 *
 */
public class WASDProtocolListener {

	private ProtocolManager manager;			 //Manager for registering/canceling protocol events
	private Plugin plugin;					//Plugin for this listener
	private PacketSteerListener listener;	//An instance of the listening class
	/**
	 * Constructs a new PrtocolListener
	 * @param manager The protocolmanager to be used .
	 * @param plugin The plugin to register events to.
	 */
	public WASDProtocolListener(ProtocolManager manager, Plugin plugin){
		this.manager = manager;
		this.plugin = plugin;
	}
	
	/**
	 * Registers the listener, and inits it.
	 */
	public void registerlistener(){
		listener = new PacketSteerListener(plugin);
		manager.addPacketListener(listener);
	}
	
	/**
	 * Cancels the listener
	 */
	public void cancelListener(){
		manager.removePacketListener(listener);
	}
	
	/**
	 * Represents an extends of the PrtocolLibrarry PacketAdapter class for reading WASD input
	 * @author dillyg10
	 *
	 */
	public class PacketSteerListener extends PacketAdapter {
		/**
		 * Constructs a new PacketListener
		 * @param plugin The plugin to register revents with. 
		 */
		public PacketSteerListener(Plugin plugin){
			super(plugin,ListenerPriority.HIGH,PacketType.Play.Client.STEER_VEHICLE);
		}

		@Override
		public void onPacketReceiving (final com.comphenix.protocol.events.PacketEvent e) {
//			Bukkit.broadcastMessage(e.getPacketType().toString());
			if(e.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
				if(e.getPacket().getHandle() instanceof PacketPlayInSteerVehicle) {
//					if (e.getPlayer().getVehicle() != null && e.getPlayer().getVehicle() instanceof ArmorStand){
//						e.setCancelled(true);
//						return;
//					}
					boolean shift =		((PacketPlayInSteerVehicle)e.getPacket().getHandle()).d();

//					Bukkit.broadcastMessage(String.valueOf(e.getPlayer().hasMetadata("vehicle")));
					if (!e.getPlayer().hasMetadata("vehicle")){
						if (shift) {
							e.setCancelled(true);
							return;
						}
						return;
					}
					GTSVehicle ent = (GTSVehicle) e.getPlayer().getMetadata("vehicle").get(0).value();
					e.setCancelled(true);
					boolean jump = 		((PacketPlayInSteerVehicle)e.getPacket().getHandle()).c();
					double sideways =		((PacketPlayInSteerVehicle)e.getPacket().getHandle()).a();
					double forward = 	((PacketPlayInSteerVehicle)e.getPacket().getHandle()).b();
					if (shift){
						if (!ent.type.flying)
							Bukkit.getScheduler().runTask(Main.getPlugin(), new Runnable() {
								public void run() {
									try{
										((GTSVehicle) e.getPlayer().getMetadata("vehicle").get(0).value()).dismount(((CraftPlayer) e.getPlayer()).getHandle());
									} catch (Exception ex){}
								}
							});
					}
					if (ent.type.flying && jump && shift)
						Bukkit.getScheduler().runTask(Main.getPlugin(), new Runnable() {
							public void run() {
								try {
									((GTSVehicle) e.getPlayer().getMetadata("vehicle").get(0).value()).dismount(((CraftPlayer) e.getPlayer()).getHandle());
								} catch (Exception ex){}
							}
						});

					if (!((CraftPlayer)e.getPlayer()).getHandle().equals(ent.driver)) return;


					ent.reset();
					if (jump) ent.jump();
					if (shift)
						if (ent.type.flying) ent.shift();
					if (forward > 0) ent.forward();
					if (forward < 0) ent.backward();
					if (sideways > 0) ent.left();
					if (sideways < 0) ent.right();
				}
			}
		}
	}
}
