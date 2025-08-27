# ---- Build stage ----
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle --no-daemon clean bootJar

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# ↓ これを書いていたなら削除する
# ENV PORT=8080
# EXPOSE 8080   # 無くてOK（残ってても害はない）

COPY --from=build /app/build/libs/*.jar /app/app.jar
# Render が渡す $PORT で待ち受ける
CMD ["sh","-c","java -Dserver.port=$PORT -jar /app/app.jar"]
