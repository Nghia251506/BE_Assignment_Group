# ========== STAGE 1: Build nhanh + cache cực mạnh ==========
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Cache dependencies trước (chỉ rebuild khi pom thay đổi)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source và build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ========== STAGE 2: Runtime siêu nhẹ, tối ưu Railway 2025 ==========
FROM eclipse-temurin:17-jre-alpine

# Tạo non-root user (Railway khuyến khích)
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

WORKDIR /app

# CHÍNH LÀ DÒNG NÀY PHẢI SỬA – copy đúng tên file + đúng đường dẫn
COPY --from=builder /app/target/news-crawler-*.jar app.jar
# → Không dùng /app/app.jar ở đây, chỉ dùng app.jar thôi
# → Vì ENTRYPOINT bên dưới sẽ chạy java -jar app.jar (không có /app/)

# Environment chuẩn Railway
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0 -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Ho_Chi_Minh"
ENV PORT=8080
EXPOSE ${PORT}

# Healthcheck nhẹ nhất có thể (Railway yêu cầu)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/actuator/health || exit 1

# CHÍNH LÀ DÒNG NÀY PHẢI SỬA – không cần /app/ ở đầu
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]