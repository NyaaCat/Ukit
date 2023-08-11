package cat.nyaa.ukit.utils;

import land.melon.lab.simplelanguageloader.utils.Pair;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public class Utils {
    public static int getReachDistance(Player player) {
        return player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE ? 5 : 6;
    }

    public static Block getBlockLookingAt(Player player) {
        return player.getTargetBlockExact(getReachDistance(player), FluidCollisionMode.NEVER);
    }

    public static Vector3D getCenterOfSign(Sign signState) {
        var block = signState.getBlock();
        var center = new Vector3D(block.getX() + 0.5, block.getY(), block.getZ() + 0.5);
        if (Tag.WALL_SIGNS.isTagged(signState.getType())) {
            if (block.getBlockData() instanceof Directional directional) {
                var face = directional.getFacing();
                if (face == BlockFace.NORTH) {
                    center = center.add(0, 0, 0.5);
                } else if (face == BlockFace.SOUTH) {
                    center = center.add(0, 0, -0.5);
                } else if (face == BlockFace.EAST) {
                    center = center.add(-0.5, 0, 0);
                } else /*face == BlockFace.WEST*/ {
                    center = center.add(0.5, 0, 0);
                }
            }
        }
        return center;
    }


    public static double angleFromSignFace(Vector3D playerLoc, Vector3D signLoc, BlockData data) {
        var initialAngle = signLoc.angle2dTo(playerLoc);
        var offsetAngle = 0.0;
        if (data instanceof Rotatable rotatable) {
            offsetAngle = getAngleFromBlockFace(rotatable.getRotation());
        }
        if (data instanceof Directional directional) {
            offsetAngle = getAngleFromBlockFace(directional.getFacing());
        }
        var angle = initialAngle - offsetAngle;

        if (angle > 360)
            angle -= 360;
        if (angle < 0)
            angle += 360;

        return angle;
    }

    public static double getAngleFromBlockFace(BlockFace blockFace) {
        Vector directionVector = blockFace.getDirection();

        double angleRadians = Math.atan2(directionVector.getZ(), directionVector.getX());
        double angleInDegrees = angleRadians * 180 / Math.PI;

        if (angleInDegrees >= 0)
            angleInDegrees += 90;
        if (angleInDegrees <= 0)
            angleInDegrees += 450;

        if (angleInDegrees > 360)
            angleInDegrees -= 360;

        return angleInDegrees;
    }

    public static SignSide getSignSideLookingAt(Player player, Sign sign) {
        var signLoc = getCenterOfSign(sign);
        var playerLoc = Vector3D.fromBukkitLocation(player.getLocation());
        var angle = angleFromSignFace(playerLoc, signLoc, sign.getBlockData());

        if (angle > 270 || angle <= 90)
            return sign.getSide(Side.FRONT);
        else
            return sign.getSide(Side.BACK);
    }

    public static Entity getEntityLookingAt(Player player, Predicate<Entity> predicate) {
        var trace = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getLocation().getDirection(), getReachDistance(player), 0, predicate);
        if (trace == null) return null;
        return trace.getHitEntity();
    }

    public static void silentBroadcast(String text) {
        Bukkit.getOnlinePlayers().forEach(
                p -> p.sendMessage(text)
        );
    }

    public static void silentBroadcast(BaseComponent baseComponent) {
        silentBroadcast(new BaseComponent[]{baseComponent});
    }

    public static void silentBroadcast(BaseComponent[] baseComponents) {
        Bukkit.getOnlinePlayers().forEach(
                p -> p.spigot().sendMessage(baseComponents)
        );
    }

    public static Pair<EquipmentSlot, ItemStack> getItemInHand(Player player) {
        var item = player.getInventory().getItemInMainHand();
        var isOffhand = false;
        if (item.getType().isAir()) {
            item = player.getInventory().getItemInOffHand();
            isOffhand = true;
        }
        if (item.getType().isAir())
            return null;
        else
            return Pair.of(isOffhand ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND, item);
    }

    public static Pair<EquipmentSlot, ItemStack> getItemInHand(Player player, Material type) {
        var item = player.getInventory().getItemInMainHand();
        var isOffhand = false;
        if (item.getType() != type) {
            item = player.getInventory().getItemInOffHand();
            isOffhand = true;
        }
        if (item.getType() != type)
            return null;
        else
            return Pair.of(isOffhand ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND, item);
    }

    public static void setItemInHand(Player player, Pair<EquipmentSlot, ItemStack> itemStackPair) {
        player.getInventory().setItem(itemStackPair.key(), itemStackPair.value());
    }
}
