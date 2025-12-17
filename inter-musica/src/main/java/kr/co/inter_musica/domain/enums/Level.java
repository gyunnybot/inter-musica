package kr.co.inter_musica.domain.enums;

import java.util.Locale;

public enum Level {
    HOBBY_UNDER_A_YEAR,
    HOBBY_UNDER_FIVE_YEAR,
    HOBBY_OVER_FIVE_YEAR,
    MAJOR_STUDENT;

    public static Level from(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("레벨 선택은 필수입니다.");
        }

        String v = raw.trim().toUpperCase(Locale.ROOT);

        return Level.valueOf(v);
    }

    // 자동으로 오름차순 기준으로 수준 비교
    public boolean isAtLeast(Level requiredMin) {
        return this.ordinal() >= requiredMin.ordinal();
    }
}
