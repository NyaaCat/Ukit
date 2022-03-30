package cat.nyaa.ukit.utils;

import com.google.common.primitives.Ints;
import org.bukkit.entity.Player;

public final class ExperienceUtils {
    // From NyaaCore
    // https://github.com/NyaaCat/NyaaCore/blob/0bc366debf51b0f4dcd867b657be19e14e772100/src/main/java/cat/nyaa/nyaacore/utils/ExperienceUtils.java

    /**
     * How much exp points at least needed to reach this level.
     * i.e. getLevel() = level &amp;&amp; getExp() == 0
     */
    public static int getExpForLevel(int level) {
        if (level < 0) throw new IllegalArgumentException();
        else if (level <= 16) return (level + 6) * level;
        else if (level < 32) return Ints.checkedCast(Math.round(2.5 * level * level - 40.5 * level + 360));
        else return Ints.checkedCast(Math.round(4.5 * level * level - 162.5 * level + 2220));
    }

    /**
     * The true exp point for a player at this time.
     */
    public static int getExpPoints(Player p) {
        int pointForCurrentLevel = Math.round(p.getExpToLevel() * p.getExp());
        return getExpForLevel(p.getLevel()) + pointForCurrentLevel;
    }

    public static void subtractPlayerExpPoints(Player p, int points) {
        addPlayerExpPoints(p, -points);
    }

    /**
     * Which level the player at if he/she has this mount of exp points
     * TODO optimization
     */
    public static int getLevelForExp(int exp) {
        if (exp < 0) throw new IllegalArgumentException();
        for (int lv = 1; lv < 21000; lv++) {
            if (getExpForLevel(lv) > exp) return lv - 1;
        }
        throw new IllegalArgumentException("exp too large");
    }

    /**
     * Change the player's experience (not experience level)
     * Related events may be triggered.
     *
     * @param p   the target player
     * @param points amount of xp to be added to the player,
     *            if negative, then subtract from the player.
     * @throws IllegalArgumentException if the player ended with negative xp
     */
    public static void addPlayerExpPoints(Player p, int points) {
        int playerPreviousExoPoints = getExpPoints(p);
        if (playerPreviousExoPoints < -points) throw new IllegalArgumentException("Negative Exp Left");
        int newLevel = getLevelForExp(playerPreviousExoPoints + points);
        int remPoint = playerPreviousExoPoints + points - getExpForLevel(newLevel);
        p.setLevel(newLevel);
        p.setExp(0);
        p.giveExp(remPoint);
    }
}