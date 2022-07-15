FROM openjdk:11
VOLUME /tmp
ADD build/libs/*.jar dockerimage.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","dockerimage.jar"]

