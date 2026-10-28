package net.yeehawitsjake.cursedcreepers.entity;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
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
    /** how far it still has to run to reach them, in blocks (-1: not chasing), for timing the sound */
    private static final int DW_REMAINING = 21;
    /** its chase speed in blocks per second, so clients time the sound with the server's setting */
    private static final int DW_SPEED = 22;
    /** after giving up on a player it can't reach, leaves them alone this long */
    private static final int GIVE_UP_COOLDOWN = 20 * 10;
    /** gives up and fades away after this long without anyone to chase */
    private static final int MAX_IDLE_TICKS = 20 * 120;

    private int idleTicks;
    /** goes off when it reaches its target (decided when it spawns, saved with it) */
    private boolean explodes;
    private int gaveUpOn = -1;
    private int gaveUpUntil;
    /** player entity id -> {tick checked, 1 if reachable}: working out a path is too slow to redo every tick */
    private final HashMap<Integer, int[]> reachable = new HashMap<Integer, int[]>();
    /** client: the target last seen in the data watcher, to notice when it locks on to us */
    private int seenTarget = -1;

    public EntityHallucinationCreeper(World world) {
        super(world);
        // the creeper's own AI would swell and explode; replace all of it
        tasks.taskEntries.clear();
        targetTasks.taskEntries.clear();
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIHallucinationChase(this));
        tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, (float) Config.hallucinationSightRange));
        tasks.addTask(4, new EntityAIWander(this, 0.8D));
        tasks.addTask(5, new EntityAILookIdle(this));
        // only locks on to players it can see and actually get to
        targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 5, true) {

            @Override
            protected boolean isSuitableTarget(EntityLivingBase target, boolean ignoreTeams) {
                return super.isSuitableTarget(target, ignoreTeams) && canReach(target);
            }
        });
        getNavigator().setAvoidsWater(false);
        getNavigator().setCanSwim(true);
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
        // its chase doesn't use this (see EntityAIHallucinationChase); it's only how fast it wanders, like a creeper
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(Config.hallucinationSightRange);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(DW_TARGET, Integer.valueOf(-1));
        dataWatcher.addObject(DW_REMAINING, Float.valueOf(-1));
        dataWatcher.addObject(DW_SPEED, Float.valueOf((float) Config.hallucinationChaseSpeed));
    }

    /**
     * a path all the way to them, or null. Vanilla's own reachability check only compares x and z, so a player on a
     * roof counted as reachable from the foot of the wall; this wants the path to end within reach in all three.
     */
    public PathEntity pathTo(EntityLivingBase target) {
        // straight to the pathfinder: the navigator won't plan for a mob that isn't on the ground, which one about to
        // be
        // spawned (or mid-glide) isn't. Same settings as its navigator: through open doors, swimming
        PathEntity path = worldObj.getPathEntityToEntity(
            this,
            target,
            (float) Config.hallucinationSightRange,
            true,
            false,
            getNavigator().getAvoidsWater(),
            true);
        if (path == null) {
            return null;
        }
        PathPoint end = path.getFinalPathPoint();
        double dx = end.xCoord + 0.5 - target.posX, dy = end.yCoord - target.posY, dz = end.zCoord + 0.5 - target.posZ;
        double reach = Config.hallucinationVanishDistance + 1.5;
        return dx * dx + dy * dy + dz * dz <= reach * reach ? path : null;
    }

    public boolean canReach(EntityLivingBase target) {
        if (target.getEntityId() == gaveUpOn && ticksExisted < gaveUpUntil) {
            return false;
        }
        int[] known = reachable.get(target.getEntityId());
        if (known == null || ticksExisted - known[0] >= 20) {
            known = new int[] { ticksExisted, pathTo(target) != null ? 1 : 0 };
            reachable.put(target.getEntityId(), known);
        }
        return known[1] == 1;
    }

    /** stop chasing someone it can't get to, and leave them be for a while */
    public void giveUpOn(EntityLivingBase target) {
        gaveUpOn = target.getEntityId();
        gaveUpUntil = ticksExisted + GIVE_UP_COOLDOWN;
        setAttackTarget(null);
    }

    public void setRemainingDistance(float blocks) {
        if (Math.abs(dataWatcher.getWatchableObjectFloat(DW_REMAINING) - blocks) > 0.01f) {
            dataWatcher.updateObject(DW_REMAINING, Float.valueOf(blocks));
        }
    }

    /** blocks left to run (-1 if unknown) */
    public float getRemainingDistance() {
        return dataWatcher.getWatchableObjectFloat(DW_REMAINING);
    }

    /** chase speed, blocks per second */
    public float getChaseSpeed() {
        return dataWatcher.getWatchableObjectFloat(DW_SPEED);
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

    /**
     * it got to them: gone, with the second part of the meme clip as its "explosion" (heard as far as its approach),
     * maybe a real bang's worth of damage, and once in a hundred million times it takes the server with it
     */
    private void arrive(Entity target) {
        setDead();
        worldObj.playSoundEffect(
            posX,
            posY,
            posZ,
            "cursedcreepers:arrive",
            (float) Math.max(1, Math.max(Config.hallucinationSoundRange, Config.hallucinationSightRange) / 16),
            1.0F);
        if (explodes) {
            // a real explosion would knock out paintings and item frames even without breaking blocks: fake one
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
