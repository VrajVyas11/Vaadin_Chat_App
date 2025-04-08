# # Build stage
# FROM eclipse-temurin:17-jdk-jammy as builder
# WORKDIR /workspace/app

# # Copy Maven wrapper and POM first
# COPY .mvn .mvn
# COPY mvnw .
# COPY pom.xml .

# # Add execute permissions to mvnw
# RUN chmod +x mvnw

# # Download dependencies
# RUN ./mvnw dependency:go-offline

# # Copy source files
# COPY src src

# # Build with production profile
# RUN ./mvnw clean package -Pproduction -DskipTests

# # Runtime stage
# FROM eclipse-temurin:17-jre-jammy
# WORKDIR /app
# COPY --from=builder /workspace/app/target/*.jar app.jar
# ENTRYPOINT ["java", "-jar", "app.jar"]

### After anirudh private chat
# Build stage
FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR /workspace/app

# Copy Maven wrapper and POM first
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# Add execute permissions to mvnw
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy all source files and frontend resources
COPY src src
COPY frontend frontend
COPY package.json .
COPY tsconfig.json .
COPY vite.config.ts .
COPY types.d.ts .

# Ensure the CSS file is in both possible locations Vaadin might look for it
RUN mkdir -p src/main/frontend/themes/chatappgroupproject/
RUN cp frontend/themes/chatappgroupproject/private-chat.css src/main/frontend/themes/chatappgroupproject/ || true

# Build with production profile
RUN ./mvnw clean package -Pproduction -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/app/target/*.jar app.jar

# Set environment variables if needed
# ENV SERVER_PORT=8080

# Expose the application port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]