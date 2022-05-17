FROM openjdk:11
COPY target/dispatcher-0.2.0-SNAPSHOT-shaded.jar /opt/statefun-tsukuyomi/dispatcher.jar
WORKDIR /opt/statefun-tsukuyomi
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "dispatcher.jar"]