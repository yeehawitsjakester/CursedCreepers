package net.yeehawitsjake.cursedcreepers.entity;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.yeehawitsjake.cursedcreepers.Config;
import net.yeehawitsjake.cursedcreepers.CursedCreepers;

/**
 * A creeper that isn't there. It spots a player from far away, runs at them absurdly fast, and the moment it reaches
 * them it's simply gone: no drops, and usually no explosion (some go off, harmlessly to blocks). Only the player it's
 * after can see it, everyone near can hear it coming, nothing can hurt or touch it, and it makes no footsteps.
 */
public class EntityHallucinationCreeper extends EntityCreeper {

    /** data watcher slot with the entity id of the player it's after (-1: nobody), so their client can show it */
    private static final int DW_TARGET = 20;
    /** gives up and fades away after this long without anyone to chase */
    private static final int MAX_IDLE_TICKS = 20 * 120;

    private int idleTicks;
    /** goes off when it reaches its target (decided when it spawns, saved with it) */
    private boolean explodes;
    /** client: the target last seen in the data watcher, to notice when it locks on to us */
    private int seenTarget = -1;

    public EntityHallucinationCreeper(World world) {
        super(world);
        // the creeper's own AI would swell and explode; replace all of it
        tasks.taskEntries.clear();
        targetTasks.taskEntries.clear();
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, true));
        tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, (float) Config.hallucinationSightRange));
        tasks.addTask(4, new EntityAIWander(this, 0.3D));
        tasks.addTask(5, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        isImmuneToFire = true;
        experienceValue = 0;
        if (world != null && !world.isRemote) {
            explodes = world.rand.nextDouble() * 100 < Config.hallucinationExplodeChance;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setBoolean("Explodes", explodes);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        explodes = tag.getBoolean("Explodes");
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(Config.hallucinationSpeed);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(Config.hallucinationSightRange);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(DW_TARGET, Integer.valueOf(-1));
    }

    /** whether it's after this player (works on clients too) */
    public boolean isChasing(EntityPlayer player) {
        return player != null && dataWatcher.getWatchableObjectInt(DW_TARGET) == player.getEntityId();
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (worldObj.isRemote) {
            int target = dataWatcher.getWatchableObjectInt(DW_TARGET);
            if (target != seenTarget) {
                seenTarget = target;
                Entity victim = target < 0 ? null : worldObj.getEntityByID(target);
                if (victim instanceof EntityPlayer) {
                    CursedCreepers.proxy.playLockOn(this, (EntityPlayer) victim); // it's seen someone
                }
            }
            return;
        }
        if (isDead) {
            return;
        }
        EntityLivingBase target = getAttackTarget();
        int targetId = target == null ? -1 : target.getEntityId();
        if (dataWatcher.getWatchableObjectInt(DW_TARGET) != targetId) {
            dataWatcher.updateObject(DW_TARGET, Integer.valueOf(targetId));
        }

        if (target != null) {
            idleTicks = 0;
            double reach = Config.hallucinationVanishDistance;
            if (getDistanceSqToEntity(target) <= reach * reach) {
                arrive(target);
            }
        } else if (++idleTicks > MAX_IDLE_TICKS) {
            setDead();
        }
    }

    /** on clients, only the player it's chasing can see it */
    @Override
    public boolean isInvisible() {
        if (worldObj.isRemote) {
            return !CursedCreepers.proxy.isLocalPlayer(dataWatcher.getWatchableObjectInt(DW_TARGET));
        }
        return super.isInvisible();
    }

    // ---------------------------------------------------------------- never explodes

    @Override
    public int getCreeperState() {
        return -1;
    }

    @Override
    protected boolean interact(EntityPlayer player) {
        return false; // no lighting it with flint and steel
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt bolt) {}

    // ---------------------------------------------------------------- isn't really there

    @Override
    public boolean attackEntityAsMob(Entity target) {
        if (!isDead) {
            arrive(target); // close enough to swing at them: it reaches them instead
        }
        return false;
    }

    /** it got to them: gone, maybe with a bang, and once in a hundred million times it takes the server with it */
    private void arrive(Entity target) {
        setDead();
        if (explodes) {
            // a real explosion would knock out paintings and item frames even without breaking blocks: fake one
            worldObj.playSoundEffect(
                posX,
                posY,
                posZ,
                "random.explode",
                4.0F,
                (1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F) * 0.7F);
            ((WorldServer) worldObj).func_147487_a("hugeexplosion", posX, posY + 1, posZ, 1, 0, 0, 0, 0);
            target.attackEntityFrom(
                new EntityDamageSource("explosion.player", this).setDifficultyScaled()
                    .setExplosion(),
                (float) Config.hallucinationExplodeDamage);
        }
        if (rand.nextDouble() * 100 < Config.crashChance) {
            throw new ReportedException(
                CrashReport.makeCrashReport(
                    new IllegalStateException("It was never there."),
                    "Being reached by a Hallucination Creeper"));
        }
    }

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
    protected void dropFewItems(boolean hitByPlayer, int looting) {}

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return 0;
    }
}
