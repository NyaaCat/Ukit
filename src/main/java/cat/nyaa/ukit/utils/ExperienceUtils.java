package cat.nyaa.ukit.utils;

import com.google.common.primitives.Ints;
import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;


public final class ExperienceUtils {
    // From NyaaCore
    // https://github.com/NyaaCat/NyaaCore/blob/0bc366debf51b0f4dcd867b657be19e14e772100/src/main/java/cat/nyaa/nyaacore/utils/ExperienceUtils.java

    // refer to https://minecraft.wiki/w/Experience
    private static final List<Integer> usableSplashExpList = List.of(1, 2, 6, 16, 36, 72, 148, 306, 616, 1236, 2476, 32767).reversed();
    private static final Random random = new Random();

    /**
     * How much exp points at least needed to reach this level.
     * i.e. getLevel() = level &amp;&amp; getExp() == 0
     */
    public static int getExpForLevel(int level) {
        if (level < 0) throw new IllegalArgumentException();
        else if (level <= 16) return (level + 6) * level;
        else if (level < 32)
            return Ints.checkedCast(Math.round(2.5 * level * level - 40.5 * level + 360));
        else
            return Ints.checkedCast(Math.round(4.5 * level * level - 162.5 * level + 2220));
    }

    /**
     * The true exp point for a player at this time.
     */
    public static int getExpPoints(Player p) {
        int pointForCurrentLevel = Math.round(p.getExpToLevel() * p.getExp());
        return getExpForLevel(p.getLevel()) + pointForCurrentLevel;
    }

    public static void subtractExpPoints(Player p, int points) {
        if (points < 0) throw new IllegalArgumentException();
        if (points == 0) return;
        int total = getExpPoints(p);
        if (total < points)
            throw new IllegalArgumentException("Negative Exp Left");
        int newLevel = getLevelForExp(total - points);
        int remPoint = total - points - getExpForLevel(newLevel);
        p.setLevel(newLevel);
        p.setExp(0);
        p.giveExp(remPoint);
    }

    /**
     * Which level the player at if he/she has this mount of exp points
     * refer <a href="https://minecraft.wiki/w/Experience">minecraft.wiki/w/Experience</a>
     */
    public static int getLevelForExp(Integer total) {
        // 0-352
        // 353-1507
        // 1508+
        return (int) switch (total) {
            case Integer i when i < 353 -> Math.sqrt(total + 9.0) - 3.0;
            case Integer i when i < 1508 ->
                    81.0 / 10.0 + Math.sqrt(2.0 / 5.0 * (total - 7839.0 / 40.0));
            default ->
                    conditionalRounding(325.0 / 18.0 + Math.sqrt(2.0 / 9.0 * (total - 54215.0 / 72.0)));
        };
    }

    public static double conditionalRounding(double value) {
        double threshold = 1e-8;
        long nearestInt = Math.round(value);
        double diff = Math.abs(value - nearestInt);
        return diff < threshold ? nearestInt : value;
    }

    public static void splashExp(int amount, Location location) {
        splashExp(amount, location, null);
    }

    public static void splashExp(int amount, Location location, Consumer<ExperienceOrb> orbCustomizer) {
        var nextOrbValueIndex = 0;
        while (amount > 0) {
            nextOrbValueIndex = firstMatchedExpIndex(amount, nextOrbValueIndex);
            var nextOrbValue = usableSplashExpList.get(nextOrbValueIndex);
            location.getWorld().spawn(location, ExperienceOrb.class, orb -> {
                // Configure orb value before it is added to world so merge-on-spawn
                // sees the final value instead of default 0.
                orb.setExperience(nextOrbValue);
                orb.setVelocity(randomVector().multiply(0.3));
                if (orbCustomizer != null) {
                    orbCustomizer.accept(orb);
                }
            }, CreatureSpawnEvent.SpawnReason.CUSTOM);
            amount -= nextOrbValue;
        }
    }

    private static Vector randomVector() {
        return new Vector(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1);
    }

    private static int firstMatchedExpIndex(int remaining, int startingIndex) {
        for (int i = startingIndex; i < usableSplashExpList.size(); i++) {
            if (usableSplashExpList.get(i) <= remaining) return i;
        }
        throw new IllegalStateException("shouldn't be here");
    }

    /**
     * Change the player's experience (not experience level)
     * Related events may be triggered.
     *
     * @param p   the target player
     * @param exp amount of xp to be added to the player,
     *            if negative, then subtract from the player.
     * @param applyMending Mend players items with mending, with same behavior as picking up orbs. calls Player.giveExp(int,boolean)
     * @throws IllegalArgumentException if the player ended with negative xp
     */
    public static void addPlayerExperience(Player p, int exp, boolean applyMending) {
        if (exp > 0) {
            p.giveExp(exp, applyMending);
        } else if (exp < 0) {
            subtractExpPoints(p, -exp);
        }
    }

    public static void addPlayerExperience(Player p, int exp) {
        addPlayerExperience(p, exp, false);
    }
}
