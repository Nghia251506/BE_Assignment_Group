# ========== STAGE 1: Build với Maven (dùng version mới nhất, nhẹ) ==========
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder

# Cache dependencies trước (tăng tốc rebuild khi chỉ thay code)
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Chỉ copy pom trước + download deps (layer cache cực mạnh)
RUN mvn dependency:go-offline -B

# Build fat jar, skip test + dùng profile production nếu có
RUN mvn clean package -DskipTests -P production -B

# ========== STAGE 2: Runtime siêu nhẹ với JRE 17 (dùng distroless hoặc temurin jre) ==========
# Cách 1: Dùng eclipse-temurin JRE alpine (nhẹ + vẫn có shell để debug khi cần)
FROM eclipse-temurin:17-jre-alpine

# Cách 2: Dùng gcr distroless nếu anh muốn bảo mật max (không có shell gì hết)
# FROM gcr.io/distroless/java17-debian12

LABEL maintainer="nghiant"
LABEL org.opencontainers.image.source="https://github.com/Nghia251506/BE_Assignment_Group.git"
LABEL org.opencontainers.image.branch="nghiant"

# Tạo non-root user (Railway + security best practice)
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

WORKDIR /app

# Copy jar từ stage build (dùng wildcard nhưng chỉ có 1 jar thôi)
COPY --from=builder /app/target/news-crawler-*.jar /app/news-crawler.jar
# Nếu anh dùng Spring Boot 3 + build với maven như mặc định thì tên jar thường là:
# COPY --from=builder /app/target/*.jar /app/news-crawler.jar

# Tối ưu JVM cho môi trường container (Railway giới hạn memory)
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.awt.headless=true \
               -Dfile.encoding=UTF-8 \
               -Dsun.stdout.encoding=UTF-8 \
               -Dsun.stderr.encoding=UTF-8"

# Expose port (Railway sẽ tự map, nhưng vẫn nên khai báo)
EXPOSE 8080

# Health check để Railway biết app sống hay chết
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

# Chạy với graceful shutdown + bind all interfaces
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/news-crawler.jar --server.address=0.0.0.0 --server.port=${PORT:-8080}"]