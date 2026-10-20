FROM eclipse-temurin:25-jdk as maven-build

COPY mvnw app/mvnw
COPY .mvn app/.mvn
COPY pom.xml app/pom.xml

WORKDIR /app

RUN ./mvnw dependency:go-offline --no-transfer-progress --batch-mode

COPY src ./src

RUN ./mvnw package -DskipTests --no-transfer-progress --batch-mode

FROM eclipse-temurin:25-jre as app-run

COPY --from=maven-build app/target/product-catalog-api.jar app/product-catalog-api.jar

WORKDIR /app

ENTRYPOINT ["java", "-jar", "product-catalog-api.jar"]