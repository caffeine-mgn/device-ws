FROM bellsoft/liberica-openjdk-alpine:21.0.6-10
COPY build/libs/full-application.jar /app.jar

ENV J_XMX "2048m"
ENV J_XMS "512m"
ENV J_PREFOMANCE_FLAGS "-XX:+PerfDisableSharedMem -Djava.security.egd=file:/dev/./urandom -XX:+UseContainerSupport -XX:+AlwaysActAsServerClassMachine"

CMD java -Xmx$J_XMX -Xms$J_XMS $J_PREFOMANCE_FLAGS -jar /app.jar