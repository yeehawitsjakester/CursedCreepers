package net.yeehawitsjake.cursedcreepers.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * a spawn egg for one of this mod's mobs (vanilla eggs only take global entity ids, which modded mobs shouldn't use)
 */
public class ItemCursedSpawnEgg extends Item {

    private final Class<? extends EntityLiving> entity;
    private final int base, spots;

    @SideOnly(Side.CLIENT)
    private IIcon overlay;

    public ItemCursedSpawnEgg(Class<? extends EntityLiving> entity, int base, int spots) {
        this.entity = entity;
        this.base = base;
        this.spots = spots;
        setCreativeTab(CreativeTabs.tabMisc);
        setTextureName("spawn_egg");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        x += Facing.offsetsXForSide[side];
        y += Facing.offsetsYForSide[side];
        z += Facing.offsetsZForSide[side];
        try {
            EntityLiving mob = entity.getConstructor(World.class)
                .newInstance(world);
            mob.setLocationAndAngles(x + 0.5, y, z + 0.5, world.rand.nextFloat() * 360f, 0f);
            mob.onSpawnWithEgg(null);
            world.spawnEntityInWorld(mob);
            if (!player.capabilities.isCreativeMode) {
                stack.stackSize--;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass) {
        return pass == 0 ? base : spots;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
        return pass > 0 ? overlay : super.getIconFromDamageForRenderPass(damage, pass);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        super.registerIcons(register);
        overlay = register.registerIcon("spawn_egg_overlay");
    }
}
