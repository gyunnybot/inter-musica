package kr.co.inter_musica.domain.enums;

import java.util.Locale;

public enum Instrument {
    VIOLIN, PIANO, VIOLA, CELLO,
    VOCAL, GUITAR, BASS, DRUMS, KEYBOARD;

    public static Instrument from(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("악기 선택은 필수입니다.");
        }

        String v = raw.trim().toUpperCase(Locale.ROOT);
        // 정해진 값으로 강제? 프론트엔드에서 드랍 다운으로 만들건데, 의미가 있나?
        // 방법 1. service layer 에서 데이터 검증을 위해 from 정의 후 사용. db 에서는 varchar 로 정의했기 때문에 필터링 불가

        return Instrument.valueOf(v);
    }
}
