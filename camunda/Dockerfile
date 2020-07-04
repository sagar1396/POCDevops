FROM camunda/camunda-bpm-platform:tomcat-latest

# Create app directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app/

RUN apt-get update
RUN apt-get install -y wget

ENV MAVEN_VERSION 3.3.9

RUN wget --no-verbose -O /tmp/apache-maven-3.2.2.tar.gz http://archive.apache.org/dist/maven/maven-3/3.2.2/binaries/apache-maven-3.2.2-bin.tar.gz
RUN echo "87e5cc81bc4ab9b83986b3e77e6b3095 /tmp/apache-maven-3.2.2.tar.gz" | md5sum -c

# install maven
RUN tar xzf /tmp/apache-maven-3.2.2.tar.gz -C /opt/
RUN ln -s /opt/apache-maven-3.2.2 /opt/maven
RUN ln -s /opt/maven/bin/mvn /usr/local/bin
RUN rm -f /tmp/apache-maven-3.2.2.tar.gz
ENV MAVEN_HOME /opt/maven

VOLUME /root/.m2

RUN cd /usr/src/app
RUN mvn clean package

RUN cp -a /usr/src/app/target/bpm-1.0.0.war /camunda/webapps/bpm-1.0.0.war

RUN rm -rf /usr/src/app
RUN rm -rf /opt/maven