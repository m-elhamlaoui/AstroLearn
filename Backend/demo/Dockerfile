# Stage 1: Build
FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app

# Copy Maven wrapper files (corrected from .mwn to .mvn)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Make mvnw executable and convert line endings if needed
RUN chmod +x mvnw && \
    sed -i 's/\r$//' mvnw  # Remove Windows CR characters

# Download dependencies
RUN ./mvnw dependency:go-offline -Dmaven.repo.local=./.m2/repository

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw package -DskipTests -Dmaven.repo.local=./.m2/repository

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy JAR from builder stage (corrected syntax)
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "/app/app.jar"]