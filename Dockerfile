# Этап 1: Сборка
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Копируем файлы для сборки
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY gradlew ./
COPY gradle.properties ./

# Скачиваем зависимости
RUN ./gradlew dependencies --no-daemon

# Копируем исходный код
COPY src ./src

# Собираем fat JAR
RUN ./gradlew shadowJar --no-daemon

# Этап 2: Запуск
FROM eclipse-temurin:17-jre
WORKDIR /app

# Копируем JAR из первого этапа
COPY --from=build /app/build/libs/*-all.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]