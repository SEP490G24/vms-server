FROM maven:3.8.3-openjdk-17 as builder
WORKDIR /out
# copy all files to /out in builder
COPY . .
RUN mvn clean install
FROM openjdk:17-alpine
# Install libfreetype6
RUN apk --no-cache add freetype
RUN apk --no-cache add ttf-dejavu
WORKDIR /out
ARG JAR_FILE=target/*.jar
ARG CONFIG_FILE=src/main/resources/application-prod.yml
# copy p12 and cer into respective places.
#COPY ./cert/tls/vms.p12 /etc/ssl/certs/vms.p12
#COPY ./cert/tls/cert.cer $JAVA_HOME/lib/security
## command to export cer from p12.
## import cer into java cacerts
#RUN cd $JAVA_HOME/lib/security \
#     && keytool -importcert -trustcacerts -cacerts -storepass changeit -noprompt -file cert.cer -alias VMS
# copy jar
COPY --from=builder /out/${JAR_FILE} ./app.jar
COPY --from=builder /out/${CONFIG_FILE} ./application.yml
# run
ENTRYPOINT ["sh", "-c"]
CMD ["exec java $JAVA_OPTS -jar app.jar --spring.config.location=file:./application.yml"]
