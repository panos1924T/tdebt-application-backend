# Stage 1: Build
FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# Stage 2: Run
FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /app/build/libs/*.jar ./app.jar
EXPOSE ${PORT:-8080}
CMD ["java", "-jar", "app.jar"]