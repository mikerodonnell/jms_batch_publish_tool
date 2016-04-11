
##About

A simple JMS producer tool that can be used for connectivity/configuration verification, replaying traffic, or poor man's load testing. For message body, choose between files in a directory or a generic test message.
<br>
<br>

##Usage

1. clone the repository: `git clone git@github.com:mikerodonnell/jms_batch_publisher.git`
<br>
2. `mvn clean install`
<br>
3. edit src/main/resources/connection.properties with your JMS broker connection information.
<br>
4. if using files for the message body, place the ASCII files in src/main/resources. one JMS messsage will be sent for each file.
<br>
5. `mvn exec:java`
<br>
6. follow the console prompts to select delay between each message, and whether to use a directory of files for message body or a generic test message. if using a generic test message, you'll be prompted for a message count.
<br>
<br>

##Tools used

* [Apache ActiveMQ](http://activemq.apache.org)
* [Spring](https://spring.io) _for dependency/properties injection and JMS connection factory_
* [Maven Exec plugin](http://www.mojohaus.org/exec-maven-plugin) _for easy usage through command line._
* [Apache Commons Lang](https://commons.apache.org/proper/commons-lang) and [Commons IO](https://commons.apache.org/proper/commons-io)
