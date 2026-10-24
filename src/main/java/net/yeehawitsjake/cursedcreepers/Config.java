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
        if (c.hasChanged()) {
            c.save();
        }
    }
}
