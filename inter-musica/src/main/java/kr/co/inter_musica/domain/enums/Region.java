package kr.co.inter_musica.domain.enums;

import java.util.Locale;

public enum Region {
    SEOUL_SEOCHO,
    SEOUL_HONGDAE,
    SEOUL_ITAEWON,
    SEOUL_SEONGSU,
    SEOUL_SEOUL_FOREST;

    public static Region from(String raw) {
        if (raw == null) throw new IllegalArgumentException("지역 선택은 필수입니다.");

        String v = raw.trim().toUpperCase(Locale.ROOT);

        return Region.valueOf(v);
    }
}
