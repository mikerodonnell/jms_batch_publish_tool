package demo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;

import org.springframework.jms.core.JmsTemplate;

/**
 * publish batches of JMS messages, either based on given Files, or a given quantity.
 * 
 * @author Mike O'Donnell  github.com/mikerodonnell
 */
public class BatchSender {

	private static final int DEFAULT_DELAY_SECONDS = 1;
	
	private JmsTemplate jmsTemplate;
	
	
	public void send( int messageCount ) throws IOException, InterruptedException {
		send( messageCount, DEFAULT_DELAY_SECONDS );
	}
	
	
	public void send( final File[] messageFiles ) throws IOException, InterruptedException {
		send( messageFiles, DEFAULT_DELAY_SECONDS );
	}
	
	
	/**
	 * publish a specified number simple test JMS messages. pause for the given number of seconds between messages.
	 * example message body: test_message_2016-04-11 14:55:58
	 * 
	 * @param messageCount the number of messages to send, > 0
	 * @param delaySeconds the number of seconds to pause between each message
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void send( int messageCount, int delaySeconds ) throws IOException, InterruptedException {
	
		System.out.println("sending " + messageCount + " simple test messages with " + delaySeconds + " seconds between messages");
		
		final SimpleMessageCreator creator = new SimpleMessageCreator();
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		for( int messageNumber=1; messageNumber<=messageCount; messageNumber++ ) {
			String timestamp = format.format(new Date());
			String messageBody = "test_message_" + messageNumber + "  " + timestamp;
			System.out.println("sending message: " + messageBody);
			creator.setMessage(messageBody);
			jmsTemplate.send(creator);
			
			if(delaySeconds > 0)
				Thread.sleep(1000*delaySeconds);
		}
	}
	
	
	/**
	 * publish a JMS message for each of the given Files. the message body will be the contents of the File. pause for the given number of seconds between messages.
	 * 
	 * @param messageFiles the ASCII Files to use for the JMS message body.
	 * @param delaySeconds the number of seconds to pause between each message
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void send( final File[] messageFiles, int delaySeconds ) throws IOException, InterruptedException {
		
		System.out.println("sending message for each of " + messageFiles.length + " files with " + delaySeconds + " seconds between messages");
		
		final SimpleMessageCreator creator = new SimpleMessageCreator();
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		for( int index=0; index<messageFiles.length; index++ ) {
			String messageBody = FileUtils.readFileToString( messageFiles[index] );
			System.out.println("sending message: " + (index+1) + " of " + messageFiles.length);
			creator.setMessage(messageBody);
			jmsTemplate.send(creator);
			
			if(delaySeconds > 0)
				Thread.sleep(1000*delaySeconds);
		}
	}
	
	
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}
}
