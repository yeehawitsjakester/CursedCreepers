package net.yeehawitsjake.cursedcreepers.client;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.yeehawitsjake.cursedcreepers.Config;
import net.yeehawitsjake.cursedcreepers.entity.EntityHallucinationCreeper;

/**
 * The sound a Hallucination Creeper makes while it runs at someone: the first part of the meme clip, at its own speed,
 * from where the creeper is, over again if the chase outlasts it, until it reaches them (when the second part plays as
 * its "explosion": see EntityHallucinationCreeper.arrive). The player it's after hears it across its whole sight
 * range, and anyone else within the sound range hears it fade with their distance, like proximity chat.
 */
public class LockOnSound extends MovingSound {

    private final EntityHallucinationCreeper creeper;
    private final EntityPlayer player;

    public static LockOnSound create(EntityHallucinationCreeper creeper, EntityPlayer player, boolean forMe) {
        return new LockOnSound(creeper, player, forMe);
    }

    private LockOnSound(EntityHallucinationCreeper creeper, EntityPlayer player, boolean forMe) {
        super(new ResourceLocation("cursedcreepers", "approach"));
        this.creeper = creeper;
        this.player = player;
        repeat = true; // a long chase hears it again
        // Minecraft fades a sound out linearly over 16 blocks per unit of volume: the victim hears it across its whole
        // sight range, getting louder as it comes; bystanders within the sound range, quieter the further they are
        double range = forMe ? Math.max(Config.hallucinationSoundRange, Config.hallucinationSightRange)
            : Config.hallucinationSoundRange;
        this.volume = (float) Math.max(1, range / 16);
        moveTo();
    }

    @Override
    public void update() {
        if (creeper.isDead || player.isDead || !creeper.isChasing(player)) {
            donePlaying = true; // it's reached them, or gone: so is the sound
            return;
        }
        moveTo();
    }

    private void moveTo() {
        xPosF = (float) creeper.posX;
        yPosF = (float) creeper.posY;
        zPosF = (float) creeper.posZ;
    }
}
