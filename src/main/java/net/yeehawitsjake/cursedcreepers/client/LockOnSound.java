package net.yeehawitsjake.cursedcreepers.client;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.yeehawitsjake.cursedcreepers.Config;
import net.yeehawitsjake.cursedcreepers.entity.EntityHallucinationCreeper;

/**
 * The sound a Hallucination Creeper makes while it runs at someone, stretched to end the moment it reaches them. It
 * comes from where the creeper is: the player it's after hears it across its whole sight range, and anyone else within
 * the sound range hears it fade with their distance, like proximity chat. The timing always follows the victim's chase.
 *
 * The clip is resampled, so stretching it changes its pitch too, on purpose. Minecraft only plays sounds between half
 * and double speed, so three copies ship at 0.25x, 1x and 4x (tools/make-sounds.sh). The copy closest to the chase
 * time is picked when it locks on; after that, every tick, the rest of the clip is played over the time it has left.
 * That time is exact: the server sends how far along its path it still has to run, and it runs at a fixed speed.
 */
public class LockOnSound extends MovingSound {

    /** length of lockon.ogg at normal speed */
    private static final double BASE_SECONDS = 1.848;
    private static final double[] SPEEDS = { 0.25, 1, 4 };
    private static final String[] NAMES = { "lockon_slow", "lockon", "lockon_fast" };

    private final EntityHallucinationCreeper creeper;
    private final EntityPlayer player;
    /** seconds long, the chosen copy */
    private final double length;
    /** how far into the clip playback is, in seconds of the chosen copy */
    private double played;

    public static LockOnSound create(EntityHallucinationCreeper creeper, EntityPlayer player, boolean forMe) {
        double rate = BASE_SECONDS / secondsLeft(creeper, player); // the speed the clip would need to fit
        int pick = 0;
        for (int i = 1; i < SPEEDS.length; i++) {
            if (Math.abs(Math.log(rate / SPEEDS[i])) < Math.abs(Math.log(rate / SPEEDS[pick]))) {
                pick = i;
            }
        }
        return new LockOnSound(creeper, player, pick, rate / SPEEDS[pick], forMe);
    }

    private LockOnSound(EntityHallucinationCreeper creeper, EntityPlayer player, int pick, double pitch,
        boolean forMe) {
        super(new ResourceLocation("cursedcreepers", NAMES[pick]));
        this.creeper = creeper;
        this.player = player;
        this.length = BASE_SECONDS / SPEEDS[pick];
        field_147663_c = clampPitch(pitch); // the sound's pitch (playback speed)
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
            donePlaying = true; // it's gone: so is the sound
            return;
        }
        played += field_147663_c / 20.0;
        if (played >= length) {
            donePlaying = true;
            return;
        }
        moveTo();
        field_147663_c = clampPitch((length - played) / secondsLeft(creeper, player));
    }

    private void moveTo() {
        xPosF = (float) creeper.posX;
        yPosF = (float) creeper.posY;
        zPosF = (float) creeper.posZ;
    }

    /** until it reaches them: its path distance left over its chase speed (straight-line until the server says) */
    private static double secondsLeft(EntityHallucinationCreeper creeper, EntityPlayer player) {
        double blocks = creeper.getRemainingDistance();
        if (blocks < 0) {
            blocks = Math.max(0, creeper.getDistanceToEntity(player) - Config.hallucinationVanishDistance);
        }
        return Math.max(0.05, blocks / Math.max(1, creeper.getChaseSpeed()));
    }

    private static float clampPitch(double p) {
        return (float) Math.max(0.5, Math.min(2.0, p)); // what Minecraft allows anyway
    }
}
