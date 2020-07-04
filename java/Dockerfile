FROM tomcat

# Install Java
RUN mkdir /opt/jdk && cd /opt
RUN wget -c --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u131-b11/d54c1d3a095b4ff2b6607d096fa80163/jdk-8u131-linux-x64.tar.gz
RUN tar -zxf jdk-8u131-linux-x64.tar.gz -C /opt/jdk
RUN update-alternatives --install /usr/bin/java java /opt/jdk/jdk1.8.0_131/bin/java 100
RUN update-alternatives --install /usr/bin/javac javac /opt/jdk/jdk1.8.0_131/bin/javac 100
RUN mkdir /etc/ssl/certs/java/
RUN cp /opt/jdk/jdk1.8.0_131/jre/lib/security/cacerts /etc/ssl/certs/java/
#RUN wget https://download.java.net/openjdk/jdk11/ri/openjdk-11+28_linux-x64_bin.tar.gz
#RUN tar -zxf openjdk-11+28_linux-x64_bin.tar.gz -C /opt/jdk/
#RUN cp /opt/jdk/java-se-8u41-ri/jre/lib/security/cacerts /etc/ssl/certs/java/

ENV JAVA_HOME /opt/jdk/jdk1.8.0_131/

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
RUN mvn clean package

RUN cp -a /usr/src/app/target/java.war /usr/local/tomcat/webapps/

RUN rm -rf /usr/src/app
RUN rm -rf /usr/share/maven
