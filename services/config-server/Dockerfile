# Image finale minimale avec JDK 21 Alpine
FROM eclipse-temurin:21-jdk-alpine

# Installer curl (et éventuellement bash si besoin, mais curl suffit)
RUN apk add --no-cache curl

# Dossier de travail
WORKDIR /app

# Copier le JAR généré par Maven
COPY target/config-server*.jar app.jar

# Exposer le port (optionnel mais propre)
EXPOSE 8888

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
