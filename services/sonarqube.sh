mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
    -Dsonar.projectKey=Smart-Innovation-Plus_camergo-microservices \
    -Dsonar.organization=smart-innovation-plus \
    -Dsonar.host.url=https://sonarcloud.io \
    -Dsonar.token=f944dd62dabc4990e65d2b9a080c073d296d8196 \
    -DskipTests=true