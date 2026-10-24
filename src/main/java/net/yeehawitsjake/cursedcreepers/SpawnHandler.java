package net.yeehawitsjake.cursedcreepers;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.yeehawitsjake.cursedcreepers.entity.EntityHallucinationCreeper;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** turns a share of naturally spawned creepers into Hallucination Creepers */
public class SpawnHandler {

    /** natural spawns only: by the time this fires the creeper passed its light and space checks and is in the world */
    @SubscribeEvent
    public void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
        if (event.world.isRemote || event.entityLiving.getClass() != EntityCreeper.class
            || event.world.rand.nextDouble() * 100 >= Config.hallucinationReplaceChance) {
            return;
        }
        EntityHallucinationCreeper ghost = new EntityHallucinationCreeper(event.world);
        ghost.setLocationAndAngles(event.x, event.y, event.z, event.entityLiving.rotationYaw, 0f);
        event.world.spawnEntityInWorld(ghost);
        event.entityLiving.setDead();
        event.setCanceled(true); // skip the creeper's spawn setup
    }
}
