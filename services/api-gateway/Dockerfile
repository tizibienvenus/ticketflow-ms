# Image finale minimale avec JDK 21 Alpine
FROM eclipse-temurin:21-jdk-alpine

# Dossier de travail
WORKDIR /app

# Copier le JAR généré par Maven
COPY target/api-gateway*.jar app.jar

# Exposer le port (optionnel mais propre)
EXPOSE 8080

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]