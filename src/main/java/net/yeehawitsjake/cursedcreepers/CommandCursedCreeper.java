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
 * Ops only; only the sender is told.
 * <ul>
 * <li>/cursedcreeper spawn [distance] [player]: sends a Hallucination Creeper at a player from a distance (50 blocks by
 * default), already after them, from somewhere it can actually get to them.</li>
 * <li>/cursedcreeper watcher [player]: Herobrine among the trees at the edge of their sight.</li>
 * <li>/cursedcreeper whisper [player] [sound]: a sound behind them only they hear.</li>
 * </ul>
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
        return "/cursedcreeper spawn [distance] [player] | watcher [player] | whisper [player] [sound]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        String sub = args.length < 1 ? "" : args[0].toLowerCase();
        if (sub.equals("watcher")) {
            EntityPlayerMP target = args.length > 1 ? getPlayer(sender, args[1]) : getCommandSenderAsPlayer(sender);
            if (!Haunting.watcher(target)) {
                throw new CommandException(
                    "Nowhere in view near trees, off to the side, "
                        + (int) Math.min(Config.watcherMinDistance, Config.watcherVanishDistance + 4)
                        + "-"
                        + Config.watcherMaxDistance
                        + " blocks from "
                        + target.getCommandSenderName());
            }
            sender.addChatMessage(
                new ChatComponentText("Something is watching " + target.getCommandSenderName() + " from the trees"));
            return;
        }
        if (sub.equals("whisper")) {
            EntityPlayerMP target = args.length > 1 ? getPlayer(sender, args[1]) : getCommandSenderAsPlayer(sender);
            String sound = Haunting.whisper(target, args.length > 2 ? args[2] : null);
            if (sound == null) {
                throw new CommandException("No whisper sounds are configured");
            }
            sender.addChatMessage(new ChatComponentText(target.getCommandSenderName() + " heard " + sound));
            return;
        }
        if (!sub.equals("spawn")) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        int distance = args.length > 1 ? parseIntBounded(sender, args[1], 1, 128) : 50;
        EntityPlayerMP target = args.length > 2 ? getPlayer(sender, args[2]) : getCommandSenderAsPlayer(sender);

        World world = target.worldObj;
        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = world.rand.nextDouble() * Math.PI * 2;
            int x = MathHelper.floor_double(target.posX + Math.cos(angle) * distance);
            int z = MathHelper.floor_double(target.posZ + Math.sin(angle) * distance);
            int y = Haunting.groundNear(world, x, MathHelper.floor_double(target.posY), z, 24);
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

    @Override
    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "spawn", "watcher", "whisper");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("whisper")) {
            String[] names = new String[Config.whisperSounds.length];
            for (int i = 0; i < names.length; i++) {
                names[i] = Config.whisperSounds[i].split(",")[0].trim();
            }
            return getListOfStringsMatchingLastWord(args, names);
        }
        if (isUsernameIndex(args, args.length - 1)) {
            return getListOfStringsMatchingLastWord(
                args,
                MinecraftServer.getServer()
                    .getAllUsernames());
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == (args.length > 0 && args[0].equalsIgnoreCase("spawn") ? 2 : 1);
    }
}
