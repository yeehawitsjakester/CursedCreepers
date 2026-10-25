package net.yeehawitsjake.cursedcreepers.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.yeehawitsjake.cursedcreepers.Config;
import net.yeehawitsjake.cursedcreepers.CursedCreepers;

/**
 * Herobrine, standing at the edge of the trees a fair way off, staring at one player: the only one who can see him. He
 * never moves. Out of the corner of their eye he's there; the moment they turn to look at him, or come close, he's
 * gone, sometimes with a rustle, sometimes with a word. He can't be hurt or touched, isn't saved with the world, and
 * leaves on his own after a while.
 */
public class EntityWatcher extends EntityLiving {

    /** data watcher slot with the entity id of the player he's watching, so their client can show him */
    private static final int DW_TARGET = 20;

    private int lifetime = 20 * 90;

    public EntityWatcher(World world) {
        super(world);
        setSize(0.6F, 1.8F);
        isImmuneToFire = true;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(DW_TARGET, Integer.valueOf(-1));
    }

    /** the player he watches, and how long he stays if they never look */
    public void watch(EntityPlayerMP player, int ticks) {
        dataWatcher.updateObject(DW_TARGET, Integer.valueOf(player.getEntityId()));
        lifetime = ticks;
        faceVictim(player);
    }

    @Override
    public boolean writeToNBTOptional(NBTTagCompound tag) {
        return false; // never saved: after a reload he'd be watching nobody
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (worldObj.isRemote || isDead) {
            return;
        }
        Entity victim = worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(DW_TARGET));
        if (!(victim instanceof EntityPlayerMP) || !victim.isEntityAlive() || ticksExisted > lifetime) {
            setDead();
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) victim;
        faceVictim(player);
        if (isLookedAt(player)
            || getDistanceSqToEntity(player) < Config.watcherVanishDistance * Config.watcherVanishDistance) {
            setDead();
            int roll = rand.nextInt(3);
            if (roll == 0) {
                // something in the leaves, where he was; only they hear it
                player.playerNetServerHandler.sendPacket(
                    new S29PacketSoundEffect("step.grass", posX, posY, posZ, 1.5F, 0.6F + rand.nextFloat() * 0.2F));
            } else if (roll == 1) {
                player.playerNetServerHandler.sendPacket(
                    new S29PacketSoundEffect("cursedcreepers:bold", posX, posY + getEyeHeight(), posZ, 2.5F, 1.0F));
            }
        }
    }

    /** whether their crosshair is within the vanish angle of his face */
    public boolean isLookedAt(EntityPlayerMP player) {
        Vec3 look = player.getLook(1.0F)
            .normalize();
        Vec3 toIt = Vec3
            .createVectorHelper(
                posX - player.posX,
                posY + getEyeHeight() - (player.posY + player.getEyeHeight()),
                posZ - player.posZ)
            .normalize();
        return look.dotProduct(toIt) > Math.cos(Math.toRadians(Config.watcherVanishAngle));
    }

    private void faceVictim(EntityPlayerMP player) {
        double dx = player.posX - posX, dz = player.posZ - posZ;
        double dy = player.posY + player.getEyeHeight() - (posY + getEyeHeight());
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(dy, MathHelper.sqrt_double(dx * dx + dz * dz)) * 180.0 / Math.PI);
        rotationYaw = renderYawOffset = rotationYawHead = yaw;
        rotationPitch = pitch;
    }

    /** on clients, only the player he's watching can see him */
    @Override
    public boolean isInvisible() {
        if (worldObj.isRemote) {
            return !CursedCreepers.proxy.isLocalPlayer(dataWatcher.getWatchableObjectInt(DW_TARGET));
        }
        return super.isInvisible();
    }

    // ---------------------------------------------------------------- isn't really there

    @Override
    protected boolean isAIEnabled() {
        return true; // no tasks: he just stands there (the old AI would wander and turn his head)
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt bolt) {}

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isEntityInvulnerable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false; // can't be aimed at, hit or shot
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void collideWithEntity(Entity entity) {}

    @Override
    protected void fall(float distance) {}

    @Override
    protected void func_145780_a(int x, int y, int z, Block block) {} // no footsteps

    @Override
    protected boolean interact(EntityPlayer player) {
        return false;
    }

    @Override
    protected void dropFewItems(boolean hitByPlayer, int looting) {}
}
