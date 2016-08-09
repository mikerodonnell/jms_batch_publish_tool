package demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:context.xml"}) // load the context.xml from our test classpath; it has different values than the context.xml from our main classpath
public class BatchSenderTest {

	private static final String MESSAGE_FILE_DIRECTORY = "src/test/resources/message";
	private static final int RECEIVE_TIMEOUT_MILLIS = 5000; // 5 seconds
	private static final String TEMPLATE_DIRECTORY = "src/test/resources/template/";
	
	@Autowired
	private BatchSender batchSender;
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Autowired
	private Destination defaultDestination;
	
	@Autowired
	private ConnectionFactory connectionFactory;
	
	
	// the ActiveMQ in-memory embedded broker allows us to publish and consume messages for more complete testing than could be achieved via mocking.
	@Rule
	public EmbeddedActiveMQBroker brokerRule = new EmbeddedActiveMQBroker();
	
	
	@Test
	public void testSend() throws Exception {
		int messageCount = 2; // send 2 simple test messages
		
		batchSender.send( messageCount);
		
		// now that we've done the publish, create a test consumer for our embedded broker to assert that the correct number of messages were published
		Connection connection = null;
		Session consumerSession = null;
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			consumerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageConsumer consumer = consumerSession.createConsumer(defaultDestination);
			
			for( int receivedCount=0; receivedCount<messageCount; receivedCount++ ) // verify that all expected messages were published
				assertNotNull( consumer.receive(RECEIVE_TIMEOUT_MILLIS) );
			
			assertNull( consumer.receive(RECEIVE_TIMEOUT_MILLIS) ); // now verify that no more messages were published
		}
		finally {
			if(consumerSession != null)
				consumerSession.close();
			
			if(connection != null)
				connection.close();
		}
	}
	
	
	@Test
	public void testSendWithFile() throws Exception {
		int messageCount = 2; // our resources/message/ directory has 2 message files
		
		final File messageDirectory = new File(MESSAGE_FILE_DIRECTORY);
		final File[] messageFiles = messageDirectory.listFiles();
		
		batchSender.send( messageFiles );
		
		// now that we've done the publish, create a test consumer for our embedded broker to assert that the correct message contents were published
		for( int receivedCount=0; receivedCount<messageCount; receivedCount++ ) {
			TextMessage message = consumeNextMesage();
			// verify the body of the JMS message matches the contents of the File object
			assertEquals( FileUtils.readFileToString(messageFiles[receivedCount]), message.getText() );
		}
		
		assertNull( consumeNextMesage() ); // now verify that no more messages were published
	}
	
	
	@Test
	public void testSendWithTemplate() throws Exception {
		int messageCount = 3; // our inpts.properties has data for 3 messages
		
		File template = new File(TEMPLATE_DIRECTORY + "template");
		File inputs = new File(TEMPLATE_DIRECTORY + "inputs.properties");
		
		Properties properties = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(inputs);
			properties.load(input);
		}
		finally {
			if (input != null)
				input.close();
		}
		
		batchSender.send( template, properties );
		
		// now that we've done the publish, create a test consumer for our embedded broker to assert that the correct message contents were published
		for( int receivedCount=0; receivedCount<messageCount; receivedCount++ ) {
			TextMessage message = consumeNextMesage();
			File renderedTemplate = new File( TEMPLATE_DIRECTORY + "rendered_template_" + receivedCount );
			// verify the body of the JMS message matches the "rendered" stub files
			assertEquals( FileUtils.readFileToString(renderedTemplate), message.getText() );
		}
		
		assertNull( consumeNextMesage() ); // now verify that no more messages were published
	}
	
	
	/**
	 * helper method to read the next JMS message from our embedded broker's test queue. returns null if no messages are on the queue.
	 * 
	 * @return
	 * @throws JMSException
	 */
	protected TextMessage consumeNextMesage() throws JMSException {
		TextMessage message = null;
		Connection jmsConnection = null;
		Session consumerSession = null;
		
		try {
			jmsConnection = connectionFactory.createConnection();
			jmsConnection.start();
			consumerSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageConsumer consumer = consumerSession.createConsumer(defaultDestination);
			
			// consume 1 message from the queue
			message = (TextMessage) consumer.receive(RECEIVE_TIMEOUT_MILLIS);
		}
		finally {
			if(consumerSession != null)
				consumerSession.close();
			
			if(jmsConnection != null)
				jmsConnection.close();
		}
		
		return message;
	}
	
}
