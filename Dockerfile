FROM maven:3.9.11-openjdk-17 AS build

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY --from=build /app/target/TradingBit-1.0.0.jar TradingBit.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "TradingBit.jar"]