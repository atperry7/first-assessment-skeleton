package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.interfaces.IBroadcasterListener;
import com.cooksys.assessment.model.ClientMessages;
import com.cooksys.assessment.model.Commands;
import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable, IBroadcasterListener {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	private ObjectMapper mapper;
	private PrintWriter writer;
	
	// Variables used for data saved for the current client
	private String currentUser;
	private String lastCommand = "unknown";
	private boolean isConnected = false;
	
	// Variables used for time stamping information received from the client
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
		this.mapper = new ObjectMapper();
		
		 try {
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			log.error("Unable to establish connection with writer on the socket: ", e);
		}
	}

	public void run() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {

			Calendar calendar;
			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);				
				
				message = commandCheck(message);
				
				//This attaches a time stamp based on when the server receives the message
				calendar = Calendar.getInstance();
				message.setTimeStamp(simpleDateFormat.format(calendar.getTime()));

				switch (message.getCommand()) {
				case "connect":
					if (!cmdConnected(message)) {
						this.socket.close();						
					}
					break;
				case "disconnect":
					if (cmdDisconnect(message)) {
						this.socket.close();
					}
					break;
				case "echo":
					cmdEcho(message);
					break;
				case "broadcast":
					cmdBroadcast(message);
					break;
				case "users":
					cmdUsers(message);						
					break;
				case "help":
					cmdHelp(message);
					break;
				default:
					if (message.getCommand().startsWith("@")) {
						if (!cmdWhisper(message)) {
							message.setContents(ClientMessages.USER_NOT_FOUND.getMessage());
							writeToClient(message);
						}
					} else {
						message.setContents(ClientMessages.COMMAND_NOT_RECOGNIZED.getMessage());
						writeToClient(message);
					}
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		} finally {
			writer.close();
		}
	}

	/**
	 * Command("@username") used by the client to send a private message to the client
	 * @param message Message Object sent by client
	 */
	private boolean cmdWhisper(Message message) {
		String userToMessage = message.getCommand().substring(1);
		log.info("user <{}> whispered <{}>", message.getUsername(), userToMessage);
		message.setCommand("whisper");
		return Server.whisper(message, userToMessage);
	}

	/**
	 * Command use by client to obtain a list of commands they can use
	 * @param message Message Object sent by client
	 * @throws JsonProcessingException
	 */
	private void cmdHelp(Message message) throws JsonProcessingException {
		log.info("user <{}> requested help", message.getUsername());
		message.setContents(ClientMessages.HELP_COMMAND_MESSAGE.getMessage());
		writeToClient(message);
	}

	/**
	 * Command used by client to obtain a list of currently connected users
	 * @param message Message Object sent by client
	 * @throws JsonProcessingException
	 */
	private void cmdUsers(Message message) throws JsonProcessingException {
		log.info("user <{}> requested currently connected users", message.getUsername());
		message.setContents(Server.getCurrentUsersOnServer());
		writeToClient(message);
	}

	/**
	 * Command used by client to broadcast a message to other clients
	 * @param message Message Object sent by client
	 */
	private void cmdBroadcast(Message message) {
		log.info("user <{}> broadcast message <{}>", message.getUsername(), message.getContents());
		Server.broadcast(message);
	}

	/**
	 * Command used by client to echo a message to the server and back to them
	 * @param message Message Object sent by client
	 * @throws JsonProcessingException
	 */
	private void cmdEcho(Message message) throws JsonProcessingException {
		log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
		writeToClient(message);
	}

	/**
	 * Command used by client to disconnect from server
	 * @param message Message Object sent by client
	 * @return true if the server was able to unregister the client
	 */
	private boolean cmdDisconnect(Message message) {
		if (Server.unregister(this)) {
			log.info("user <{}> disconnected", message.getUsername());
			message.setContents(ClientMessages.HAS_DISCONNECTED.getMessage());
			Server.broadcast(message);
			return true;
		}
		return false;
	}
	
	/**
	 * Command used by client to connect to server
	 * @param message Message Object sent by client
	 * @return true if there is not another client using the current username
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	private boolean cmdConnected(Message message) throws JsonProcessingException, IOException {
		if (isConnected == true) {
			message.setContents(ClientMessages.IS_CONNECTED.getMessage());
			writeToClient(message);
			return true;
		} else if (Server.checkForUser(message.getUsername())) {
			log.info("user <{}> already exisits closing socket", message.getUsername());
			message.setContents(ClientMessages.USER_EXISTS.getMessage());
			writeToClient(message);
			return false;
		} else {
			log.info("user <{}> connected", message.getUsername());
			message.setContents(ClientMessages.HAS_CONNECTED.getMessage());
			Server.broadcast(message);
			Server.register(this);
			currentUser = message.getUsername();
			isConnected = true;
			return true;
		}
	}

	/**
	 * Checks if the user is trying to use last command
	 * @param message Message Object to be checked
	 * @return Message Object after being checked by the method
	 */
	private Message commandCheck(Message message) {
		String currentCommand = message.getCommand();
		
		//Grabs a list of enumerated commands and checks if the command the user used is actually apart of the accepted list
		Commands[] commmands = Commands.values();
		Optional<Commands> commandFound = Stream.of(commmands)
												.filter(command -> command.getCommand().equals(currentCommand))
												.findFirst();
		
		if (commandFound.isPresent() || currentCommand.startsWith("@")) {
			lastCommand = currentCommand;
			
		} else if (!lastCommand.equals("unknown") && !currentCommand.equals("disconnect") && !currentCommand.equals("connect")) {
			String newContents = message.getCommand() + " " + message.getContents();
			message.setCommand(lastCommand);
			message.setContents(newContents);
		}
		
		return message;
	}
	
	/**
	 * Writes messages to the current client
	 * @param message Message object to be sent back to the client
	 * @throws JsonProcessingException Thrown if unable to parse message to JSON
	 */
	private void writeToClient(Message message) throws JsonProcessingException {
			String response = mapper.writeValueAsString(message);
			writer.write(response);
			writer.flush();
	}

	@Override
	public synchronized void receiveMessage(Message message) {
		try {
			writeToClient(message);
		} catch (JsonProcessingException e) {
			log.error("Unable to process JSON data: ", e);
		}
	}

	public String getCurrentUser() {
		return currentUser;
	}

}
