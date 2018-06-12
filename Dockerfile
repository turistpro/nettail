FROM openjdk:8-jre-alpine
EXPOSE 8080
ADD *.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
