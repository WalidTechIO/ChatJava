FROM amazoncorretto:17.0.10
COPY ./client.jar /home/app/
COPY ./javafx-linux /home/app/javafx-linux
RUN yum update -y && yum install -y gtk3
WORKDIR /home/app
CMD ["java", "-Djava.library.path=/home/app/javafx-linux/lib",  "--module-path", "/home/app/javafx-linux/lib",  "--add-modules", "javafx.controls,javafx.fxml" , "-jar", "client.jar"]