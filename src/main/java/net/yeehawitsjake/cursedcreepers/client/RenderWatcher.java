package net.yeehawitsjake.cursedcreepers.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/** Herobrine: a player model with his skin, and white eyes that glow in the dark like an enderman's */
public class RenderWatcher extends RenderLiving {

    private static final ResourceLocation SKIN = new ResourceLocation(
        "cursedcreepers",
        "textures/entity/herobrine.png");
    private static final ResourceLocation EYES = new ResourceLocation(
        "cursedcreepers",
        "textures/entity/herobrine_eyes.png");

    public RenderWatcher() {
        super(new ModelBiped(), 0.5F);
        setRenderPassModel(new ModelBiped());
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return SKIN;
    }

    /** the eyes, drawn again at full brightness; vanilla draws this pass even for invisible mobs, so skip it then */
    @Override
    protected int shouldRenderPass(EntityLivingBase entity, int pass, float partialTicks) {
        if (pass != 0 || entity.isInvisible()) {
            return -1;
        }
        bindTexture(EYES);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(true);
        int bright = 61680;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, bright % 65536, bright / 65536);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        return 1;
    }
}
