package kr.co.inter_musica.domain.enums;

import java.util.Locale;

public enum Level {
    HOBBY_UNDER_A_YEAR,
    HOBBY_UNDER_FIVE_YEAR,
    HOBBY_OVER_FIVE_YEAR,
    MAJOR_STUDENT;

    public static Level from(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("level is null");
        }

        String v = raw.trim().toUpperCase(Locale.ROOT);
        return Level.valueOf(v);
    }

    public boolean isAtLeast(Level requiredMin) {
        return this.ordinal() >= requiredMin.ordinal();
    }
}
