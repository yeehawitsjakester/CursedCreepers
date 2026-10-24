package net.yeehawitsjake.cursedcreepers.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderCreeper;
import net.yeehawitsjake.cursedcreepers.client.LockOnSound;
import net.yeehawitsjake.cursedcreepers.entity.EntityHallucinationCreeper;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        // looks exactly like a creeper: that's the point
        RenderingRegistry.registerEntityRenderingHandler(EntityHallucinationCreeper.class, new RenderCreeper());
    }

    @Override
    public void playLockOn(EntityHallucinationCreeper creeper) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getSoundHandler()
            .playSound(LockOnSound.create(creeper, mc.thePlayer));
    }

    @Override
    public boolean isLocalPlayer(int entityId) {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.thePlayer != null && mc.thePlayer.getEntityId() == entityId;
    }
}
