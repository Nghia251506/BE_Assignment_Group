# ========== STAGE 1: Build nhanh + cache cực mạnh ==========
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Cache dependencies trước (chỉ rebuild khi pom.xml thay đổi)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source và build (skip test)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ========== STAGE 2: Runtime siêu nhẹ, tối ưu cho Railway + Redis ==========
FROM eclipse-temurin:17-jre-alpine

# Metadata
LABEL maintainer="nghiant"
LABEL org.opencontainers.image.source="https://github.com/Nghia251506/BE_Assignment_Group"

# Tạo user non-root (Railway yêu cầu bảo mật ong)
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

WORKDIR /app

# Copy JAR đã build
COPY --from=builder /app/target/news-crawler-*.jar /app/app.jar

# Environment variables chuẩn cho Railway + Redis + MySQL
ENV JAVA_OPTS="\
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=80.0 \
  -Dfile.encoding=UTF-8 \
  -Duser.timezone=Asia/Ho_Chi_Minh \
  -Dspring.profiles.active=production"

# Railway sẽ tự inject PORT, Redis, MySQL env → không cần hardcode
ENV PORT=8080
EXPOSE ${PORT}

# Healthcheck CHUẨN Railway 2025 (dùng curl có sẵn trong JRE Alpine)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/actuator/health || exit 1

# Chạy app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --server.port=${PORT}"]
