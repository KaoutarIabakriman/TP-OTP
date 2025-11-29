FROM tomcat:10.1.48-jdk17

RUN rm -rf /usr/local/tomcat/webapps/*

COPY ./build/libs/TP3-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
