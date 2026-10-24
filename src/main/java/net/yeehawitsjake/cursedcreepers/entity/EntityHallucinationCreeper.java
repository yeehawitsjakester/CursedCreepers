package net.yeehawitsjake.cursedcreepers.entity;

import net.minecraft.block.Block;
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
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.yeehawitsjake.cursedcreepers.Config;
import net.yeehawitsjake.cursedcreepers.CursedCreepers;

/**
 * A creeper that isn't there. It spots a player from far away, runs at them absurdly fast, and the moment it reaches
 * them it's simply gone: no explosion, no particles, no drops. Only the player it's after can see it, nothing can
 * hurt or touch it, and it makes no footsteps.
 */
public class EntityHallucinationCreeper extends EntityCreeper {

    /** data watcher slot with the entity id of the player it's after (-1: nobody), so their client can show it */
    private static final int DW_TARGET = 20;
    /** gives up and fades away after this long without anyone to chase */
    private static final int MAX_IDLE_TICKS = 20 * 120;

    private int idleTicks;

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

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (worldObj.isRemote || isDead) {
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
                setDead(); // ...it was never there
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
        setDead(); // close enough to swing at them: it vanishes instead
        return false;
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
