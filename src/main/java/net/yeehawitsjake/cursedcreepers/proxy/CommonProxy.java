package net.yeehawitsjake.cursedcreepers.proxy;

public class CommonProxy {

    public void registerRenderers() {}

    /** client only: is this entity id the player at the keyboard? */
    public boolean isLocalPlayer(int entityId) {
        return false;
    }
}
