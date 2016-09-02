package com.mineswine.jedicraft.vehicle;

import net.minecraft.server.v1_9_R1.AxisAlignedBB;
import net.minecraft.server.v1_9_R1.DamageSource;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityHuman;
import net.minecraft.server.v1_9_R1.EntityItem;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.EnumParticle;
import net.minecraft.server.v1_9_R1.ItemStack;
import net.minecraft.server.v1_9_R1.MathHelper;
import net.minecraft.server.v1_9_R1.MovingObjectPosition;
import net.minecraft.server.v1_9_R1.MovingObjectPosition.EnumMovingObjectType;
import net.minecraft.server.v1_9_R1.Vec3D;
import net.minecraft.server.v1_9_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class Saber extends EntityItem {

	public Player thrower;
	public boolean returning = false;
	public boolean nohurt = false;
	public Saber(World world, ItemStack itm, double d0, double d1, double d2, Player thrower) {
		super(world,d0,d1,d2,itm);
		this.thrower=thrower;
	}
	public void t_() {
		if (getItemStack() == null) {
			die();
		} else {
			if (this.onGround) die();
			if (this.ticksLived >= 5*20){
				die();
				thrower.getInventory().addItem(CraftItemStack.asBukkitCopy(getItemStack()));
				return;
			}
			if (returning){
				Location throwerloc = thrower.getEyeLocation();
				Vector newVelcoity = throwerloc.toVector().subtract(getBukkitEntity().getLocation().toVector());
				newVelcoity = newVelcoity.multiply(0.5);
				this.motX=newVelcoity.getX();
				this.motY=newVelcoity.getY();
				this.motZ=newVelcoity.getZ();
				this.velocityChanged=true;
			}
			if (!returning && this.getBukkitEntity().getLocation().distance(thrower.getEyeLocation()) > 20){
				Location throwerloc = thrower.getEyeLocation();
				this.returning = true;
				Vector newVelcoity = throwerloc.toVector().subtract(getBukkitEntity().getLocation().toVector());
				newVelcoity = newVelcoity.multiply(0.5);
				this.motX=newVelcoity.getX();
				this.motY=newVelcoity.getY();
				this.motZ=newVelcoity.getZ();
				this.velocityChanged=true;
			}
			//			super.h();
//			K();
			//			setOnFire(1);
			Vec3D vec3d = new Vec3D(this.locX, this.locY, this.locZ);
			Vec3D vec3d1 = new Vec3D(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
			MovingObjectPosition movingobjectposition = this.world.rayTrace(vec3d,vec3d1);

			vec3d = new Vec3D(this.locX, this.locY, this.locZ);
			vec3d1 = new Vec3D(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
			if (movingobjectposition != null) {
				vec3d1 = new Vec3D(movingobjectposition.pos.x, movingobjectposition.pos.y, movingobjectposition.pos.z);
			}

			Entity entity = null;
			List list = this.world.getEntities(this, this.getBoundingBox().a(this.motX, this.motY, this.motZ).grow(1.0D, 1.0D, 1.0D));
			double d0 = 0.0D;

			for (int i = 0; i < list.size(); i++) {
				Entity entity1 = (Entity)list.get(i);
				/*&& ((!entity1.h(((CraftPlayer)this.thrower).getHandle()))*/
				//				if ((entity1.R())  /*(this.au >= 25)*/) {
				float f = 0.3F;
				AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow(f, f, f);
				MovingObjectPosition movingobjectposition1 = axisalignedbb.a(vec3d, vec3d1);
				if (movingobjectposition1 != null) {
					double d1 = vec3d.distanceSquared(movingobjectposition1.pos);
					if ((d1 < d0) || (d0 == 0.0D)) {
						entity = entity1;
						d0 = d1;
					}
				}
			}
			//			}
			if (entity != null) {
				movingobjectposition = new MovingObjectPosition(entity);
			}

			if (movingobjectposition != null) {
				a(movingobjectposition);
			}

			this.locX += this.motX;
			this.locY += this.motY;
			this.locZ += this.motZ;
			float f1 = MathHelper.sqrt(this.motX * this.motX + this.motZ * this.motZ);

			this.yaw = ((float)(Math.atan2(this.motZ, this.motX) * 180.0D / 3.141592741012573D) + 90.0F);

			for (this.pitch = ((float)(Math.atan2(f1, this.motY) * 180.0D / 3.141592741012573D) - 90.0F); this.pitch - this.lastPitch < -180.0F; this.lastPitch -= 360.0F);
			while (this.pitch - this.lastPitch >= 180.0F) {
				this.lastPitch += 360.0F;
			}

			while (this.yaw - this.lastYaw < -180.0F) {
				this.lastYaw -= 360.0F;
			}

			while (this.yaw - this.lastYaw >= 180.0F) {
				this.lastYaw += 360.0F;
			}

			this.pitch = (this.lastPitch + (this.pitch - this.lastPitch) * 0.2F);
			this.yaw = (this.lastYaw + (this.yaw - this.lastYaw) * 0.2F);
			float f2 = j_();

//			if (V()) {
//				for (int j = 0; j < 4; j++) {
//					float f3 = 0.25F;
//
//					this.world.addParticle(EnumParticle.WATER_BUBBLE, this.locX - this.motX * f3, this.locY - this.motY * f3, this.locZ - this.motZ * f3, this.motX, this.motY, this.motZ);
//				}
//
//				f2 = 0.8F;
//			}

			this.motX += this.motX;
			this.motY += this.motY;
			this.motZ += this.motZ;
//			this.motX *= f2;
//			this.motY *= f2;
//			this.motZ *= f2;
			this.world.addParticle(EnumParticle.FIREWORKS_SPARK, this.locX, this.locY, this.locZ, 0.0D, 0.0D, 0.0D);
			setPosition(this.locX, this.locY, this.locZ);
		}
	}

	protected float j_() {
		return 0.95F;
	}

	protected void a(MovingObjectPosition paramMovingObjectPosition){
		if (paramMovingObjectPosition.type == MovingObjectPosition.EnumMovingObjectType.MISS) return;
		if (paramMovingObjectPosition.type == EnumMovingObjectType.ENTITY && paramMovingObjectPosition.entity != null) {
			if (nohurt) return;
			//			if (this.invinc.contains(paramMovingObjectPosition.entity)) return;
			if (paramMovingObjectPosition.entity.equals(((CraftPlayer)thrower).getHandle())){
				return;
			} else {
				if (paramMovingObjectPosition.entity instanceof EntityPlayer){
					paramMovingObjectPosition.entity.damageEntity(DamageSource.playerAttack(((CraftPlayer)thrower).getHandle()), 5.0f);
				}
			}
		}
		die();
		if (returning) return;
		Location throwerloc = thrower.getEyeLocation();
		this.returning = true;
		Vector newVelcoity = throwerloc.toVector().subtract(getBukkitEntity().getLocation().toVector());
		newVelcoity = newVelcoity.multiply(0.06);
		this.motX=newVelcoity.getX();
		this.motY=newVelcoity.getY();
		this.motZ=newVelcoity.getZ();
		this.velocityChanged=true;
	}

	public boolean a(EntityItem entityitem) {
		return false;
	}

	protected void burn(int i) {
	}

	public boolean damageEntity(DamageSource damagesource, float f) {
		return false;
	}

	public void d(EntityHuman entityhuman){
		die();
		if (entityhuman.getName().equals(thrower.getName())) {
			return;
		}
		entityhuman.damageEntity(DamageSource.playerAttack(((CraftPlayer)thrower).getHandle()), 5.0f);
		if (true) return;
		nohurt = true;
		returning=true;
		if (!returning) return;
			die();
	}

}
