package Looter;

import com.runemate.game.api.hybrid.player_sense.PlayerSense;
import com.runemate.game.api.hybrid.util.calculations.Random;

import java.util.function.Supplier;

public class CustomPlayerSense {
    public static void initializeKeys() {
        for (Key key : Key.values()) {
            if (PlayerSense.get(key.name) == null) {
                PlayerSense.put(key.name, key.supplier.get());
            }
        }
    }

    public enum Key {
        ACTIVENESS_FACTOR_WHILE_WAITING("prime_activeness_factor", () -> Random.nextDouble(0.2, 0.8)),
        SPAM_CLICK_COUNT("prime_spam_click_count", () -> Random.nextInt(2, 6)),
        REACION_TIME("prime_reaction_time", () -> Random.nextLong(160, 260)),
        SPAM_CLICK_TO_HEAL("prime_spam_healing", () -> Random.nextBoolean());

        private final String name;
        private final Supplier supplier;

        Key(String name, Supplier supplier) {
            this.name = name;
            this.supplier = supplier;
        }

        public String getKey() {
            return name;
        }

        public Integer getAsInteger() {
            return PlayerSense.getAsInteger(name);
        }

        public Double getAsDouble() {
            return PlayerSense.getAsDouble(name);
        }

        public Long getAsLong() {
            return PlayerSense.getAsLong(name);
        }

        public Boolean getAsBoolean() {
            return PlayerSense.getAsBoolean(name);
        }
    }
}