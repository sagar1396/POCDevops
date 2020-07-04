# Java hello docker
Hello REST service example with corresponding Dockerfile.
Java and Maven based project.

To build and launch Java hello docker separately follow following instructions.

## Requirements
1. Java
2. Maven

## Generate Java REST stubs from swagger

REST schemas defined in Swagger located in /src/main/resources/api.yaml

Read more:
1. https://github.com/swagger-api/swagger-codegen/wiki/Server-stub-generator-HOWTO 
2. https://github.com/swagger-api/swagger-codegen/blob/master/README.md

## Build war package
Generate war file to /target folder: _mvn clean install_

## Build Docker image
1. Go to the Java Docker project in your computer.
Run following docker command to build docker image:
_$ docker build -t krixerx/docker-java ._
2. Check images: _$ docker images_

## Set up right Java
By default OpenJdk is used on Docker which doesn't match.
Install the Oracle JRE:
_$ docker run java:8-jre-alpine_

## Launch the docker image to container
_$ docker run -d -p 8080:8080 krixerx/docker-java_

## Check processes: 
_$ docker ps_

## Check results
Check url: [http://192.168.99.100:8080/java/rest/hello/John](http://192.168.99.100:8080/java/rest/hello/John)
