package net.yeehawitsjake.cursedcreepers;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.yeehawitsjake.cursedcreepers.entity.EntityHallucinationCreeper;

/**
 * /cursedcreeper spawn [distance] [player]: sends a Hallucination Creeper at a player from a distance (50 blocks by
 * default), already after them, from somewhere it can actually get to them. Ops only; only the sender is told.
 */
public class CommandCursedCreeper extends CommandBase {

    @Override
    public String getCommandName() {
        return "cursedcreeper";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/cursedcreeper spawn [distance] [player]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1 || !args[0].equalsIgnoreCase("spawn")) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        int distance = args.length > 1 ? parseIntBounded(sender, args[1], 1, 128) : 50;
        EntityPlayerMP target = args.length > 2 ? getPlayer(sender, args[2]) : getCommandSenderAsPlayer(sender);

        World world = target.worldObj;
        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = world.rand.nextDouble() * Math.PI * 2;
            int x = MathHelper.floor_double(target.posX + Math.cos(angle) * distance);
            int z = MathHelper.floor_double(target.posZ + Math.sin(angle) * distance);
            int y = groundNear(world, x, MathHelper.floor_double(target.posY), z);
            if (y < 0) {
                continue;
            }
            EntityHallucinationCreeper creeper = new EntityHallucinationCreeper(world);
            creeper.setLocationAndAngles(x + 0.5, y, z + 0.5, world.rand.nextFloat() * 360f, 0f);
            if (creeper.pathTo(target) == null) {
                continue; // somewhere it couldn't get to them from: try another direction
            }
            creeper.setAttackTarget(target);
            world.spawnEntityInWorld(creeper);
            sender.addChatMessage(
                new ChatComponentText(
                    "A Hallucination Creeper is on its way to " + target
                        .getCommandSenderName() + " from " + distance + " blocks (" + x + ", " + y + ", " + z + ")"));
            return;
        }
        throw new CommandException(
            "Couldn't find ground " + distance
                + " blocks from "
                + target.getCommandSenderName()
                + " that it could reach them from");
    }

    /** the standable y closest to the player's height in this column (within 24 blocks), or -1; never loads chunks */
    private static int groundNear(World world, int x, int y0, int z) {
        if (!world.blockExists(x, y0, z)) {
            return -1;
        }
        for (int dy = 0; dy <= 24; dy++) {
            for (int y : new int[] { y0 + dy, y0 - dy }) {
                if (y > 1 && y < 254
                    && world.getBlock(x, y - 1, z)
                        .getMaterial()
                        .isSolid()
                    && world.isAirBlock(x, y, z)
                    && world.isAirBlock(x, y + 1, z)) {
                    return y;
                }
            }
        }
        return -1;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "spawn");
        }
        if (args.length == 3) {
            return getListOfStringsMatchingLastWord(
                args,
                MinecraftServer.getServer()
                    .getAllUsernames());
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 2;
    }
}
