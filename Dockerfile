# 第一階段：編譯 (改用 maven 搭配 eclipse-temurin 的 Java 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# 第二階段：執行 (改用 eclipse-temurin 官方推薦的輕量化 jre 映像檔)
FROM eclipse-temurin:21-jre-alpine
COPY --from=build /target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]