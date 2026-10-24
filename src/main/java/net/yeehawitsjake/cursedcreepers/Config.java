package net.yeehawitsjake.cursedcreepers;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

/** config/cursedcreepers.cfg. Set it the same on the server and every client. */
public final class Config {

    private Config() {}

    public static double hallucinationSpeed = 0.6;
    public static double hallucinationSightRange = 64;
    public static double hallucinationVanishDistance = 1.5;
    public static int hallucinationSpawnWeight = 5;

    public static void load(File file) {
        Configuration c = new Configuration(file);
        String cat = "hallucination_creeper";
        hallucinationSpeed = c
            .get(
                cat,
                "speed",
                hallucinationSpeed,
                "Movement speed. A normal creeper is 0.25, a player walking about 0.1 (sprinting 0.13).",
                0.05,
                2.0)
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
        hallucinationSpawnWeight = c
            .get(
                cat,
                "spawnWeight",
                hallucinationSpawnWeight,
                "Natural spawn weight among monsters (creepers and zombies are 100). 0 = only spawn eggs.",
                0,
                100)
            .getInt();
        if (c.hasChanged()) {
            c.save();
        }
    }
}
