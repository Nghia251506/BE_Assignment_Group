package com.tns.newscrawler.config;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "google")
@Data
public class Ga4Config {

    private String projectId;
    private String clientEmail;
    private String privateKey;           // local dev
    private String credentialsBase64;    // production
    private String propertyId;

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {

        // ƯU TIÊN CAO NHẤT: Dùng Base64 (production) → chạy ngon 100%
        if (credentialsBase64 != null && !credentialsBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(credentialsBase64.trim());
            return GoogleCredentials.fromStream(new ByteArrayInputStream(decoded))
                    .createScoped("https://www.googleapis.com/auth/analytics.readonly");
        }

        // LOCAL DEV: Dùng privateKey – fix triệt để lỗi PKCS#8
        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException("GA4 private key chưa được cấu hình!");
        }

        // BÍ KÍP ĐÂY: thay \\n thành \n thật, rồi split + join lại để đảm bảo đúng xuống dòng
        String cleanKey = privateKey
                .replace("\\n", "\n")        // chuyển \n thành xuống dòng thật
                .replace("\r", "")           // bỏ \r nếu có
                .trim();

        // Đảm bảo có đúng 1 dòng trống cuối cùng
        if (!cleanKey.endsWith("\n")) {
            cleanKey += "\n";
        }

        String json = """
            {
              "type": "service_account",
              "project_id": "%s",
              "private_key_id": "any",
              "private_key": "%s",
              "client_email": "%s",
              "client_id": "any",
              "auth_uri": "https://accounts.google.com/o/oauth2/auth",
              "token_uri": "https://oauth2.googleapis.com/token",
              "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
              "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/%s"
            }
            """.formatted(projectId, cleanKey, clientEmail, clientEmail.replace("@", "%40"));

        return GoogleCredentials.fromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
                .createScoped("https://www.googleapis.com/auth/analytics.readonly");
    }
}