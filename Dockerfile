FROM eclipse-temurin:17

RUN apt-get update && apt-get install -y redis-server

COPY target/scala-3.1.3/data_access.jar /srv/data_access.jar

# Should always be set when deployed anyway, but this is a sane default
ENV BN_ENV=${BN_ENV:-production}
CMD redis-server --daemonize yes && java -jar /srv/data_access.jar
