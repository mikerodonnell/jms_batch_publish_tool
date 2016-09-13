package demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
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
	private static final String TEMPLATE_DIRECTORY = "src/main/resources/template/";
	private static final String SPRING_CONTEXT_FILE_NAME = "context.xml";
	
	
	private enum MainMenuChoice {
		SIMPLE, DIRECTORY, TEMPLATE;
	}
	
	public static void main( String[] args ) throws IOException, InterruptedException {
		System.out.println("hello, world!");
		
		final ApplicationContext context = new ClassPathXmlApplicationContext(SPRING_CONTEXT_FILE_NAME);
		final BatchSender batchSender = context.getBean( BatchSender.class );
		
		final BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
		int delay = 0;
		Integer messageCount = null;
		MainMenuChoice mainMenuChoice = null;
		try {
			delay = getDelaySeconds(reader);
			mainMenuChoice = getMainMenuChoice(reader);
			
			if( MainMenuChoice.DIRECTORY.equals(mainMenuChoice) ) {
				final File messageDirectory = new File(MESSAGE_FILE_DIRECTORY);
				final File[] messageFiles = messageDirectory.listFiles();
				batchSender.send( messageFiles, delay );
			}
			else if( MainMenuChoice.TEMPLATE.equals(mainMenuChoice) ) {
				final File template = new File(TEMPLATE_DIRECTORY + "template");
				final File inputs = new File(TEMPLATE_DIRECTORY + "inputs.csv");
				
				batchSender.send( template, inputs, delay );
			}
			else {
				messageCount = getMessageCount(reader);
				batchSender.send( messageCount, delay );
			}
		}
		finally {
			reader.close();
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
	 * gets selection of message body source from the user -- either files in a directory, template with variables, or default simple message body.
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private static MainMenuChoice getMainMenuChoice( final BufferedReader reader) throws IOException {
		
		System.out.println("Choose an option: ");
		System.out.println("1) send a series of simple default JMS messages.");
		System.out.println("2) send a JMS message for each file in " + MESSAGE_FILE_DIRECTORY);
		System.out.println("3) send a JMS message for each row in " + TEMPLATE_DIRECTORY + "inputs.csv");
		
		while(true) {
			System.out.print("  => ");
			String input = reader.readLine().trim();
			
			int choice = -1;
			try{
				choice = Integer.valueOf(input);
			}
			catch(NumberFormatException numberFormatException) { }
			
			if(choice == 1)
				return MainMenuChoice.SIMPLE;
			if(choice == 2)
				return MainMenuChoice.DIRECTORY;
			if(choice == 3)
				return MainMenuChoice.TEMPLATE;
			
			System.out.println("choose option 1, 2, or 3 above.");
		}
		
	}
	
	
	/**
	 * gets the number of test messages to send from the user.
	 * 
	 * @param reader
	 * @return a positive Integer, or null to indicate that messages should be sent for all files in MESSAGE_FILE_DIRECTORY.
	 * @throws IOException
	 */
	private static Integer getMessageCount( final BufferedReader reader ) throws IOException {
		Integer messageCount = -1;
		
		System.out.println("Now, enter a number of simple default messages to send: ");
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
			
			if(messageCount != null && messageCount < 1)
				System.out.println("Enter a positive integer:");
			else
				break;
		}
		return messageCount;
	}
}
