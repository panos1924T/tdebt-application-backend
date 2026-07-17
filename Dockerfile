# Stage 1: Build
FROM amazoncorretto:21 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

# Stage 2: Run
FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /app/build/libs/*.jar ./app.jar
EXPOSE ${PORT:-8080}
CMD ["java", "-jar", "app.jar"]