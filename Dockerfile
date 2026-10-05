FROM eclipse-temurin:21-jre
COPY target/tgBot-1.0-SNAPSHOT.jar /app/bot.jar
CMD ["java", "-jar", "/app/bot.jar"]
