package net.yeehawitsjake.cursedcreepers.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.yeehawitsjake.cursedcreepers.entity.EntityHallucinationCreeper;

public class CommonProxy {

    public void registerRenderers() {}

    /** client only: start the sound of a Hallucination Creeper going after a player */
    public void playLockOn(EntityHallucinationCreeper creeper, EntityPlayer victim) {}

    /** client only: is this entity id the player at the keyboard? */
    public boolean isLocalPlayer(int entityId) {
        return false;
    }
}
