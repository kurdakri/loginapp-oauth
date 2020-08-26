FROM openjdk:8
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} loginapp-oauth.jar
ENTRYPOINT ["java","-jar","/loginapp-oauth.jar"]