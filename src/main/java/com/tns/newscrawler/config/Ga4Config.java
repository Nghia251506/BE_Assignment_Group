package com.tns.newscrawler.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "google")
@Data
public class Ga4Config {

    private String projectId;
    private String clientEmail;
    private String privateKey;           // local dev: dùng \n
    private String credentialsBase64;    // production: dùng base64
    private String propertyId;

    @Bean
    public GoogleCredentials googleCredentials() throws Exception {

        // ƯU TIÊN 1: PRODUCTION – DÙNG BASE64 (AN TOÀN NHẤT, KHÔNG BỊ LỖI \n)
        if (credentialsBase64 != null && !credentialsBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(credentialsBase64.trim());
            return GoogleCredentials.fromStream(new ByteArrayInputStream(decoded))
                    .createScoped("https://www.googleapis.com/auth/analytics.readonly");
        }

        // ƯU TIÊN 2: LOCAL DEV – DÙNG PRIVATE KEY (fix triệt để lỗi xuống dòng)
        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException("GA4 private key chưa được cấu hình! Thiếu google.private-key hoặc google.credentials-base64");
        }

        // BÍ KÍP CHỐNG LỖI "Unterminated object" – xử lý \n chuẩn 100%
        String fixedKey = privateKey
                .replace("\\n", "\n")     // chuyển \n thành xuống dòng thật
                .replace("\r\n", "\n")    // fix Windows line ending
                .replace("\r", "\n")      // fix Mac cũ
                .trim();

        // Đảm bảo key có đúng định dạng PEM
        if (!fixedKey.startsWith("-----BEGIN PRIVATE KEY-----")) {
            fixedKey = "-----BEGIN PRIVATE KEY-----\n" + fixedKey;
        }
        if (!fixedKey.endsWith("-----END PRIVATE KEY-----")) {
            fixedKey = fixedKey + "\n-----END PRIVATE KEY-----";
        }

        String json = String.format("""
            {
              "type": "service_account",
              "project_id": "%s",
              "private_key_id": "key-id",
              "private_key": "%s",
              "client_email": "%s",
              "client_id": "client-id",
              "auth_uri": "https://accounts.google.com/o/oauth2/auth",
              "token_uri": "https://oauth2.googleapis.com/token",
              "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
              "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/%s"
            }
            """, projectId, fixedKey, clientEmail, clientEmail.replace("@", "%40"));

        return GoogleCredentials.fromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
                .createScoped("https://www.googleapis.com/auth/analytics.readonly");
    }

    // Dễ dùng trong controller
    public String getPropertyId() {
        return propertyId;
    }
}
