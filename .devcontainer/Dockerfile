FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine
ENV APP_HOME /usr/app

COPY src/main/resources/application-prod.yml $APP_HOME/application.yml
COPY target/app.jar $APP_HOME/app.jar
COPY $JAR_FILE $APP_HOME/app.jar

ADD ./cert $APP_HOME/cert
RUN ls -la $APP_HOME

WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java $JAVA_OPTS -jar app.jar --spring.config.location=file:./application.yml"]
