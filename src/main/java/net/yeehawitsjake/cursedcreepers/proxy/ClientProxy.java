package net.yeehawitsjake.cursedcreepers.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderCreeper;
import net.yeehawitsjake.cursedcreepers.entity.EntityHallucinationCreeper;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        // looks exactly like a creeper: that's the point
        RenderingRegistry.registerEntityRenderingHandler(EntityHallucinationCreeper.class, new RenderCreeper());
    }

    @Override
    public boolean isLocalPlayer(int entityId) {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.thePlayer != null && mc.thePlayer.getEntityId() == entityId;
    }
}
