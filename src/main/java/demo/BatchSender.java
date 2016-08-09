package demo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

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
	
	
	public void send( final File[] messageFiles ) throws IOException, InterruptedException {
		send( messageFiles, DEFAULT_DELAY_SECONDS );
	}
	
	/**
	 * publish a JMS message for each file in the given messageFiles, with the contents of the file as the message body.
	 * 
	 * @param messageFiles
	 * @param delaySeconds
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void send( final File[] messageFiles, int delaySeconds ) throws IOException, InterruptedException {
		System.out.println("sending message for each of " + messageFiles.length + " files with " + delaySeconds + " seconds between messages");
		
		final SimpleMessageCreator creator = new SimpleMessageCreator();
		
		for( int index=0; index<messageFiles.length; index++ ) {
			String messageBody = FileUtils.readFileToString( messageFiles[index] );
			System.out.println("sending message: " + (index+1) + " of " + messageFiles.length);
			creator.setMessage(messageBody);
  			jmsTemplate.send(creator);
		}
	}
	
	
	public void send( final File template, final Properties inputs ) throws IOException, InterruptedException {
		send( template, inputs, DEFAULT_DELAY_SECONDS );
	}
	
	
	/**
	 * publish a JMS message based on the given template for each set of values in the given Properties. inputs are injected into the template for the pattern
	 * ${keyName}. comma-delimited Strings are used for sending multiple messages with the same template.
	 * 
	 * for example, for the following template:
	 * <EVENT type="NEW">
	 *   <USERNAME>${username}</USERNAME>
	 *   <PASSWORD>${password}</PASSWORD>
	 * </EVENT>
	 * 
	 * the following properties:
	 * username=tjones,bsmith,jryan
	 * password=admin123,password1,Password123
	 * 
	 * will result in 3 messages, the first being:
	 * <EVENT type="NEW">
	 *   <USERNAME>tjones</USERNAME>
	 *   <PASSWORD>admin123</PASSWORD>
	 * </EVENT>
	 * 
	 * @param template
	 * @param inputs set of key-value-pairs, each value being comma-delimited String. example: username=tjones,bsmith,jryan
	 * @param delaySeconds the number of seconds to pause between each message
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void send( final File template, final Properties inputs, int delaySeconds ) throws IOException, InterruptedException {
		
		System.out.println("creating messages by injecting properties into message body template, with " + delaySeconds + " seconds between messages");
		
		final SimpleMessageCreator creator = new SimpleMessageCreator();
		
		/* suppose we have the following in our properties file:
			username=tjones,bsmith,jryan
			password=admin123,password1,Password123
		*/
		Integer messageCount = null;
		Map<String, List<String>> values = new HashMap<String, List<String>>();
		for( String key : inputs.stringPropertyNames() ) {
			String csv = inputs.getProperty(key);
			List<String> list = Arrays.asList( csv.split(",") );
			
			// the first loop through, capture the messageCount (3 in this case, one message for "tjones"+"admin123", one for "bsmith"+"password1", and one for "jryan"+"Password123"
			if(messageCount==null)
				messageCount = list.size();
			
			values.put(key, list);
		}
		/* values map is now:
			"username" => ["tjones", "bsmith", "jryan"]
			"password" => ["admin123", "password1", "Password123"]
		*/
		
		for( int index=0; index<messageCount; index++ ) { // messageCount is 3 in our example
			String messageBody = FileUtils.readFileToString( template );
			
			for( Object key : inputs.keySet() ) { // our key set is {"username", "password"} in our example
				String keyString = (String) key;
				String injectValue = values.get(keyString).get(index); // injectValue is "tjones" for index=0 and keyString="username"
				messageBody = messageBody.replace( ("${" + keyString + "}"), injectValue ); // look for ${username} and replace it with tjones
			}
			
			System.out.println("sending message: " + (index+1) + " of " + messageCount);
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
