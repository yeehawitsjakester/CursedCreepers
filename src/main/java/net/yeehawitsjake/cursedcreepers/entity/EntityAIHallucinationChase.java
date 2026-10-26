package net.yeehawitsjake.cursedcreepers.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.yeehawitsjake.cursedcreepers.Config;

/**
 * The chase. Vanilla mob movement steers towards each path point and only moves on within about half a block of it,
 * which at this speed overshoots and loops round the points. Instead the creeper glides along its path at exactly its
 * chase speed (it isn't really there, so it doesn't need physics), re-planning a few times a second as the target
 * moves. It reports how far it still has to go, which the lock-on sound is timed by, and gives up on a target it
 * can't reach for a few seconds.
 */
public class EntityAIHallucinationChase extends EntityAIBase {

    private static final int REPLAN_TICKS = 2; // at ~5 blocks a tick, a stale path soon misses them
    private static final int GIVE_UP_TICKS = 60;

    private final EntityHallucinationCreeper creeper;
    private PathEntity path;
    private int replan;
    private int unreachable;

    public EntityAIHallucinationChase(EntityHallucinationCreeper creeper) {
        this.creeper = creeper;
        setMutexBits(3); // movement and looking
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase target = creeper.getAttackTarget();
        return target != null && target.isEntityAlive();
    }

    @Override
    public void startExecuting() {
        replan = 0;
        unreachable = 0;
        path = null;
    }

    @Override
    public void resetTask() {
        path = null;
        creeper.setRemainingDistance(-1);
    }

    @Override
    public void updateTask() {
        EntityLivingBase target = creeper.getAttackTarget();
        if (target == null) {
            return;
        }
        if (--replan <= 0) {
            replan = REPLAN_TICKS;
            PathEntity fresh = creeper.pathTo(target);
            if (fresh != null) {
                path = fresh;
                unreachable = 0;
            } else if ((unreachable += REPLAN_TICKS) >= GIVE_UP_TICKS) {
                creeper.giveUpOn(target); // they've gone somewhere it can't follow
                return;
            }
        }

        double budget = Config.hallucinationChaseSpeed / 20.0; // blocks this tick
        double x = creeper.posX, y = creeper.posY, z = creeper.posZ;
        while (budget > 0 && path != null && !path.isFinished()) {
            PathPoint p = path.getPathPointFromIndex(path.getCurrentPathIndex());
            double px = p.xCoord + 0.5, py = p.yCoord, pz = p.zCoord + 0.5;
            double d = dist(x, y, z, px, py, pz);
            if (d <= budget) {
                x = px;
                y = py;
                z = pz;
                budget -= d;
                path.incrementPathIndex();
            } else {
                x += (px - x) / d * budget;
                y += (py - y) / d * budget;
                z += (pz - z) / d * budget;
                budget = 0;
            }
        }
        if (budget > 0) {
            // past the last point: straight at them for the last stretch
            double d = dist(x, y, z, target.posX, target.posY, target.posZ);
            double step = Math.min(budget, d);
            if (d > 0) {
                x += (target.posX - x) / d * step;
                y += (target.posY - y) / d * step;
                z += (target.posZ - z) / d * step;
            }
        }

        double dx = x - creeper.posX, dz = z - creeper.posZ;
        if (dx * dx + dz * dz > 1.0E-4) {
            float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
            creeper.rotationYaw = yaw;
            creeper.rotationYawHead = yaw;
            creeper.renderYawOffset = yaw;
        }
        creeper.setPosition(x, y, z);
        creeper.motionX = creeper.motionY = creeper.motionZ = 0;
        creeper.getLookHelper()
            .setLookPositionWithEntity(target, 30.0F, 30.0F);
        creeper.setRemainingDistance((float) remaining(x, y, z, target));
    }

    /** along the rest of the path, then to the target */
    private double remaining(double x, double y, double z, EntityLivingBase target) {
        double total = 0;
        if (path != null) {
            for (int i = path.getCurrentPathIndex(); i < path.getCurrentPathLength(); i++) {
                PathPoint p = path.getPathPointFromIndex(i);
                double px = p.xCoord + 0.5, py = p.yCoord, pz = p.zCoord + 0.5;
                total += dist(x, y, z, px, py, pz);
                x = px;
                y = py;
                z = pz;
            }
        }
        total += dist(x, y, z, target.posX, target.posY, target.posZ);
        return Math.max(0, total - Config.hallucinationVanishDistance);
    }

    private static double dist(double x, double y, double z, double x2, double y2, double z2) {
        double dx = x2 - x, dy = y2 - y, dz = z2 - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
