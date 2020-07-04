# Process & page flow engine and modeler
The proof of concept integrating Camunda and Form.io freeware frameworks.


## Dockerized microservices 
Application consists of multiple microservices which are programmed, launched and running separately.  

### Microservices/Dockers
1. Java - REST services, orchestrates Camunda and Form.io REST services;
2. Camunda - REST services and administrator console;
3. Form.io - REST services and UI form/wizard modeler;
4. BPMN modeler;
5. Client - Angular.js client;
6. HAProxy - Connection routing
7. Monitoring - Docker health monitoring;
8. Mongo - NoSQL database

### Install Docker
* Go to website and download Docker: [https://www.docker.com/](https://www.docker.com/)
* Install Docker as described in documentation: [https://docs.docker.com/](https://docs.docker.com/)

**Check your Docker machine:**  
_$ docker-machine ls_

**If Docker machine does not exists, create new one:**  
docker-machine create --driver virtualbox default


### Build and launch docker images
Docker images can be activated separately or together.  
Docker images activation is described in docker-compose.yml

**Launch all dockers with one command:**  
Go to the folder /org.unctad.docker folder and run command:  
_$ docker-compose up -d_

**Check what processes are running:**  
_$ docker ps_  
Now you should see running processes.
Test in your browser http://localhost:6001

**Deactivate dockers:**  
_$ docker-compose down_

### General Docker commands
**List Docker networks:**  
_$ docker network ls_

**List Docker images:**  
_$ docker images_

**Remove Docker image:**  
_$ docker rmi [imageId]_

**List Docker processes:**   
_$ docker ps_

**Stop deactivate docker on container:**  
_$ docker stop [dockerId]_

**Remove docker from container:**  
_$ docker rm [dockerId]_

**Environment settings:**    
_$ docker-machine env_

**Restart docker virtual machine:** 
_$ docker-machine restart_

**Enter into docker node shell:**  
_$ docker exec -it [processId] /bin/bash_

**Check logging with command line:**    
_$ docker logs [processId]_

**Delete all containers:**
docker rm $(docker ps -a -q)

**Delete all images:**
_$ docker rmi $(docker images -q)_

### Swagger integrations
All REST API-s are define in Swagger.
Read more: [http://swagger.io/open-source-integrations/](http://swagger.io/open-source-integrations/)

###Test server  
[http://unctad.redfunction.ee/](http://unctad.redfunction.ee/)
