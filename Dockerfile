FROM eclipse-temurin:17

COPY target/scala-3.1.3/data_access.jar /srv/data_access.jar

# Should always be set when deployed anyway, but this is a sane default
ENV BN_ENV=${BN_ENV:-production}
CMD java -jar /srv/data_access.jar
