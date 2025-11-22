# Build stage
FROM maven:4.0.0-openjdk-17 as build

WORKDIR /app

# Sao chép toàn bộ mã nguồn vào container
COPY . /app

# Build ứng dụng (bỏ qua tests)
RUN mvn clean package -DskipTests

# Final stage
FROM openjdk:17

WORKDIR /app

# Sao chép file .jar từ build stage vào final container
COPY --from=build /app/target/news-crawler.jar /app/news-crawler.jar

# Expose port 8888
EXPOSE 8888

# Chạy ứng dụng Spring Boot
CMD ["java", "-jar", "/app/news-crawler.jar"]
