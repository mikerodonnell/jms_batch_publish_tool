package demo;

import javax.jms.*;

import org.springframework.jms.core.MessageCreator;

/**
 * a simple MessageCreator implementation to create JMS messages from Strings.
 * 
 * @author Mike O'Donnell  github.com/mikerodonnell
 */
public class SimpleMessageCreator implements MessageCreator {

	private String message;
	private TextMessage textMessage;

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageId() throws Exception {
		return textMessage.getJMSMessageID();
	}

	public void setTextMessage(TextMessage textMessage) {
		this.textMessage = textMessage;
	}

	public TextMessage getTextMessage() {
		return textMessage;
	}

	@Override
	public Message createMessage(Session session) throws JMSException {
		textMessage = session.createTextMessage(message);
		return textMessage;
	}
}
