# ---- Build stage ----
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY . .
# キャッシュ効かせたい場合は、まず settings.gradle / build.gradle / gradle/ を COPY してから deps 解決→その後ソース COPY に分割してもOK
RUN gradle --no-daemon clean bootJar

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# RenderがPORTを注入する。application.propertiesで ${PORT} を読む前提
ENV PORT=8080
# （任意）日本時間にしたい場合
ENV TZ=Asia/Tokyo

# JARを配置（build/libs/ の fat-jar を拾う）
COPY --from=build /app/build/libs/*.jar /app/app.jar

EXPOSE 8080
CMD ["java","-jar","/app/app.jar"]
