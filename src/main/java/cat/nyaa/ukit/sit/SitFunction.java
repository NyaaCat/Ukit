package cat.nyaa.ukit.sit;

import cat.nyaa.ukit.SpigotLoader;
import cat.nyaa.ukit.utils.SubCommandExecutor;
import cat.nyaa.ukit.utils.SubTabCompleter;
import cat.nyaa.ukit.utils.Vector3D;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.*;

public class SitFunction implements Listener, SubCommandExecutor, SubTabCompleter {
    private final SpigotLoader pluginInstance;
    private final NamespacedKey SIT_META_KEY;
    private final String SIT_PERMISSION_NODE = "ukit.sit";
    private final Set<UUID> enabledPlayers = new HashSet<>();

    public SitFunction(SpigotLoader pluginInstance) {
        this.pluginInstance = pluginInstance;
        SIT_META_KEY = new NamespacedKey(pluginInstance, "SIT_MARKER");
    }

    @EventHandler
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !isPlayerEnabled(event.getPlayer()) || event.hasItem() || !event.getPlayer().hasPermission(SIT_PERMISSION_NODE) || event.getPlayer().isInsideVehicle() || event.getPlayer().isSneaking()) {
            return;
        }
        if (event.getBlockFace() == BlockFace.DOWN || !pluginInstance.config.sitConfig.isEnabled(event.getClickedBlock().getType())) {
            return;
        }
        //set armor stand location and direction
        var baseLocation = event.getClickedBlock().getLocation();
        var relativeUpper1 = baseLocation.clone().add(0, 1, 0);
        if (!relativeUpper1.getBlock().getType().isAir())
            return;
        baseLocation.setY(event.getClickedBlock().getBoundingBox().getMaxY()); /* on the top of clicked block */
        baseLocation.add(0.5, -0.3, 0.5);

        var nearByEntities = baseLocation.getWorld().getNearbyEntities(baseLocation, 0.5, 0.7, 0.5);
        if (nearByEntities.stream().anyMatch(e -> e.getType() == EntityType.ARMOR_STAND && isSitMarker(e))) {
            event.getPlayer().sendMessage(pluginInstance.language.sitLang.invalidLocation.produce());
            return;
        }

        var preference = pluginInstance.config.sitConfig.getOffset(event.getClickedBlock().getType());
        baseLocation.add(preference.x, preference.y, preference.z);
        baseLocation.setYaw(event.getPlayer().getLocation().getYaw());
        //set yaw if block is directional
        if (event.getClickedBlock().getBlockData() instanceof Directional directional) {
            switch (directional.getFacing()) {
                case SOUTH -> baseLocation.setYaw(180);
                case WEST -> baseLocation.setYaw(-90);
                case NORTH -> baseLocation.setYaw(0);
                case EAST -> baseLocation.setYaw(90);
            }
        }
        //spawn entity
        var armorStandEntity = (ArmorStand) baseLocation.getWorld().spawnEntity(baseLocation, EntityType.ARMOR_STAND);
        armorStandEntity.setMarker(true);
        armorStandEntity.setInvisible(true);
        armorStandEntity.setInvulnerable(true);
        armorStandEntity.setGravity(false);
        armorStandEntity.setBasePlate(false);
        armorStandEntity.getPersistentDataContainer().set(SIT_META_KEY, PersistentDataType.LONG_ARRAY, Vector3D.fromBukkitLocation(event.getPlayer().getLocation()).toLangArray());
        armorStandEntity.addPassenger(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDismount(EntityDismountEvent event) {
        if (event.getEntityType() != EntityType.PLAYER || !isSitMarker(event.getDismounted())) {
            return;
        }
        var armorStand = event.getDismounted();
        var initialLocationInVector = Vector3D.fromLongArray(armorStand.getPersistentDataContainer().get(SIT_META_KEY, PersistentDataType.LONG_ARRAY));
        var teleportLocation = event.getEntity().getLocation();
        teleportLocation.setX(initialLocationInVector.x);
        teleportLocation.setY(initialLocationInVector.y);
        teleportLocation.setZ(initialLocationInVector.z);
        armorStand.remove();
        event.getEntity().teleport(teleportLocation);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().isInsideVehicle() && isSitMarker(event.getEntity().getVehicle())) {
            event.getEntity().getVehicle().remove();
        }
    }

    private boolean isSitMarker(Entity entity) {
        return entity == null || (entity.getType() == EntityType.ARMOR_STAND && entity.getPersistentDataContainer().has(SIT_META_KEY, PersistentDataType.LONG_ARRAY));
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(pluginInstance.language.commonLang.playerOnlyCommand.produce());
        } else if (!player.hasPermission(SIT_PERMISSION_NODE)) {
            player.sendMessage(pluginInstance.language.commonLang.permissionDenied.produce());
        } else if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "enable" -> enablePlayer(player);
                case "disable" -> disablePlayer(player);
                case "toggle" -> togglePlayer(player);
                default -> {
                    return false;
                }
            }
        } else {
            togglePlayer(player);
        }
        return true;
    }

    private void enablePlayer(Player player) {
        enabledPlayers.add(player.getUniqueId());
        player.sendMessage(pluginInstance.language.sitLang.sitEnabled.produce());
    }

    private void disablePlayer(Player player) {
        enabledPlayers.remove(player.getUniqueId());
        player.sendMessage(pluginInstance.language.sitLang.sitDisabled.produce());
    }

    private boolean isPlayerEnabled(Player player) {
        return enabledPlayers.contains(player.getUniqueId());
    }

    private void togglePlayer(Player player) {
        if (isPlayerEnabled(player)) {
            disablePlayer(player);
        } else {
            enablePlayer(player);
        }
    }

    @Override
    public String getHelp() {
        return pluginInstance.language.sitLang.help.produce();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        var subCommandList = Arrays.asList("enable", "disable", "toggle");
        var completeList = new ArrayList<String>();
        if (args.length == 0) {
            return subCommandList;
        } else if (args.length == 1) {
            for (var subCommand : subCommandList) {
                if (subCommand.startsWith(args[0]) && !subCommand.equalsIgnoreCase(args[0])) {
                    completeList.add(subCommand);
                }
            }
        }
        return completeList;
    }
}
