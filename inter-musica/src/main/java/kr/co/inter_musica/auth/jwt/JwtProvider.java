package kr.co.inter_musica.auth.jwt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class JwtProvider {

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expirationSeconds;

    public JwtProvider(ObjectMapper objectMapper, String secret, long expirationSeconds) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateAccessToken(Long userId) {
        try {
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("alg", "HS256"); // 서명 방식
            header.put("typ", "JWT"); // 토큰 타입

            // LocalDate 방식은 사용 불가능한 이유?
            // JWT 스펙에서 iat (발급 시각) / exp (만료 시각)은 초 단위의 절대 시각 기입이 표준 관행
            long now = Instant.now().getEpochSecond();
            long exp = now + expirationSeconds;

            Map<String, Object> payload = new HashMap<String, Object>();
            payload.put("sub", String.valueOf(userId));
            payload.put("iat", now);
            payload.put("exp", exp);

            String headerJson = objectMapper.writeValueAsString(header);
            String payloadJson = objectMapper.writeValueAsString(payload);

            String headerB64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

            String unsignedToken = headerB64 + "." + payloadB64;
            String signature = hmacSha256(unsignedToken, secret);

            return unsignedToken + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate token", e);
        }
    }

    public boolean validate(String token) {
        try {
            Map<String, Object> payload = parseAndVerify(token);

            long exp = ((Number) payload.get("exp")).longValue();
            long now = Instant.now().getEpochSecond();

            return now <= exp;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        try {
            Map<String, Object> payload = parseAndVerify(token);
            String sub = String.valueOf(payload.get("sub"));
            return Long.parseLong(sub);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> parseAndVerify(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token format");
        }

        String headerB64 = parts[0];
        String payloadB64 = parts[1];
        String signatureB64 = parts[2];

        String unsignedToken = headerB64 + "." + payloadB64;
        String expectedSig = hmacSha256(unsignedToken, secret);

        if (!constantTimeEquals(signatureB64, expectedSig)) {
            throw new IllegalArgumentException("Invalid token signature");
        }

        byte[] payloadBytes = base64UrlDecode(payloadB64);
        Map<String, Object> payload = objectMapper.readValue(payloadBytes, new TypeReference<Map<String, Object>>() {});
        return payload;
    }

    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(digest);
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String b64) {
        return Base64.getUrlDecoder().decode(b64);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
