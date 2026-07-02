FROM eclipse-temurin:21-jdk
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENV TZ=Asia/Seoul
EXPOSE 80
ENTRYPOINT java -jar -Djasypt.encryptor.password=${JASYPT_PASSWORD} -D<파일경로관련 변수>=/uploadtest/ app.jar
