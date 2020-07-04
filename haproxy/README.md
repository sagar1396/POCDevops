# haproxy docker
haproxy is manager proxy which is hub for REST services.
haproxy routes other docker REST services.

## Build docker image
Go to the project folder and run command:  
_$ docker build -t krixerx/haproxy ._

## Start microservice
_$ docker run -d -p 6000:6000 krixerx/haproxy_

## Check microservice
[http://192.168.99.100:6000/hello/John](http://192.168.99.100:6000/hello/John)

