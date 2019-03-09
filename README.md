# kafkaQueue
Repository to manage kafka queue

### How to import project for editing ###

* Import as maven project in your IDE

### Pre-requisite to run the application server

Install apache-kafka_2.12-2.1.0

Run following commands to run zookeeper and kafka servers

	zookeeper-server-start ../config/zookeeper.properties
	kafka-server-start ../config/server.properties

Configure following properties

	server.port=8190
	# Kafka configuration
	kafka.bootstrap.server=localhost:9092
	kafka.server=localhost:2181
	kafka.topic=backupDevice
	kafka.group=cisco

### Build, install and run application ###

To get started build the build the latest sources with Maven 3 and Java 8 
(or higher). 

	$ cd kafkaQueue
	$ mvn clean install 

You can run this application as spring-boot app by following command:

	$ mvn spring-boot:run

Once done you can run the application by executing 

	$ java -jar target/kafkaQueue-x.x.x-SNAPSHOT-exec.jar

## API endpoints description

### /kafka/send

API to send message in kafka queue default topic.

	Method: POST
	Params:
		msg	*		String 	string or json message or messages list
	Response:
		Json		Success result or error
