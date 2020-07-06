FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ./target/southsystem-process-api-0.0.1-SNAPSHOT.jar southsystem-process-api.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/southsystem-process-api.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]