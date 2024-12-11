# Start from an OpenJDK base image with Maven
FROM maven:3.6.3-openjdk-8-slim

# Copy the source code and dependencies to the container
COPY . /app
WORKDIR /app

# Build the application inside the container
RUN mvn clean package -DskipTests

# Expose port 8080
EXPOSE 8089

# Start the app
ENTRYPOINT ["java", "-jar", "target/app.jar"]
