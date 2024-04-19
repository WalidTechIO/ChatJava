FROM amazoncorretto:17.0.10
COPY ./server.jar /home/app/
WORKDIR /home/app
ENTRYPOINT ["java", "-jar", "server.jar"]
CMD ["7777"]