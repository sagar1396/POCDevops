FROM tomcat

# Install Java
RUN mkdir /opt/jdk && cd /opt
RUN wget https://download.java.net/openjdk/jdk8u41/ri/openjdk-8u41-b04-linux-x64-14_jan_2020.tar.gz
RUN tar -zxf openjdk-8u41-b04-linux-x64-14_jan_2020.tar.gz -C /opt/jdk
RUN update-alternatives --install /usr/bin/java java /opt/jdk/java-se-8u41-ri/bin/java 100
RUN update-alternatives --install /usr/bin/javac javac /opt/jdk/java-se-8u41-ri/bin/javac 100
RUN mkdir /etc/ssl/certs/java/
RUN cp /opt/jdk/java-se-8u41-ri/jre/lib/security/cacerts /etc/ssl/certs/java/


ENV JAVA_HOME /opt/jdk/java-se-8u41-ri/

# Create app directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app/

ENV MAVEN_VERSION 3.6.3

RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
&& mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
&& ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven

VOLUME /root/.m2

RUN cd /usr/src/app
RUN mvn clean package -Djavax.net.ssl.trustStore=...

RUN cp -a /usr/src/app/target/java.war /usr/local/tomcat/webapps/

RUN rm -rf /usr/src/app
RUN rm -rf /usr/share/maven
