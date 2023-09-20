FROM maven:3.8.3-openjdk-17 as builder
WORKDIR /out
# copy all files to /out in builder
COPY . .
RUN mvn clean install
FROM openjdk:17-alpine
WORKDIR /out
ARG JAR_FILE=target/*.jar
# copy p12 and cer into respective places.
COPY ./cert/tls/vms.p12 /etc/ssl/certs/vms.p12
COPY ./creds/tls/cert.cer $JAVA_HOME/lib/security
# command to export cer from p12.
# import cer into java cacerts
RUN cd $JAVA_HOME/lib/security \
     && keytool -importcert -trustcacerts -cacerts -storepass changeit -noprompt -file cert.cer -alias VMS
# copy jar
COPY --from=builder /out/${JAR_FILE} ./app.jar
# run
ENTRYPOINT ["java", "-jar", "app.jar"]