# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 as build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-oracle

# Set the working directory in the container
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/news-crawler.jar /app/news-crawler.jar

# Expose the port your application will run on (commonly 8080 for Spring Boot apps)
EXPOSE 8080

# Ensure the app runs on all network interfaces, not just localhost
ENTRYPOINT ["java", "-jar", "/app/news-crawler.jar", "--server.address=0.0.0.0"]

