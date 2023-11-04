package cat.nyaa.ukit.utils;


import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.function.Supplier;

public class CompatibleScheduler {
    private static final boolean isSpigot = ((Supplier<Boolean>) () -> {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }).get();

    private static final boolean isFolia = ((Supplier<Boolean>) () -> {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }).get();

    public static void runTask(Plugin pluginInstance, Runnable runnable) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().run(pluginInstance, t -> runnable.run());
        } else if (isSpigot) {
            Bukkit.getScheduler().runTask(pluginInstance, runnable);
        } else {
            throw new RuntimeException("Unsupported server platform!");
        }
    }

    public static void runTaskLater(Plugin pluginInstance, Runnable runnable, long delay) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(pluginInstance, t -> runnable.run(), delay <= 0 ? 1 : delay);
        } else if (isSpigot) {
            Bukkit.getScheduler().runTaskLater(pluginInstance, runnable, delay);
        } else {
            throw new RuntimeException("Unsupported server platform!");
        }
    }

    public static CompatibleTask runTaskTimer(Plugin pluginInstance, Runnable runnable, long delay, long taskTimer) {
        if (isFolia) {
            return new CompatibleTask(Bukkit.getGlobalRegionScheduler().runAtFixedRate(pluginInstance, t -> runnable.run(), delay <= 0 ? 1 : delay, taskTimer));
        } else if (isSpigot) {
            return new CompatibleTask(Bukkit.getScheduler().runTaskTimer(pluginInstance, runnable, delay, taskTimer));
        } else {
            throw new RuntimeException("Unsupported server platform!");
        }
    }

    public static void cancelTasks(Plugin pluginInstance) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().cancelTasks(pluginInstance);
        } else if (isSpigot) {
            Bukkit.getScheduler().cancelTasks(pluginInstance);
        } else {
            throw new RuntimeException("Unsupported server platform!");
        }
    }

}

class CompatibleTask {
    private final Object task;

    public CompatibleTask(Object task) {
        if (!(task instanceof ScheduledTask) && !(task instanceof org.bukkit.scheduler.BukkitTask))
            throw new RuntimeException("Unsupported task type!");
        this.task = task;
    }

    public void cancel() {
        if (task instanceof ScheduledTask) {
            ((ScheduledTask) task).cancel();
        } else if (task instanceof org.bukkit.scheduler.BukkitTask) {
            ((org.bukkit.scheduler.BukkitTask) task).cancel();
        }
    }

}
