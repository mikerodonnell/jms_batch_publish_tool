
##About

A simple standalone JMS producer tool that can be used for connectivity/configuration verification, replaying traffic, or poor man's load testing. For the message body, create a template with variables, or choose a generic test message. Unit testing is powered by ActiveMQ's [embedded broker](http://activemq.apache.org/how-to-unit-test-jms-code.html).
<br>
<br>

##Usage

1. clone the repository: `git clone git@github.com:mikerodonnell/jms_batch_publisher.git`
<br>
2. `mvn clean install`
<br>
3. edit src/main/resources/connection.properties with your JMS broker connection information.
<br>
4. if using a template for the message body, edit src/main/resources/template/template and src/main/resources/template/inputs.properties as desired. see example in src/main/resources/template/inputs.properties.
<br>
5. `mvn exec:java`
<br>
6. follow the console prompts to select delay between each message, and whether to use a template or generic test message. if using a generic test message, you'll be prompted for a message count.
<br>
<br>

##Tools used

* [Apache ActiveMQ](http://activemq.apache.org)
* [Apache ActiveMQ Embedded Broker](http://activemq.apache.org/how-to-unit-test-jms-code.html) _for in-memory embedded broker, allowing more complete testing than could be achieved via mocking_
* [Spring](https://spring.io) _for dependency/properties injection and JMS connection factory_
* [JUnit](http://junit.org)
* [Maven Exec plugin](http://www.mojohaus.org/exec-maven-plugin) _for easy usage through command line._
* [Apache Commons Lang](https://commons.apache.org/proper/commons-lang) and [Commons IO](https://commons.apache.org/proper/commons-io)

