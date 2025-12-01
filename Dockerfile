FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "$JAVA_OPTS", "-jar", "app.jar"]
#ENTRYPOINT ["java", "-jar", "app.jar"]


## Dockerfile 내에서 VM 옵션을 환경 변수로 설정
 #ENV JAVA_OPTS="-Xmx512m -Dspring.profiles.active=prod"
 #
 ## ENTRYPOINT가 java 명령어를 실행하도록 설정되어 있으면 이 옵션이 자동 적용됨
 #ENTRYPOINT ["java", "$JAVA_OPTS", "-jar", "app.jar"]