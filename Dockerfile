FROM openjdk
MAINTAINER Alexwang
VOLUME /tmp
ADD target/kubeblog.jar /kubeblog.jar
EXPOSE 9100
ENTRYPOINT ["java","-jar","/kubeblog.jar"]