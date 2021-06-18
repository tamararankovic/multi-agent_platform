package agents;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import chatmanager.ChatManagerRemote;
import messagemanager.ACLMessage;
import model.Message;
import model.User;
import websocket.Logger;

@Stateful
@Remote(Agent.class)
public class UserAgent extends BaseAgent {

	private static final long serialVersionUID = 1L;
	
	@EJB ChatManagerRemote chm;
	@EJB Logger logger;
	
	@Override
	public void handleMessage(ACLMessage message) {
		switch(message.getPerformative()) {
		case LOG_IN: {
			String username = message.getUserArg("username").toString();
			String password = message.getUserArg("password").toString();
			logIn(username, password);
			break;
		}
		case REGISTER: {
			String username = message.getUserArg("username").toString();
			String password = message.getUserArg("password").toString();
			register(username, password);
			break;
		}
		case LOG_OUT: {
			String username = message.getUserArg("username").toString();
			logOut(username);
			break;
		}
		case REGISTERED_LIST: {
			String username = message.getUserArg("username").toString();
			getRegistered(username);
			break;
		}
		case LOGGED_IN_LIST: {
			String username = message.getUserArg("username").toString();
			getLoggedIn(username);
			break;
		}
		case SEND_MESSAGE_ALL: {
			String subject = message.getUserArg("subject").toString();
			String content = message.getUserArg("content").toString();
			String sender = message.getUserArg("sender").toString();
			sendMessageToAll(sender, subject, content);
			break;
		}
		case SEND_MESSAGE_USER: {
			String subject = message.getUserArg("subject").toString();
			String content = message.getUserArg("content").toString();
			String sender = message.getUserArg("sender").toString();
			String receiver = message.getUserArg("receiver").toString();
			sendMessage(sender, receiver, subject, content);
			break;
		}
		case GET_MESSAGES: {
			String username = message.getUserArg("username").toString();
			getAllMessages(username);
			break;
		}
		}
	}
	
	private void logIn(String username, String password) {
		boolean success = chm.logIn(username, password);
		if(success)
			logger.send("User with username " + username + " successfully logged in");
		else
			logger.send("User with username " + username + " doesn't exist or the password is incorrect");
	}
	
	private void register(String username, String password) {
		boolean success = chm.register(username, password);
		if(success)
			logger.send("User with username " + username + " successfully registered");
		else
			logger.send("User with username " + username + " already exists");
	}
	
	private void logOut(String username) {
		if(loggedIn(username)) {
			chm.logOut(username);
			logger.send("User with username " + username + " successfully logged out");
		}
	}
	
	private void getLoggedIn(String username) {
		if(loggedIn(username)) {
			List<User> users = chm.getLoggedIn();
			ObjectMapper mapper = new ObjectMapper();
			try {
				String usersJSON = mapper.writeValueAsString(users);
				logger.send("Logged in users: " + usersJSON);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void getRegistered(String username) {
		if(loggedIn(username)) {
			List<User> users = chm.getRegistered();
			ObjectMapper mapper = new ObjectMapper();
			try {
				String usersJSON = mapper.writeValueAsString(users);
				logger.send("Registered users: " + usersJSON);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendMessage(String sender, String receiver, String subject, String content) {
		if(loggedIn(sender)) {
			if(!chm.existsRegistered(receiver)) {
				logger.send("Reciever with username " + receiver + " doesn't exist");
				return;
			}
			Message message = chm.saveMessage(sender, receiver, subject, content);
			ObjectMapper mapper = new ObjectMapper();
			try {
				String messageJSON = mapper.writeValueAsString(message);
				logger.send("Message: " + messageJSON + " sent");
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendMessageToAll(String sender, String subject, String content) {
		if(loggedIn(sender))
			for(User user : chm.getLoggedIn())
				sendMessage(sender, user.getUsername(), subject, content);
	}
	
	private void getAllMessages(String username) {
		if(loggedIn(username)) {
			List<Message> messages = chm.getMessages(username);
			ObjectMapper mapper = new ObjectMapper();
			try {
				String messagesJSON = mapper.writeValueAsString(messages);
				logger.send("Messages for user with username " + username + ": " + messagesJSON);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean loggedIn(String username) {
		if(!chm.existsLoggedIn(username)) {
			logger.send("User with username " + username + " is not logged in");
			return false;
		}
		return true;
	}
}
