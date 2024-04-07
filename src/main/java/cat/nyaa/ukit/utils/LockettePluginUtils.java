package cat.nyaa.ukit.utils;

import me.crafter.mc.lockettepro.LocketteProAPI;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LockettePluginUtils {
    private static final boolean hasLockettePro;


    static {
        hasLockettePro = Bukkit.getServer().getPluginManager().getPlugin("LockettePro") != null;
    }

    public static boolean isLockSign(Block block) {
        if (hasLockettePro) {
            return LocketteProAPI.isLockSignOrAdditionalSign(block) || LocketteProAPI.isLocked(block);
        } else {
            return false;
        }
    }

    public static boolean isLockedBlock(Block block) {
        if (hasLockettePro) {
            return LocketteProAPI.isLocked(block);
        } else {
            return false;
        }
    }

    public static boolean isBlockOwner(Player player, Block block){
        // need isLockedBlock
        if(hasLockettePro){
            return LocketteProAPI.isOwner( block,player);
        }else{
            return false;
        }
    }
}
