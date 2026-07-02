FROM eclipse-temurin:21-jdk
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENV TZ=Asia/Seoul
EXPOSE 80
ENTRYPOINT java -Djasypt.encryptor.password=${JASYPT_PASSWORD} -Dfile.dir=/uploadtest/ -jar app.jar
