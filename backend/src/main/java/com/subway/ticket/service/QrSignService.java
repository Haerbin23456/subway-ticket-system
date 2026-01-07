package com.subway.ticket.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class QrSignService {
    private final String secret;

    public QrSignService(String secret) {
        this.secret = secret;
    }

    public String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] h = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(h);
        } catch (Exception e) {
            throw new RuntimeException("SIGN_ERROR" + e.getMessage(), e);
        }
    }

    public boolean verify(String data, String signature) {
        String s = sign(data);
        return constantEquals(s, signature);
    }

    private boolean constantEquals(String a, String b) {
        if (a == null || b == null) return false;
        int n1 = a.length();
        int n2 = b.length();
        int n = Math.max(n1, n2);
        int res = 0;
        for (int i = 0; i < n; i++) {
            char c1 = i < n1 ? a.charAt(i) : 0;
            char c2 = i < n2 ? b.charAt(i) : 0;
            res |= c1 ^ c2;
        }
        return res == 0;
    }
}
