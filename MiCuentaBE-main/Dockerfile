FROM maven:3.9.6-eclipse-temurin-21

WORKDIR /app

COPY MiCuentaBE-main/pom.xml .
COPY MiCuentaBE-main/src ./src

RUN mvn clean package -DskipTests=true -Dmaven.test.skip=true

EXPOSE 8080

CMD ["sh", "-c", "java -Dserver.port=$PORT -jar target/*.jar"]