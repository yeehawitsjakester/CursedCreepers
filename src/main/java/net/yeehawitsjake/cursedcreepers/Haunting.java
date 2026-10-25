package net.yeehawitsjake.cursedcreepers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.yeehawitsjake.cursedcreepers.entity.EntityWatcher;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Things only one player notices. Every so often (on average every few minutes, per player) one of them is haunted:
 * either a sound from somewhere behind them that nobody else hears, or Herobrine at the edge of the trees. Both can be
 * set off by ops with /cursedcreeper whisper and /cursedcreeper watcher.
 */
public class Haunting {

    private final Random rand = new Random();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        MinecraftServer server = MinecraftServer.getServer();
        if (event.phase != TickEvent.Phase.END || Config.hauntMinutes <= 0 || server.getTickCounter() % 20 != 0) {
            return;
        }
        double chance = 1.0 / (Config.hauntMinutes * 60); // per player per second
        @SuppressWarnings("unchecked")
        List<EntityPlayerMP> players = server.getConfigurationManager().playerEntityList;
        for (EntityPlayerMP player : players) {
            if (player.isEntityAlive() && rand.nextDouble() < chance) {
                haunt(player);
            }
        }
    }

    private void haunt(EntityPlayerMP player) {
        if (rand.nextDouble() * 100 < Config.watcherShare && watcher(player)) {
            return;
        }
        whisper(player, null); // not near any trees, or it wasn't a watcher this time
    }

    // ---------------------------------------------------------------- whispers

    /**
     * plays a sound to this player alone, from somewhere behind them; a random one of the configured sounds that suits
     * where they are when name is null. Returns what it played, or null if there's nothing to play.
     */
    public static String whisper(EntityPlayerMP player, String name) {
        Random r = player.getRNG();
        String[] entry = null;
        if (name == null) {
            boolean underground = underground(player);
            List<String[]> fits = new ArrayList<String[]>();
            for (String s : Config.whisperSounds) {
                String[] e = s.split(",");
                if (!(e.length > 4 && e[4].trim()
                    .equalsIgnoreCase("cave")) || underground) {
                    fits.add(e);
                }
            }
            if (fits.isEmpty()) {
                return null;
            }
            entry = fits.get(r.nextInt(fits.size()));
        } else {
            for (String s : Config.whisperSounds) {
                if (s.split(",")[0].trim()
                    .equals(name)) {
                    entry = s.split(",");
                }
            }
            if (entry == null) {
                entry = new String[] { name }; // any sound at all, at the default distance
            }
        }
        String sound = entry[0].trim();
        double min = entry.length > 1 ? parse(entry[1], 16) : 16;
        double max = entry.length > 2 ? parse(entry[2], min) : min + 8;
        double loudness = entry.length > 3 ? parse(entry[3], 1) : 1;
        double dist = min + r.nextDouble() * Math.max(0, max - min);
        // behind them, give or take 60 degrees
        double angle = Math.toRadians(player.rotationYaw + 180 + (r.nextDouble() - 0.5) * 120);
        double x = player.posX - Math.sin(angle) * dist;
        double z = player.posZ + Math.cos(angle) * dist;
        double y = player.posY + player.getEyeHeight() + (r.nextDouble() - 0.5) * 4;
        // Minecraft fades a sound out linearly over 16 blocks per unit of volume (and plays it quieter below 1): loud
        // enough to just carry this far, so it always sounds distant, never next to them, then scaled
        float volume = (float) (Math.max(1, dist / 0.65 / 16) * loudness);
        player.playerNetServerHandler
            .sendPacket(new S29PacketSoundEffect(sound, x, y, z, volume, 0.9F + r.nextFloat() * 0.2F));
        return sound;
    }

    /** no daylight reaches them at all: a cave, not a house */
    private static boolean underground(EntityPlayerMP player) {
        int x = MathHelper.floor_double(player.posX), z = MathHelper.floor_double(player.posZ);
        int y = MathHelper.floor_double(player.posY + player.getEyeHeight());
        return player.worldObj.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) == 0;
    }

    private static double parse(String s, double fallback) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // ---------------------------------------------------------------- watchers

    /** puts Herobrine among the trees at the edge of their sight, if there's a spot; whether it did */
    public static boolean watcher(EntityPlayerMP player) {
        World world = player.worldObj;
        Random r = player.getRNG();
        Vec3 eye = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        for (int attempt = 0; attempt < 48; attempt++) {
            // off to one side, where they'd only catch it out of the corner of their eye
            double side = (25 + r.nextDouble() * 20) * (r.nextBoolean() ? 1 : -1);
            double angle = Math.toRadians(player.rotationYaw + side);
            double dist = Config.watcherMinDistance
                + r.nextDouble() * Math.max(0, Config.watcherMaxDistance - Config.watcherMinDistance);
            int x = MathHelper.floor_double(player.posX - Math.sin(angle) * dist);
            int z = MathHelper.floor_double(player.posZ + Math.cos(angle) * dist);
            int y = groundNear(world, x, MathHelper.floor_double(player.posY), z, 12);
            if (y < 0 || Config.watcherNeedsTrees && !nearTrees(world, x, y, z)) {
                continue;
            }
            EntityWatcher watcher = new EntityWatcher(world);
            watcher.setLocationAndAngles(x + 0.5, y, z + 0.5, 0, 0);
            Vec3 head = Vec3.createVectorHelper(x + 0.5, y + watcher.getEyeHeight(), z + 0.5);
            // it has to be in plain view (grass and flowers don't count as cover), but not right where they're looking
            if (world.func_147447_a(eye, head, false, true, false) != null || watcher.isLookedAt(player)) {
                continue;
            }
            watcher.watch(player, Config.watcherSeconds * 20);
            world.spawnEntityInWorld(watcher);
            return true;
        }
        return false;
    }

    /** a tree trunk right beside it, or leaves overhead: somewhere to lurk */
    private static boolean nearTrees(World world, int x, int y, int z) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 4; dy++) {
                    Block b = world.getBlock(x + dx, y + dy, z + dz);
                    if (dy <= 2 && Math.abs(dx) <= 1 && Math.abs(dz) <= 1 && b.isWood(world, x + dx, y + dy, z + dz)
                        || dy >= 3 && dx == 0 && dz == 0 && b.isLeaves(world, x, y + dy, z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * the standable y closest to y0 in this column (within range blocks), or -1; never loads chunks. Tall grass and
     * flowers are fine to stand in; the tops of trees aren't ground
     */
    public static int groundNear(World world, int x, int y0, int z, int range) {
        if (!world.blockExists(x, y0, z)) {
            return -1;
        }
        for (int dy = 0; dy <= range; dy++) {
            for (int y : new int[] { y0 + dy, y0 - dy }) {
                if (y > 1 && y < 254
                    && standable(world.getBlock(x, y - 1, z))
                    && open(world.getBlock(x, y, z))
                    && open(world.getBlock(x, y + 1, z))) {
                    return y;
                }
            }
        }
        return -1;
    }

    private static boolean standable(Block b) {
        return b.getMaterial()
            .isSolid() && b.getMaterial() != Material.leaves;
    }

    private static boolean open(Block b) {
        return !b.getMaterial()
            .blocksMovement()
            && !b.getMaterial()
                .isLiquid();
    }
}
