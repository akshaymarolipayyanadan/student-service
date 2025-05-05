# Use a base image compatible with all platforms
FROM openjdk:17-oracle

# Set the working directory
WORKDIR /app

# Copy the built JAR file
COPY target/student-service-0.0.1-SNAPSHOT.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
