FROM openjdk:17.0.1-jdk-slim

WORKDIR /app

COPY target/todoapp-0.0.1-SNAPSHOT.jar /app/

EXPOSE 8080

CMD ["java", "-jar", "todoapp-0.0.1-SNAPSHOT.jar"]
