## about

A simple standalone JMS producer for connectivity/configuration verification, replaying traffic, or poor man's load testing. For the message body, choose between
 * a simple default test message
 * a custom template, and a CSV with variables
 * files in a configured directory

Unit testing is powered by ActiveMQ's [embedded broker](http://activemq.apache.org/how-to-unit-test-jms-code.html).

## usage

1. requires Java 1.6 or later
1. `git clone git@github.com:mikerodonnell/jms_batch_publisher.git`
1. edit src/main/resources/connection.properties with your JMS broker connection information.
1. if using files for the message body, place them in src/main/resources/message/. one message will be published for each file.
1. if using a template for the message body, edit src/main/resources/template/template and src/main/resources/template/inputs.csv as desired. see example in src/main/resources/template/inputs.csv.
1. `mvn clean install`
1. `mvn exec:java`
1. follow the console prompts to select message body and delay between each messages. if using default test messages, you'll be prompted for a message count.

## tools

* [Apache ActiveMQ](http://activemq.apache.org)
* [Apache ActiveMQ Embedded Broker](http://activemq.apache.org/how-to-unit-test-jms-code.html) _for in-memory embedded broker, allowing more complete testing than could be achieved via mocking_
* [Spring](https://spring.io) _for dependency/properties injection and JMS connection factory_
* [JUnit](http://junit.org)
* [Maven Exec plugin](http://www.mojohaus.org/exec-maven-plugin) _for easy usage through command line._
* [Apache Commons CSV](https://commons.apache.org/proper/commons-csv) _for CSV parsing_
* [Apache Commons Lang](https://commons.apache.org/proper/commons-lang) and [Commons IO](https://commons.apache.org/proper/commons-io)
