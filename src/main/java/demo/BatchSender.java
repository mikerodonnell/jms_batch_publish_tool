package demo;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.core.JmsTemplate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * publish batches of JMS messages, either based on given Files, or a given quantity.
 *
 * @author Mike O'Donnell  github.com/mikerodonnell
 */
public class BatchSender {

	private static final int DEFAULT_DELAY_SECONDS = 1;

	private JmsTemplate jmsTemplate;

	public void send(int messageCount) throws IOException, InterruptedException {
		send(messageCount, DEFAULT_DELAY_SECONDS);
	}

	/**
	 * publish a specified number simple test JMS messages. pause for the given number of seconds between messages.
	 * example message body: test_message_2016-04-11 14:55:58
	 *
	 * @param messageCount the number of messages to send, > 0
	 * @param delaySeconds the number of seconds to pause between each message
	 * @throws InterruptedException
	 */
	public void send(int messageCount, int delaySeconds) throws InterruptedException {
		System.out.println("sending " + messageCount + " simple test messages with " + delaySeconds + " seconds between messages");

		final SimpleMessageCreator creator = new SimpleMessageCreator();
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));

		for (int messageNumber = 1; messageNumber <= messageCount; messageNumber++) {
			String timestamp = format.format(new Date());
			String messageBody = "test_message_" + messageNumber + "  " + timestamp;
			System.out.println("sending message: " + messageBody);
			creator.setMessage(messageBody);
			jmsTemplate.send(creator);

			if (delaySeconds > 0) {
				Thread.sleep(1000 * delaySeconds);
			}
		}
	}

	public void send(final File messageDirectory) throws IOException {
		send(messageDirectory, DEFAULT_DELAY_SECONDS);
	}

	/**
	 * publish a JMS message for each file in the given messageFiles, with the contents of the file as the message body.
	 *
	 * @param messageDirectory
	 * @param delaySeconds
	 * @throws IOException
	 */
	public void send(final File messageDirectory, int delaySeconds) throws IOException {
		final File[] messageFiles = messageDirectory.listFiles(new HiddenFileFilter());
		System.out.println("sending message for each of " + messageFiles.length + " files with " + delaySeconds + " seconds between messages");

		final SimpleMessageCreator creator = new SimpleMessageCreator();

		for (int index = 0; index < messageFiles.length; index++) {
			String messageBody = FileUtils.readFileToString(messageFiles[index]);
			System.out.println("sending message: " + (index + 1) + " of " + messageFiles.length);
			creator.setMessage(messageBody);
			jmsTemplate.send(creator);
		}
	}

	public void send(final File template, final File inputs) throws IOException, InterruptedException {
		send(template, inputs, DEFAULT_DELAY_SECONDS);
	}

	/**
	 * publish a JMS message based on the given template File for each set of values in the given CSV File. inputs are injected into the template for the pattern
	 * ${keyName}. comma-delimited Strings are used for sending multiple messages with the same template.
	 * <p>
	 * for example, for the following template:
	 * <EVENT type="NEW">
	 * <USERNAME>${username}</USERNAME>
	 * <PASSWORD>${password}</PASSWORD>
	 * </EVENT>
	 * <p>
	 * the following CSV:
	 * username,password
	 * tjones,admin123
	 * bsmith,password1
	 * jryan,Password123
	 * <p>
	 * will result in 3 messages, the first being:
	 * <EVENT type="NEW">
	 * <USERNAME>tjones</USERNAME>
	 * <PASSWORD>admin123</PASSWORD>
	 * </EVENT>
	 *
	 * @param template
	 * @param inputs
	 * @param delaySeconds the number of seconds to pause between each message
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void send(final File template, final File inputs, int delaySeconds) throws IOException, InterruptedException {
		System.out.println("creating messages by injecting properties into message body template, with " + delaySeconds + " seconds between messages");

		final SimpleMessageCreator creator = new SimpleMessageCreator();

		final String templateBody = FileUtils.readFileToString(template);

		final Map<String, Integer> columnIndices = new HashMap<>();

		final Reader reader = new FileReader(inputs);

		// we need to collect the template variables ahead of time, because we require that each has a value from the CSV
		final Collection<String> templateVariables = new HashSet<>();
		final Pattern pattern = Pattern.compile("\\$\\{.*\\}"); // double slash to for each regex escape character, because the backslash itself is a Java special character
		final Matcher matcher = pattern.matcher(templateBody);

		while (matcher.find()) {
			templateVariables.add(matcher.group()); // the values in templateVariables include the brackets, example: "${username}"
		}

		final Iterable<CSVRecord> records;
		try {
			// the Reader stays open until the last message is published; we're just getting an Iterable, not pulling all CSV records into memory
			records = CSVFormat.DEFAULT.parse(reader);
			
			/* suppose we have the following in our CSV file:
				username,password
				tjones,admin123
				bsmith,password1
			*/
			csvLoop:
			// we'll use this tag to continue to the next record when skipping invalid ones.
			for (CSVRecord record : records) { // records has length/size 3 in our example
				/* if this is our first record in the CSV, it's the header row. scan the headers and store them in columnIndices so we can process the following rows. example:
					columnIndices => {
						"username" => 0
						"password" => 1
					}
				*/
				if (record.getRecordNumber() == 1) {
					for (int columnIndex = 0; columnIndex < record.size(); columnIndex++) {
						columnIndices.put(record.get(columnIndex), columnIndex);
					}

					continue; // all done with this record, skip to next record
				}

				String messageBody = templateBody; // start with the template, then we'll inject variables into it one at a time

				for (String templateVariable : templateVariables) {
					String columnName = templateVariable.substring(2, templateVariable.length() - 1); // for example templateVariable=="${username}", columnName=="username"
					String injectValue = record.get(columnIndices.get(columnName));

					if (StringUtils.isEmpty(injectValue)) {
						System.out.println("WARN: skipping message # " + (record.getRecordNumber() - 1) + " because no value was found in the CSV for the template variable " + templateVariable);
						continue csvLoop;
					}

					messageBody = messageBody.replace(templateVariable, injectValue);
				}

				System.out.println("sending message # " + (record.getRecordNumber() - 1));
				creator.setMessage(messageBody);
				jmsTemplate.send(creator);

				if (delaySeconds > 0) {
					Thread.sleep(1000 * delaySeconds);
				}
			}
		} finally {
			reader.close();
		}
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}
}
