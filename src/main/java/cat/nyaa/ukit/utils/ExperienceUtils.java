package cat.nyaa.ukit.utils;

import com.google.common.base.Strings;
import org.bukkit.entity.Player;

public final class ExperienceUtils {
    // From NyaaCore
    // https://github.com/NyaaCat/NyaaCore/blob/0bc366debf51b0f4dcd867b657be19e14e772100/src/main/java/cat/nyaa/nyaacore/utils/ExperienceUtils.java

    /**
     * How much exp points at least needed to reach this level.
     * i.e. getLevel() = level &amp;&amp; getExp() == 0
     * The formula is from the way how exp level grows in vanilla, and the formula is:
     * \[
     * f(x) =
     * \left \{
     *     \begin{array}{l}
     *     x ^ 2 + 6 x, 0 \leq x \leq 16 \\
     *     \frac{5}{2} x ^ 2 - \frac{81}{2} x + 360, 16 < x \leq 31 \\
     *     \frac{9}{2} x ^ 2 - \frac{325}{2} x + 2220, 31 < x \leq 21863
     *     \end{array}
     * \right .
     * \]
     */
    public static int getExpForLevel(int level) {
        if (level < 0) {
            throw new IllegalArgumentException();
        }
        if (level <= 16) {
            return (level + 6) * level;
        }
        if (level <= 31) {
            return (((5 * level - 81) * level) / 2) + 360;
        }
        if(level <= 21863) {
            return ((9 * level - 325) * level + 4440) >>> 1;
        }
        throw new IllegalArgumentException(Strings.lenientFormat("Out of range: The exp points of the level %s is out of the range of Integer.", level));
    }

    /**
     * The true exp point for a player at this time.
     */
    public static int getExpPoints(Player p) {
        int pointForCurrentLevel = Math.round(p.getExpToLevel() * p.getExp());
        return getExpForLevel(p.getLevel()) + pointForCurrentLevel;
    }

    public static void subtractExpPoints(Player p, int points) {
        addPlayerExperience(p, -points);
    }

    /**
     * Which level the player at if he/she has this mount of exp points
     * The formula of this method is the exact inverse function of the function getLeastExpForLevel.
     * \[
     * f(y) =
     * \left \{
     *     \begin{array}{l}
     *     \sqrt{9 + y} - 3 , 0 \leq y \leq 352 \\
     *     \frac{\sqrt{40 y - 7839} }{10} + \frac{81}{10} , 352 < y \leq 1507 \\
     *     \frac{\sqrt{72 y - 54215} }{18} + \frac{325}{18} , 1507 < y \leq 2 ^ {31}
     *     \end{array}
     * \right .
     * \]
     */
    public static int getLevelForExp(int exp) {
        if (exp < 0) throw new IllegalArgumentException();
        if(exp <= 352) {
            return (int) Math.sqrt(9 + exp) - 3;
        }

        if(exp <= 1507) {
            return (int) (Math.sqrt(40 * exp - 7839) / 10 + 81d / 10d);
        } else {
            return (int) (Math.sqrt(72L * exp - 54215) / 18 + 325d / 18d);
        }
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
    public static void addPlayerExperience(Player p, int points) {
        int playerPreviousExoPoints = getExpPoints(p);
        if (playerPreviousExoPoints < -points) throw new IllegalArgumentException("Negative Exp Left");
        int newLevel = getLevelForExp(playerPreviousExoPoints + points);
        int remPoint = playerPreviousExoPoints + points - getExpForLevel(newLevel);
        p.setLevel(newLevel);
        p.setExp(0);
        p.giveExp(remPoint);
    }
}