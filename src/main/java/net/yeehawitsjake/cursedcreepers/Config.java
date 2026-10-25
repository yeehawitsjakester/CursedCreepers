package net.yeehawitsjake.cursedcreepers;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

/** config/cursedcreepers.cfg. Set it the same on the server and every client. */
public final class Config {

    private Config() {}

    public static double hallucinationChaseSpeed = 24;
    public static double hallucinationSightRange = 64;
    public static double hallucinationVanishDistance = 1.5;
    public static int hallucinationSpawnWeight = 0;
    public static double hallucinationReplaceChance = 1;
    public static double hallucinationSoundRange = 60;
    public static double hallucinationExplodeChance = 20;
    public static double hallucinationExplodeDamage = 3;
    public static double crashChance = 0.000001;
    public static double hauntMinutes = 10;
    public static double watcherShare = 40;
    public static String[] whisperSounds = { "cursedcreepers:whisper,20,32", "cursedcreepers:bold,24,40",
        "creeper.primed,2,4", "mob.zombie.wood,10,20", "random.door_open,8,16", "random.bow,12,24",
        "mob.endermen.stare,16,24", "cursedcreepers:cave_fart,6,14,0.4,cave" };
    public static int watcherMinDistance = 20;
    public static int watcherMaxDistance = 40;
    public static double watcherVanishAngle = 12;
    public static double watcherVanishDistance = 12;
    public static int watcherSeconds = 90;
    public static boolean watcherNeedsTrees = true;

    public static void load(File file) {
        Configuration c = new Configuration(file);
        String cat = "hallucination_creeper";
        hallucinationChaseSpeed = c
            .get(
                cat,
                "chaseSpeed",
                hallucinationChaseSpeed,
                "Blocks per second it runs at you, exactly (a sprinting player does about 5.6, a creeper 2.8).",
                1,
                100)
            .getDouble();
        hallucinationSightRange = c
            .get(
                cat,
                "sightRange",
                hallucinationSightRange,
                "How far away it notices players, in blocks (creepers: 16).",
                8,
                128)
            .getDouble();
        hallucinationVanishDistance = c
            .get(
                cat,
                "vanishDistance",
                hallucinationVanishDistance,
                "How close it gets to its target before it disappears, in blocks.",
                0.5,
                8.0)
            .getDouble();
        hallucinationSpawnWeight = c.get(
            cat,
            "spawnWeight",
            hallucinationSpawnWeight,
            "Extra natural spawn weight among monsters (creepers and zombies are 100), on top of replaceChance. 0 = off.",
            0,
            100)
            .getInt();
        hallucinationReplaceChance = c
            .get(
                cat,
                "replaceChance",
                hallucinationReplaceChance,
                "Percent of naturally spawned creepers that are Hallucination Creepers instead.",
                0,
                100)
            .getDouble();
        hallucinationSoundRange = c.get(
            cat,
            "soundRange",
            hallucinationSoundRange,
            "Blocks from the creeper that other players hear it coming, fading with distance. Its target always hears it.",
            0,
            256)
            .getDouble();
        hallucinationExplodeChance = c
            .get(
                cat,
                "explodeChance",
                hallucinationExplodeChance,
                "Percent that go off when they reach their target: a bang and a little damage, never blocks.",
                0,
                100)
            .getDouble();
        hallucinationExplodeDamage = c
            .get(
                cat,
                "explodeDamage",
                hallucinationExplodeDamage,
                "Damage those do, in half-hearts (before difficulty scaling).",
                0,
                40)
            .getDouble();
        crashChance = c.get(
            "general",
            "crashChance",
            crashChance,
            "Percent chance, each time a Hallucination Creeper reaches someone, that it crashes the server. 0 = never.",
            0,
            100)
            .getDouble();
        cat = "haunting";
        c.setCategoryComment(cat, "Things only one player notices. Server side only.");
        hauntMinutes = c.get(
            cat,
            "minutesBetween",
            hauntMinutes,
            "On average, minutes between hauntings for each player (a whisper or Herobrine). 0 = never on their own.",
            0,
            1440)
            .getDouble();
        watcherShare = c.get(
            cat,
            "watcherChance",
            watcherShare,
            "Percent of hauntings that are Herobrine watching (when there are trees to put him in); the rest are whispers.",
            0,
            100)
            .getDouble();
        whisperSounds = c.get(
            cat,
            "whisperSounds",
            whisperSounds,
            "Sounds a whisper picks from, as name,minDistance,maxDistance[,volume][,cave]: played from that far behind "
                + "the player, to them alone. volume scales it (1 = just carries that far); cave = only when no daylight "
                + "reaches them. Any sound name works, including ones a resource pack adds.")
            .getStringList();
        watcherMinDistance = c
            .get(cat, "watcherMinDistance", watcherMinDistance, "Closest Herobrine appears, in blocks.", 4, 96)
            .getInt();
        watcherMaxDistance = c
            .get(cat, "watcherMaxDistance", watcherMaxDistance, "Furthest Herobrine appears, in blocks.", 4, 96)
            .getInt();
        watcherVanishAngle = c
            .get(
                cat,
                "watcherVanishAngle",
                watcherVanishAngle,
                "Herobrine vanishes when the player's crosshair comes within this many degrees of him.",
                1,
                45)
            .getDouble();
        watcherVanishDistance = c
            .get(
                cat,
                "watcherVanishDistance",
                watcherVanishDistance,
                "...or when they come this close, in blocks.",
                0,
                64)
            .getDouble();
        watcherSeconds = c
            .get(cat, "watcherSeconds", watcherSeconds, "How long Herobrine stays if he's never looked at.", 5, 600)
            .getInt();
        watcherNeedsTrees = c
            .get(
                cat,
                "watcherNeedsTrees",
                watcherNeedsTrees,
                "Herobrine only appears beside a tree trunk or under leaves. Off: anywhere in plain view.")
            .getBoolean();
        if (c.hasChanged()) {
            c.save();
        }
    }
}
