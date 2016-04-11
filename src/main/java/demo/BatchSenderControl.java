package demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * main class for capturing user options and initiating batch publish.
 * 
 * @author Mike O'Donnell  github.com/mikerodonnell
 */
public class BatchSenderControl {
	
	private static final String MESSAGE_FILE_DIRECTORY = "src/main/resources/message";
	private static final String SPRING_CONTEXT_FILE_NAME = "context.xml";
	
	
	public static void main( String[] args ) throws IOException, InterruptedException {
		System.out.println("hello, world!");
		
		final ApplicationContext context = new ClassPathXmlApplicationContext(SPRING_CONTEXT_FILE_NAME);
		final BatchSender batchSender = context.getBean( BatchSender.class );
		
		final BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
		
		int delay = 0;
		Integer messageCount = null;
		try {
			delay = getDelaySeconds(reader);
			messageCount = getMessageCount(reader);
		}
		finally {
			reader.close();
		}
		
		if( messageCount == null ) {
			final File messageDirectory = new File(MESSAGE_FILE_DIRECTORY);
			final File[] messageFiles = messageDirectory.listFiles();
			batchSender.send( messageFiles, delay );
		}
		else {
			batchSender.send( messageCount, delay );
		}
		
		( (AbstractApplicationContext) context).close();
		System.out.println("done publishing all messages.");
		System.out.println("goodbye, cruel world.");
	}
	
	
	/**
	 * get the number of seconds to pause between messages from the user.
	 * 
	 * @param reader
	 * @return an integer, 0 or greater
	 * @throws IOException
	 */
	private static int getDelaySeconds( final BufferedReader reader ) throws IOException {
		int delaySeconds = -1;
		
		System.out.println("How many seconds delay between each message?:");
		while(true) {
			System.out.print("  => ");
			String input = reader.readLine().trim();
			
			try {
				delaySeconds = Integer.valueOf(input);
			}
			catch(NumberFormatException numberFormatException) { }
			
			if(delaySeconds < 0)
				System.out.println("Enter a positive integer, or 0 for no delay:");
			else
				break;
		}
		
		return delaySeconds;
	}
	
	
	/**
	 * gets the number of test messages to send from the user. returns null to indicate that messages should be sent for all files in MESSAGE_FILE_DIRECTORY.
	 * 
	 * @param reader
	 * @return a positive Integer, or null to indicate that messages should be sent for all files in MESSAGE_FILE_DIRECTORY.
	 * @throws IOException
	 */
	private static Integer getMessageCount( final BufferedReader reader ) throws IOException {
		Integer messageCount = -1;
		
		System.out.println("Now, press enter to send a JMS message for each file in " + MESSAGE_FILE_DIRECTORY + ". Or, enter a number of simple messages to send: ");
		while(true) {
			System.out.print("  => ");
			String input = reader.readLine().trim();
			
			if(StringUtils.isBlank(input)) {
				messageCount = null;
			}
			else {
				try {
					messageCount = Integer.valueOf(input);
				}
				catch(NumberFormatException numberFormatException) { }
			}
			
			if(messageCount == null || messageCount < 1)
				System.out.println("Enter a positive integer, or press return to use files in " + MESSAGE_FILE_DIRECTORY + ":");
			else
				break;
		}
		return messageCount;
	}
}
