package kr.co.inter_musica.domain.enums;

import java.util.Locale;

public enum Instrument {
    VIOLIN, PIANO, VIOLA, CELLO,
    VOCAL, GUITAR, BASS, DRUMS, KEYBOARD;

    public static Instrument from(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("instrument is null");
        }

        String v = raw.trim().toUpperCase(Locale.ROOT); // 입력 타입 강제

        return Instrument.valueOf(v);
    }
}
