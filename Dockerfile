# Stage 1: Build the application
FROM maven:4.0.0-openjdk-17 as build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM adoptopenjdk:17-jdk-hotspot
WORKDIR /app
COPY --from=build /app/target/news-crawler.jar /app/news-crawler.jar
ENTRYPOINT ["java", "-jar", "news-crawler.jar"]
