# 학교 서버 배포용: 서버에 Java가 없어도 Docker 안에서 빌드·실행
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test \
 && cp "$(ls build/libs/*.jar | grep -v plain)" /app.jar

FROM eclipse-temurin:21-jre
ENV TZ=Asia/Seoul
WORKDIR /app
COPY --from=build /app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
