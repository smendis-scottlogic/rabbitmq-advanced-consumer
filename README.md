# Getting Started

### Prerequisites
* Docker Desktop
* Java SDK 17

### Docker Container Commands

To create a new docker container run

`docker-compose -p "rabbitmq-poc" up`

To restart an existing container

`docker-compose -p "rabbitmq-poc" start`

To stop a container

`docker-compose -p "rabbitmq-poc" stop`

To destroy an existing container

`docker-compose -p "rabbitmq-poc" down`

### Management Console

* URL: [http://localhost:15672/](http://localhost:15672/)
* Username: guest
* Password: guest

