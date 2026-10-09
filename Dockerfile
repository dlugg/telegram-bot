FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /dockerTgBot
COPY pom.xml /dockerTgBot
COPY src /dockerTgBot/src
RUN mvn package -DskipTests


FROM eclipse-temurin:21-jre
COPY --from=build /dockerTgBot/target/tgBot-1.0-SNAPSHOT.jar /app/bot.jar
CMD ["java", "-jar", "/app/bot.jar"]
