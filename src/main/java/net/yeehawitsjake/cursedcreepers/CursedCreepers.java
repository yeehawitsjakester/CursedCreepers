package net.yeehawitsjake.cursedcreepers;

import java.util.ArrayList;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;
import net.yeehawitsjake.cursedcreepers.entity.EntityHallucinationCreeper;
import net.yeehawitsjake.cursedcreepers.item.ItemCursedSpawnEgg;
import net.yeehawitsjake.cursedcreepers.proxy.CommonProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(
    modid = CursedCreepers.MODID,
    name = "Cursed Creepers",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]")
public class CursedCreepers {

    public static final String MODID = "cursedcreepers";

    @Mod.Instance(MODID)
    public static CursedCreepers instance;

    @SidedProxy(
        clientSide = "net.yeehawitsjake.cursedcreepers.proxy.ClientProxy",
        serverSide = "net.yeehawitsjake.cursedcreepers.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Config.load(event.getSuggestedConfigurationFile());

        // mod entity ids are per mod, so these never clash with the pack's other mobs
        EntityRegistry
            .registerModEntity(EntityHallucinationCreeper.class, "HallucinationCreeper", 0, this, 160, 2, true);
        GameRegistry.registerItem(
            new ItemCursedSpawnEgg(EntityHallucinationCreeper.class, 0x0DA70B, 0x101010)
                .setUnlocalizedName(MODID + ".spawnEggHallucinationCreeper"),
            "spawn_egg_hallucination_creeper");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.registerRenderers();
        MinecraftForge.EVENT_BUS.register(new SpawnHandler());
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandCursedCreeper());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // after every mod has added its biomes, so they get the creeper too
        if (Config.hallucinationSpawnWeight > 0) {
            ArrayList<BiomeGenBase> biomes = new ArrayList<BiomeGenBase>();
            for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray()) {
                if (b != null && b != BiomeGenBase.hell && b != BiomeGenBase.sky && b != BiomeGenBase.mushroomIsland) {
                    biomes.add(b);
                }
            }
            EntityRegistry.addSpawn(
                EntityHallucinationCreeper.class,
                Config.hallucinationSpawnWeight,
                1,
                1,
                EnumCreatureType.monster,
                biomes.toArray(new BiomeGenBase[biomes.size()]));
        }
    }
}
