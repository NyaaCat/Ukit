package cat.nyaa.ukit;

import cat.nyaa.ukit.utils.ExperienceUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpUtilTest {
    @Test
    public void testExpCalculationMatch() {
        var levelsForTest = List.of(1, 2, 3, 5, 7, 9, 15, 16, 25, 30, 31, 40, 50, 70, 90, 120, 150, 200, 300, 500, 700, 1000, 2000, 4000, 8000, 1000, 12000, 15000, 18000, 21000);
        // will break on lvl 21863 + 75705 offset with the total reach Integer.MAX_VALUE
        for (int lvl : levelsForTest) {
            var total = ExperienceUtils.getExpForLevel(lvl);
            for (int offset = 0; offset < maxExpOffset(lvl); offset++) {
                var result = ExperienceUtils.getLevelForExp(total + offset);
                assertEquals(lvl, result);
            }
        }
    }

    private int maxExpOffset(Integer level) {
        return switch (level) {
            case Integer i when i < 16 -> 2 * level + 7;
            case Integer i when i < 31 -> 5 * level - 38;
            default -> 9 * level - 158;
        };
    }
}
