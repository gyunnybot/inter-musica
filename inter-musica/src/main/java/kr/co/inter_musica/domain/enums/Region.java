package kr.co.inter_musica.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public static List<String> normalizeList(List<String> rawRegions) {
        if (rawRegions == null) throw new IllegalArgumentException("지역 선택은 필수입니다.");

        List<String> normalized = rawRegions.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .map(Region::from)
                .map(Region::name)
                .distinct()
                .collect(Collectors.toList());

        if (normalized.isEmpty()) throw new IllegalArgumentException("지역 선택은 필수입니다.");

        return normalized;
    }

    public static List<String> parseStored(String raw) {
        if (raw == null || raw.isBlank()) return List.of();

        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .map(Region::from)
                .map(Region::name)
                .distinct()
                .collect(Collectors.toList());
    }
}
