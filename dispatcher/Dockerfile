FROM openjdk:11
COPY target/dispatcher-1.0-SNAPSHOT-shaded.jar /opt/zorby/dispatcher.jar
WORKDIR /opt/zorby
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "dispatcher.jar"]