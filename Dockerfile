# ========== STAGE 1: Build với Maven (nhẹ + cache cực mạnh) ==========
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Cache dependencies trước (chỉ rebuild khi pom thay đổi)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source + build (SKIP TEST + KHÔNG DÙNG PROFILE production → fix lỗi profile không tồn tại + lỗi UTF-8)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ========== STAGE 2: Runtime siêu nhẹ + bảo mật ==========
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="nghiant"
LABEL org.opencontainers.image.source="https://github.com/Nghia251506/BE_Assignment_Group.git"

# Tạo non-root user (bảo mật)
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

WORKDIR /app

# Copy jar từ stage builder (dùng wildcard an toàn)
COPY --from=builder /app/target/news-crawler-*.jar /app/news-crawler.jar

# Tối ưu JVM + fix encoding cho container
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=80.0 \
               -Dfile.encoding=UTF-8 \
               -Dspring.profiles.active=production"

# Port linh hoạt (Railway, Render, Docker đều dùng được)
ENV PORT=8080
EXPOSE ${PORT}

# Health check cho Railway/Render
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/actuator/health || exit 1

# Chạy app (PORT từ env, fallback 8080)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/news-crawler.jar --server.port=${PORT}"]
